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
import java.io.FilenameFilter;

import org.openhab.tools.analysis.checkstyle.api.AbstractExternalLibrariesCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Verifies that all jar files in the bundle are located in a folder named "lib".
 *
 * @author Velin Yordanov - Initial contribution
 *
 */
public class OutsideOfLibExternalLibrariesCheck extends AbstractExternalLibrariesCheck {
    private static final String TARGET_FOLDER = "target";
    private static final String JAR_FILES_NEED_TO_BE_PLACED_IN_A_LIB_FOLDER = "All jar files need to be placed inside a lib folder and added to MANIFEST.MF and build.properties";

    public OutsideOfLibExternalLibrariesCheck() {
        setFileExtensions(PROPERTIES_EXTENSION);
    }

    private void checkBundleForOutOfPlaceJarFiles(File bundleDirectory, String folderToSkip) {
        File[] files = bundleDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // When build with maven a "target" folder is generated.
                if (name.equals(folderToSkip) || name.equals(TARGET_FOLDER)) {
                    return false;
                }

                if (name.endsWith(JAR_FILE_EXTENSION)) {
                    log(0, JAR_FILES_NEED_TO_BE_PLACED_IN_A_LIB_FOLDER);
                }

                return dir.isDirectory();
            }
        });

        if (files != null) {
            for (File file : files) {
                checkBundleForOutOfPlaceJarFiles(file, null);
            }
        }
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (!file.getName().equals(BUILD_PROPERTIES_FILE_NAME)) {
            return;
        }
        File bundleDirectory = file.getParentFile();
        checkBundleForOutOfPlaceJarFiles(bundleDirectory, LIB_FOLDER_NAME);
    }
}
