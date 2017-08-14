/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Checks if the required files for the bundle are present. These required files
 * are listed as a configuration properties. The missing files will be reported
 * one by one.
 *
 * @author Petar Valchev
 *
 */
public class RequiredFilesCheck extends AbstractStaticCheck {
    private String fileParentDirectoryPath;

    /**
     * Required files for a project which can be set as a configuration
     * property(in the rulesets/checkstyle/*.xml files).
     */
    private List<String> requiredFiles;
    private List<String> foundFiles = new ArrayList<>();

    public void setExtensions(String[] extensions) {
        setFileExtensions(extensions);
    }

    // configuration property for the required files
    public void setRequiredFiles(String[] files) {
        requiredFiles = Arrays.asList(files);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (fileParentDirectoryPath == null) {
            fileParentDirectoryPath = file.getParent();
        }

        String fileName = file.getName();
        if (requiredFiles.contains(fileName)) {
            foundFiles.add(fileName);
        }
    }

    @Override
    public void finishProcessing() {
        List<String> notFoundFiles = new ArrayList<>(requiredFiles);
        notFoundFiles.removeAll(foundFiles);

        for (String file : notFoundFiles) {
            logMessage(file);
        }
    }

    private void logMessage(String fileName) {
        String filePath;
        if (fileParentDirectoryPath != null) {
            filePath = fileParentDirectoryPath + File.separator + fileName;
        } else {
            filePath = File.separator + fileName;
        }
        String message = String.format("Missing %s file.", fileName);
        logMessage(filePath, 0, fileName, message);
    }
}
