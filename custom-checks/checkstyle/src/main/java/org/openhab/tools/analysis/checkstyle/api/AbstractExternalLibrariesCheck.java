/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.tools.analysis.checkstyle.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.pde.core.build.IBuildEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Abstract class that contains logic that is used by other external libraries checks.
 *
 * @author Velin Yordanov - Initial contribution
 */
public abstract class AbstractExternalLibrariesCheck extends AbstractStaticCheck {
    protected static final String LIB_FOLDER_NAME = "lib";
    protected static final String JAR_FILE_EXTENSION = ".jar";
    private static final String BIN_EXCLUDES = "bin.excludes";
    protected static final String COULD_NOT_OPEN_BUILD_PROPERTIES = "Could not open build properties";
    private final Logger logger = LoggerFactory.getLogger(AbstractExternalLibrariesCheck.class);

    protected List<String> getLibFolderJarFiles(File libDirectory, String buildPropertiesPath) {
        List<String> excludedJarFiles = new ArrayList<>();
        try {
            excludedJarFiles = getExcludedJarFiles(buildPropertiesPath);
        } catch (IOException e) {
            logger.error(COULD_NOT_OPEN_BUILD_PROPERTIES);
        }

        File[] files = libDirectory.listFiles();
        List<String> libJarFiles = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(JAR_FILE_EXTENSION)) {
                String fileName = LIB_FOLDER_NAME + "/" + file.getName();
                if (!excludedJarFiles.contains(fileName)) {
                    libJarFiles.add(fileName);
                }
            }
        }

        return libJarFiles;
    }

    private List<String> getExcludedJarFiles(String buildPropertiesPath) throws IOException {
        File buildProperties = new File(buildPropertiesPath);
        FileText buildPropertiesFileText = new FileText(buildProperties, "UTF-8");
        IBuildEntry binExcludesEntry = null;
        try {
            binExcludesEntry = parseBuildProperties(buildPropertiesFileText).getEntry(BIN_EXCLUDES);
        } catch (CheckstyleException ex) {
            throw new IOException(COULD_NOT_OPEN_BUILD_PROPERTIES);
        }

        List<String> excludedJarFiles = new ArrayList<>();
        if (binExcludesEntry != null) {
            String[] binExcludes = binExcludesEntry.getTokens();
            for (int i = 0; i < binExcludes.length; i++) {
                if (binExcludes[i].endsWith(JAR_FILE_EXTENSION)) {
                    // Trimming in case there is a space at the end of the entry
                    excludedJarFiles.add(binExcludes[i].trim());
                }
            }
        }

        return excludedJarFiles;
    }

    protected void checkFiles(List<String> jarFilesToCheck, List<String> jarFiles, String messageTemplate) {
        List<String> filesToCheck = new ArrayList<>(jarFilesToCheck);
        filesToCheck.removeAll(jarFiles);
        filesToCheck.forEach(fileName -> {
            log(0, String.format(messageTemplate, fileName));
        });
    }
}
