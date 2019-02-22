/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.text.MessageFormat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ImportExportedPackagesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link ImportExportedPackagesCheck}
 *
 * @author Mihaela Memova
 *
 */
public class ImportExportedPackagesCheckTest extends AbstractStaticCheckTest {
    private static final String NOT_IMPORTED_PACKAGE_MESSAGE = "The exported package `{0}` is not imported";

    private static DefaultConfiguration configuration;

    @BeforeClass
    public static void setUp() {
        configuration = createModuleConfig(ImportExportedPackagesCheck.class);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/importExportedPackagesCheckTest";
    }

    @Test
    public void testManifestFileThatDoesNotImportAnExportedPackage() throws Exception {
        String testFileName = "ManifestNotImportingAnExportedPackage.MF";
        int lineNumber = 12;
        String[] warningMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.eclipse.smarthome.buildtools.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestFileThatDoesNotImportSeveralExportedPackages() throws Exception {
        String testFileName = "ManifestNotImportingSeveralExportedPackages.MF";
        int lineNumber = 13;

        String[] warningMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.eclipse.smarthome.buildtools.second.package"),
                lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.eclipse.smarthome.buildtools.third.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestFileThatDoesNotImportAnyPackages() throws Exception {
        String testFileName = "ManifestNotImportingAnyPackages.MF";
        int lineNumber = 10;

        String[] warningMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.eclipse.smarthome.buildtools.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestThatImportAllExportedPackages() throws Exception {
        String testFileName = "ManifestImportingAllExportedPackages.MF";
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testJustifiedManifestFileThatDoesNotImportAnExportedPackage() throws Exception {
        String testFileName = "JustifiedManifestNotImportingAnExportedPackage.MF";
        int lineNumber = 12;
        String[] warningMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.eclipse.smarthome.buildtools.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    public void verifyManifestFile(String fileName, String[] warningMessages) throws Exception {
        String filePath = getPath(fileName);
        verify(configuration, filePath, warningMessages);
    }
}
