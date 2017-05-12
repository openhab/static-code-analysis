/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ImportExportedPackagesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ImportExportedPackagesCheck}
 *
 * @author Mihaela Memova
 *
 */
public class ImportExportedPackagesCheckTest extends AbstractStaticCheckTest {
    private static final String TEST_DIRECTORY_NAME = "importExportedPackagesCheckTest";
    private static final String NOT_IMPORTED_PACKAGE_MESSAGE = "The exported package is not imported";

    private static DefaultConfiguration configuration;

    @BeforeClass
    public static void setUp() {
        configuration = createCheckConfig(ImportExportedPackagesCheck.class);
    }

    @Test
    public void testManifestFileThatDoesNotImportAnExportedPackage() throws Exception {
        String testFileName = "ManifestNotImportingAnExportedPackage.MF";
        int lineNumber = 12;
        String[] warningMessages = generateExpectedMessages(lineNumber, NOT_IMPORTED_PACKAGE_MESSAGE);
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestFileThatDoesNotImportSeveralExportedPackages() throws Exception {
        String testFileName = "ManifestNotImportingSeveralExportedPackages.MF";
        int firstLineNumber = 14;
        int secondLineNumber = 15;

        String[] warningMessages = generateExpectedMessages(firstLineNumber, NOT_IMPORTED_PACKAGE_MESSAGE,
                secondLineNumber, NOT_IMPORTED_PACKAGE_MESSAGE);
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestFileThatDoesNotImportAnyPackages() throws Exception {
        String testFileName = "ManifestNotImportingAnyPackages.MF";
        int lineNumber = 10;

        String[] warningMessages = generateExpectedMessages(lineNumber, NOT_IMPORTED_PACKAGE_MESSAGE);
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestThatImportAllExportedPackages() throws Exception {
        String testFileName = "ManifestImportingAllExportedPackages.MF";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifestFile(testFileName, warningMessages);
    }

    public void verifyManifestFile(String fileName, String[] warningMessages) throws Exception {
        String filePath = getPath(TEST_DIRECTORY_NAME + File.separator + fileName);
        verify(configuration, filePath, warningMessages);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration dc = new DefaultConfiguration("root");
        dc.addChild(config);
        return dc;
    }
}
