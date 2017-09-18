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
import java.util.List;

import org.openhab.tools.analysis.checkstyle.api.AbstractExternalLibrariesCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Verifies that all jar files in the bundle are located in a folder named "lib".
 *
 * @author Velin Yordanov - Initial contribution
 *
 */
public class OutsideOfLibExternalLibrariesCheck extends AbstractExternalLibrariesCheck {
    private List<String> ignoredDirectories;
    private static final String JAR_FILES_NEED_TO_BE_PLACED_IN_A_LIB_FOLDER = "There is a jar outside of the lib folder %s"
            + File.separator + "%s";

    public OutsideOfLibExternalLibrariesCheck() {
        setFileExtensions(PROPERTIES_EXTENSION);
    }

    public void setIgnoredDirectories(String[] values) {
        ignoredDirectories = Arrays.asList(values);
    }

    private void checkBundleForOutOfPlaceJarFiles(File bundleDirectory, String folderToSkip) {
        File[] files = bundleDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // When build with maven a "target" folder is generated.
                if (name.equals(folderToSkip) || containsIgnoredDirectories(name)) {
                    return false;
                }

                if (name.endsWith(JAR_FILE_EXTENSION)) {
                    log(0, String.format(JAR_FILES_NEED_TO_BE_PLACED_IN_A_LIB_FOLDER, dir.getAbsolutePath(), name));
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

    private boolean containsIgnoredDirectories(String directoryName) {
        return ignoredDirectories.contains(directoryName);
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
