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
 * @author Mihaela Memova - Initial contribution
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
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.openhab.core.buildtools.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestFileThatDoesNotImportSeveralExportedPackages() throws Exception {
        String testFileName = "ManifestNotImportingSeveralExportedPackages.MF";
        int lineNumber = 13;

        String[] warningMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.openhab.core.buildtools.second.package"),
                lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.openhab.core.buildtools.third.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    @Test
    public void testManifestFileThatDoesNotImportAnyPackages() throws Exception {
        String testFileName = "ManifestNotImportingAnyPackages.MF";
        int lineNumber = 10;

        String[] warningMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.openhab.core.buildtools.package"));
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
                MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, "org.openhab.core.buildtools.package"));
        verifyManifestFile(testFileName, warningMessages);
    }

    public void verifyManifestFile(String fileName, String[] warningMessages) throws Exception {
        String filePath = getPath(fileName);
        verify(configuration, filePath, warningMessages);
    }
}
