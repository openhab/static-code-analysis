/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.google.common.collect.ImmutableList;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks whether whitespace characters are use instead of tabs in xml files
 * indentations and generates warnings in such cases.
 *
 * @author Lyubomir Papazov - initial contribution
 * @author Kristina Simova - Removed REGEX and changed the way we look for
 *         indentation
 * @author Velin Yordanov - Made the check more generic and added exceptions
 *         property
 *
 */
public class IndentationCheck extends AbstractStaticCheck {
    private static final String TAB_CHARACTER = "\t";
    private static final String SPACE_CHARACTER = " ";
    private static final String TABS = "tabs";
    private static final String WARNING_MESSAGE = "Not supported indentation characters used";
    private boolean onlyShowFirstWarning;
    private Map<String, String> fileExtensionsToIndentation = new Hashtable<>();
    private Collection<String> exceptions = new ArrayList<>();
    private final Log logger = LogFactory.getLog(this.getClass());

    public void setOnlyShowFirstWarning(Boolean showFirstExceptionOnly) {
        this.onlyShowFirstWarning = showFirstExceptionOnly;
    }

    public void setFileExtensionsToIndentation(String value) {
        String[] fileExtensionsAndIndentation = value.split("[-,]");
        int length = fileExtensionsAndIndentation.length;
        if (length % 2 != 0) {
            logger.error(
                    "FileExtensionsToIndentation not set correctly. It should be file extensions and indentation character.Skipping last parameter.");
            length -= 1;
        }

        for (int i = 0; i < length; i += 2) {
            String indentationType = fileExtensionsAndIndentation[i + 1];
            if (!("tabs".equals(indentationType) || "spaces".equals(indentationType))) {
                throw new IllegalArgumentException("Only tabs or spaces can be used as indentation symbols");
            }

            fileExtensionsToIndentation.put(fileExtensionsAndIndentation[i], fileExtensionsAndIndentation[i + 1]);
        }

        setFileExtensions(
                fileExtensionsToIndentation.keySet().toArray(new String[fileExtensionsToIndentation.keySet().size()]));

    }

    public void setExceptions(String[] value) {
        exceptions = ImmutableList.copyOf(value);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) {
        processIdentationCheck(fileText, getIndentationCharacterForFile(file.getName()));
    }

    private String getIndentationCharacterForFile(String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);
        String indentationCharacter = TABS.equals(fileExtensionsToIndentation.get(fileExtension)) ? TAB_CHARACTER
                : SPACE_CHARACTER;
        if (exceptions.contains(fileName)) {
            if (indentationCharacter == TAB_CHARACTER) {
                indentationCharacter = SPACE_CHARACTER;
            } else {
                indentationCharacter = TAB_CHARACTER;
            }
        }

        return indentationCharacter;
    }

    private void processIdentationCheck(FileText fileText, String indentationCharacter) {
        for (int lineNumber = 0; lineNumber < fileText.size(); lineNumber++) {
            String line = fileText.get(lineNumber);
            // if line is empty and does not contain only tabs for indentation
            if (line.trim().isEmpty() && !doesLineContainOnlyIndentationCharacters(line, indentationCharacter)) {
                if (onlyShowFirstWarning) {
                    logMessage(lineNumber);
                    return;
                }
                logMessage(lineNumber);
                continue;
            }
            int indexNonWhitespaceCharacter = line.indexOf(line.trim());
            if (indexNonWhitespaceCharacter > 0) {
                // we get the part of the line before the first non whitespace character
                String lineBeforeCharacter = line.substring(0, indexNonWhitespaceCharacter);
                if (!doesLineContainOnlyIndentationCharacters(lineBeforeCharacter, indentationCharacter)) {
                    if (onlyShowFirstWarning) {
                        logMessage(lineNumber);
                        return;
                    }
                    logMessage(lineNumber);
                }
            }
        }
    }

    /**
     * Checks if line contains only tabs used for indentation
     *
     * @param lineBeforeCharacter
     *            the String before the first non whitespace character
     * @return true if line contains only tabs for indentation, false otherwise
     */
    private boolean doesLineContainOnlyIndentationCharacters(String lineBeforeCharacter, String indentationCharacter) {
        return StringUtils.containsOnly(lineBeforeCharacter, indentationCharacter);
    }

    /**
     * Logs the warning message
     *
     * @param lineNumber
     *            the line number where the message should be logged
     */
    private void logMessage(int lineNumber) {
        log(lineNumber + 1, WARNING_MESSAGE);
    }
}
