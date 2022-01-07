/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the required files for the bundle are present. These required files
 * are listed as a configuration properties. The missing files will be reported
 * one by one.
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Use relative path for required files
 */
public class RequiredFilesCheck extends AbstractStaticCheck {
    private Path projectRootPath;

    /**
     * The relative paths to the root directory of the files that are required
     *
     * Required files for a project which can be set as a configuration
     * property(in the rulesets/checkstyle/*.xml files).
     */
    private List<Path> requiredFiles;
    /**
     * The relative paths to the root directory of the files that are found
     */
    private List<Path> foundFiles = new ArrayList<>();

    public void setExtensions(String[] extensions) {
        setFileExtensions(extensions);
    }

    // configuration property for the required files
    public void setRequiredFiles(String[] files) {
        requiredFiles = Arrays.stream(files).map(Paths::get).collect(Collectors.toList());
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        Path absoluteFilePath = file.toPath();
        for (Path relativeRequiredPath : requiredFiles) {
            if (absoluteFilePath.endsWith(relativeRequiredPath)) {
                if (projectRootPath == null) {
                    projectRootPath = getRootPath(absoluteFilePath, relativeRequiredPath);
                }
                foundFiles.add(relativeRequiredPath);
            }
        }
    }

    private Path getRootPath(Path absolute, Path relative) {
        Path root = absolute;
        for (int i = 0; i < relative.getNameCount(); i++) {
            root = root.getParent();
        }
        return root;
    }

    @Override
    public void finishProcessing() {
        List<Path> notFoundFiles = new ArrayList<>(requiredFiles);
        notFoundFiles.removeAll(foundFiles);

        for (Path path : notFoundFiles) {
            logMessage(path);
        }
    }

    private void logMessage(Path path) {
        if (projectRootPath != null) {
            path = projectRootPath.resolve(path);
        } else {
            path = Paths.get(File.separator).resolve(path);
        }
        String fileName = path.getFileName().toString();
        String message = String.format("Missing %s file.", fileName);
        logMessage(path.toString(), 0, fileName, message);
    }
}
