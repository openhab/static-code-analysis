/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.osgi.core.BundleInfo;
import org.openhab.tools.analysis.checkstyle.api.AbstractExternalLibrariesCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * A check that asserts that all jar files from the lib folder are added to MANIFEST.MF and all jar files in MANIFEST.MF
 * are
 * present in the lib folder.
 *
 * @author Velin Yordanov - Initial contribution
 *
 */
public class ManifestExternalLibrariesCheck extends AbstractExternalLibrariesCheck {
    private static final String COULD_NOT_OPEN_MANIFEST = "Could not open MANIFEST.MF";
    private static final String FILES_NEED_TO_BE_IN_A_LIB_FOLDER = "All jar files need to be placed inside a lib folder.";
    private static final String JAR_NOT_PRESENT_IN_LIB_FOLDER = "The jar file %s is not present in the lib folder";
    private static final String JAR_PRESENT_IN_LIB_NOT_IN_MANIFEST = "The jar file %s is present in the lib folder but is not present in the MANIFEST.MF file";
    private static final Log logger = LogFactory.getLog(ManifestExternalLibrariesCheck.class);

    public ManifestExternalLibrariesCheck() {
        // build.properties will not be explicitly processed by the check.
        setFileExtensions(MANIFEST_EXTENSION);
    }

    private List<String> getManifestJarFiles(FileText fileText) throws IOException {
        BundleInfo manifest = null;
        try {
            manifest = parseManifestFromFile(fileText);
        } catch (CheckstyleException ex) {
            throw new IOException(COULD_NOT_OPEN_MANIFEST);
        }

        List<String> classpathEntries = manifest.getClasspath();
        if (classpathEntries != null) {
            // Spaces at the end of the bundle classpath are trimmed.
            classpathEntries.replaceAll(String::trim);

            // Binaries, compiled from the bundle sources are excluded. We will check only external binaries.
            classpathEntries.removeIf(x -> !x.contains(".jar"));
            return classpathEntries;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (!file.getName().equals(MANIFEST_FILE_NAME)) {
            return;
        }

        final String rootFolderPath = file.getParentFile().getParentFile().getAbsolutePath();
        final String buildPropertiesPath = rootFolderPath + File.separator + "build.properties";
        final String libFolderPath = rootFolderPath + File.separator + LIB_FOLDER_NAME;

        List<String> manifestJarFiles = Collections.emptyList();
        try {
            manifestJarFiles = getManifestJarFiles(fileText);
        } catch (IOException e) {
            logger.error(COULD_NOT_OPEN_MANIFEST);
            return;
        }

        File lib = new File(libFolderPath);
        if (!lib.exists() || !lib.isDirectory()) {
            if (manifestJarFiles.isEmpty()) {
                return;
            }

            log(0, FILES_NEED_TO_BE_IN_A_LIB_FOLDER);
            return;
        }

        List<String> libJarFiles = getLibFolderJarFiles(lib, buildPropertiesPath);
        checkFiles(manifestJarFiles, libJarFiles, JAR_NOT_PRESENT_IN_LIB_FOLDER);
        checkFiles(libJarFiles, manifestJarFiles, JAR_PRESENT_IN_LIB_NOT_IN_MANIFEST);

    }
}
