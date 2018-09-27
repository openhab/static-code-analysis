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
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Test for the {@link ManifestExternalLibrariesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class ManifestExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(ManifestExternalLibrariesCheck.class);
    private static String TEST_FILE_NAME = "MANIFEST.MF";
    private static final String TEST_FOLDER_NAME = "META-INF";

    @Override
    protected String getPackageLocation() {
        return "checkstyle/manifestExternalLibrariesCheck";
    }

    @Test
    public void shouldNotLogWhenBundleIsValid() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("validBundle")), warningMessages);
    }

    @Test
    public void shouldLogWhenBundleIsMissingLibFolder() throws Exception {
        String message = "All jar files need to be placed inside a lib folder.";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithoutLibFolder")), warningMessages);
    }

    @Test
    public void shouldLogWhenBundleIsMissingFilesFromLibFolder() throws Exception {
        String message = "The jar file lib/test2.jar is not present in the lib folder";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithMissingFilesFromLibFolder")), warningMessages);
    }

    @Test
    public void shouldNotLogWhenThereAreNoExternalLibraries() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithoutExternalLibraries")), warningMessages);
    }

    @Test
    public void shouldLogWhenThereAreMissingFilesInManifest() throws Exception {
        String message = "The jar file lib/test2.jar is present in the lib folder but is not present in the MANIFEST.MF file";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithMissingFilesInManifest")), warningMessages);
    }

    @Test
    public void shouldNotLogWhenThereAreExcludedFiles() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithExcludedFiles")), warningMessages);
    }

    @Test
    public void shouldNotLogWhenThereAreSpacesInManifest() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithSpacesInManifest")), warningMessages);
    }

    @Test
    public void shouldNotLogWhenBundleHasVariousBundleClasspathEntries() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifest(getManifestFilePathFromFolder(addMetaInfToFilePath("bundleWithVariousBundleClasspathEntries")), warningMessages);
    }

    private String getManifestFilePathFromFolder(String folderName) throws IOException {
        return getPath(folderName + File.separator + TEST_FILE_NAME);
    }

    private String addMetaInfToFilePath(String filePath) throws IOException {
        return filePath + File.separator + TEST_FOLDER_NAME;
    }

    private void verifyManifest(String filePath, String[] warningMessages) throws Exception {
        verify(CONFIGURATION, filePath, warningMessages);
    }
}
