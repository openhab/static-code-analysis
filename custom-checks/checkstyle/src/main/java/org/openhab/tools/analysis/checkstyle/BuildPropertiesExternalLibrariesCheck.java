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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractExternalLibrariesCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * A check that asserts that all jar files in the lib folder are added to build.properties and all jar files in
 * build.properties
 * are added to the lib folder.
 *
 * @author Velin Yordanov - Initial contribution
 */
public class BuildPropertiesExternalLibrariesCheck extends AbstractExternalLibrariesCheck {
    private static final String BIN_INCLUDES = "bin.includes";
    private static final String FILES_NEED_TO_BE_IN_A_LIB_FOLDER = "All jar files need to be placed inside a lib folder.";
    private static final String JAR_PRESENT_IN_BUILD_PROPERTIES_NOT_IN_LIB = "The file %s is present in the build properties but not in the lib folder.";
    private static final String JAR_PRESENT_IN_LIB_NOT_IN_BUILD_PROPERTIES = "The jar file %s is present in the lib folder but is not present in the build properties";
    private final Logger logger = LoggerFactory.getLogger(BuildPropertiesExternalLibrariesCheck.class);

    public BuildPropertiesExternalLibrariesCheck() {
        // build.properties will not be explicitly processed by the check.
        setFileExtensions(PROPERTIES_EXTENSION);
    }

    private List<String> getBuildPropertiesJarFiles(FileText buildProperties) {
        List<String> buildPropertiesJarFiles = new ArrayList<>();
        try {
            for (String token : getBinIncludesTokens(buildProperties)) {
                if (token.contains(JAR_FILE_EXTENSION)) {
                    // Trimming in case there is a space at the end of the entry
                    buildPropertiesJarFiles.add(token.trim());
                }
            }
        } catch (IOException e) {
            logger.error(COULD_NOT_OPEN_BUILD_PROPERTIES);
        }

        return buildPropertiesJarFiles;
    }

    private String[] getBinIncludesTokens(FileText buildProperties) throws IOException {
        IBuildEntry binIncludes = null;
        try {
            binIncludes = parseBuildProperties(buildProperties).getEntry(BIN_INCLUDES);
        } catch (CheckstyleException ex) {
            throw new IOException(COULD_NOT_OPEN_BUILD_PROPERTIES);
        }

        return binIncludes.getTokens();
    }

    private boolean checkIfBuildPropertiesContainsFolder(String[] binIncludesTokens) {
        List<String> tokens = Arrays.asList(binIncludesTokens);

        return (tokens.contains(LIB_FOLDER_NAME + "/") || tokens.contains(LIB_FOLDER_NAME + "/*"));
    }

    private boolean checkIfLibFolderExists(File file) {
        File rootFolder = file.getParentFile();
        for (File bundleFile : rootFolder.listFiles()) {
            if (LIB_FOLDER_NAME.equals(bundleFile.getName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (!file.getName().equals(BUILD_PROPERTIES_FILE_NAME)) {
            return;
        }

        final String rootFolderPath = file.getParentFile().getAbsolutePath();
        final File libDirectory = new File(rootFolderPath + File.separator + LIB_FOLDER_NAME);
        String[] binIncludes = null;
        try {
            binIncludes = getBinIncludesTokens(fileText);
        } catch (IOException e) {
            logger.error(COULD_NOT_OPEN_BUILD_PROPERTIES);
            return;
        }

        boolean containsFolder = checkIfBuildPropertiesContainsFolder(binIncludes);
        boolean containsLib = checkIfLibFolderExists(file);

        List<String> libJarFiles = new ArrayList<>();
        if (containsLib) {
            libJarFiles = getLibFolderJarFiles(libDirectory, file.getAbsolutePath());
        }

        if (containsFolder && containsLib) {
            return;
        }

        List<String> buildPropertiesJarFiles = getBuildPropertiesJarFiles(fileText);

        if (containsFolder || !buildPropertiesJarFiles.isEmpty()) {
            if (!containsLib) {
                log(0, FILES_NEED_TO_BE_IN_A_LIB_FOLDER);
                return;
            }
        }

        checkFiles(buildPropertiesJarFiles, libJarFiles, JAR_PRESENT_IN_BUILD_PROPERTIES_NOT_IN_LIB);
        checkFiles(libJarFiles, buildPropertiesJarFiles, JAR_PRESENT_IN_LIB_NOT_IN_BUILD_PROPERTIES);
    }
}
