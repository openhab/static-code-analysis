/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ManifestExternalLibrariesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Test for the {@link ManifestExternalLibrariesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class ManifestExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CONFIGURATION = createCheckConfig(ManifestExternalLibrariesCheck.class);
    private static String TEST_FILE_NAME = "MANIFEST.MF";
    private static final String MAIN_DIRECTORY = "manifestExternalLibrariesCheck";

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = createCheckConfig(TreeWalker.class);
        configParent.addChild(config);
        return configParent;
    }

    @Test
    public void shouldNotLogWhenBundleIsValid() throws Exception {
        final String VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator + "validBundle"
                + File.separator + "META-INF";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePath(VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES), warningMessages);
    }

    @Test
    public void shouldLogWhenBundleIsMissingLibFolder() throws Exception {
        final String BUNDLE_WITH_MISSING_LIB_FOLDER = MAIN_DIRECTORY + File.separator + "bundleWithoutLibFolder"
                + File.separator + "META-INF";
        String message = "All jar files need to be placed inside a lib folder.";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyManifest(getManifestFilePath(BUNDLE_WITH_MISSING_LIB_FOLDER), warningMessages);
    }

    @Test
    public void shouldLogWhenBundleIsMissingFilesFromLibFolder() throws Exception {
        final String BUNDLE_WITH_MISSING_FILES_FROM_LIB_FOLDER = MAIN_DIRECTORY + File.separator
                + "bundleWithMissingFilesFromLibFolder" + File.separator + "META-INF";
        String message = "The jar file lib/test2.jar is not present in the lib folder";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyManifest(getManifestFilePath(BUNDLE_WITH_MISSING_FILES_FROM_LIB_FOLDER), warningMessages);
    }

    @Test
    public void shouldNotLogWhenThereAreNoExternalLibraries() throws Exception {
        final String BUNDLE_WITHOUT_EXTERNAL_DEPENDANCIES = MAIN_DIRECTORY + File.separator
                + "bundleWithoutExternalLibraries" + File.separator + "META-INF";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePath(BUNDLE_WITHOUT_EXTERNAL_DEPENDANCIES), warningMessages);
    }

    @Test
    public void shouldLogWhenThereAreMissingFilesInManifest() throws Exception {
        final String BUNDLE_WITH_MISSING_FILES_IN_MANIFEST = MAIN_DIRECTORY + File.separator
                + "bundleWithMissingFilesInManifest" + File.separator + "META-INF";
        String message = "The jar file lib/test2.jar is present in the lib folder but is not present in the MANIFEST.MF file";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyManifest(getManifestFilePath(BUNDLE_WITH_MISSING_FILES_IN_MANIFEST), warningMessages);
    }

    @Test
    public void shouldNotLogWhenThereAreExcludedFiles() throws Exception {
        final String BUNDLE_WITH_EXCLUDED_FILES = MAIN_DIRECTORY + File.separator + "bundleWithExcludedFiles"
                + File.separator + "META-INF";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePath(BUNDLE_WITH_EXCLUDED_FILES), warningMessages);
    }

    @Test
    public void shouldNotLogWhenThereAreSpacesInManifest() throws Exception {
        final String BUNDLE_WITH_SPACES_IN_MANIFEST = MAIN_DIRECTORY + File.separator + "bundleWithSpacesInManifest"
                + File.separator + "META-INF";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePath(BUNDLE_WITH_SPACES_IN_MANIFEST), warningMessages);
    }

    @Test
    public void shouldNotLogWhenBundleHasVariousBundleClasspathEntries() throws Exception {
        final String VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator
                + "bundleWithVariousBundleClasspathEntries" + File.separator + "META-INF";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePath(VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES), warningMessages);
    }

    private String getManifestFilePath(String bundlePath) throws IOException {
        return getPath(bundlePath + File.separator + TEST_FILE_NAME);
    }

    private void verifyManifest(String filePath, String[] warningMessages) throws Exception {
        verify(CONFIGURATION, filePath, warningMessages);
    }
}
