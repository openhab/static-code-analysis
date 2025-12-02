/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.tools.analysis.checkstyle;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;

/**
 * Checks whether whitespace characters are use instead of tabs in xml files
 * indentations and generates warnings in such cases.
 *
 * @author Lyubomir Papazov - Initial contribution
 * @author Kristina Simova - Removed REGEX and changed the way we look for indentation
 */
public class OnlyTabIndentationCheck extends AbstractStaticCheck {

    private static final String TAB_CHARACTER = "\t";
    private static final String WARNING_MESSAGE = "There were whitespace characters used for indentation. Please use tab characters instead";
    private boolean onlyShowFirstWarning;

    public void setOnlyShowFirstWarning(Boolean showFirstExceptionOnly) {
        this.onlyShowFirstWarning = showFirstExceptionOnly;
    }

    public void setFileTypes(String[] value) {
        setFileExtensions(value);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) {
        MessageDispatcher dispatcher = getMessageDispatcher();
        dispatcher.fireFileStarted(file.getPath());
        processTabIndentationCheck(fileText);
        fireErrors(file.getAbsolutePath());
        dispatcher.fireFileFinished(file.getPath());
    }

    private void processTabIndentationCheck(FileText fileText) {
        for (int lineNumber = 0; lineNumber < fileText.size(); lineNumber++) {
            String line = fileText.get(lineNumber);
            // if line is empty and does not contain only tabs for indentation
            if (line.trim().isEmpty() && !doesLineContainOnlyTabs(line)) {
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
                if (!doesLineContainOnlyTabs(lineBeforeCharacter)) {
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
     * @param lineBeforeCharacter the String before the first non whitespace character
     * @return true if line contains only tabs for indentation, false otherwise
     */
    private boolean doesLineContainOnlyTabs(String lineBeforeCharacter) {
        return StringUtils.containsOnly(lineBeforeCharacter, TAB_CHARACTER);
    }

    /**
     * Logs the warning message
     *
     * @param lineNumber the line number where the message should be logged
     */
    private void logMessage(int lineNumber) {
        log(lineNumber + 1, WARNING_MESSAGE);
    }
}
