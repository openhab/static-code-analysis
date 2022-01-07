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
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.ExportInternalPackageCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link ExportInternalPackageCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Petar Valchev - Added the general verifyManifest() method
 */
public class ExportInternalPackageCheckTest extends AbstractStaticCheckTest {
    private static DefaultConfiguration config;

    @BeforeAll
    public static void setUpClass() {
        config = createModuleConfig(ExportInternalPackageCheck.class);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/exportInternalPackageCheckTest";
    }

    @Test
    public void testManifestFileExportsSingleInternalPackage() throws Exception {
        int lineNumber = 12;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Remove internal package export org.openhab.core.buildtools.internal");
        verifyManifest("singleInternalPackageExported.MF", expectedMessages);
    }

    @Test
    public void testManifestFileExportsMultipleInternalPackages() throws Exception {
        int lineNumber = 12;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Remove internal package export org.openhab.core.buildtools.internal", lineNumber,
                "Remove internal package export org.openhab.core.buildtools.internal.test");
        verifyManifest("multipleInternalPackagesExported.MF", expectedMessages);
    }

    @Test
    public void testEmptyFile() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber, "Manifest file is empty!");
        verifyManifest("emptyManifest.MF", expectedMessages);
    }

    @Test
    public void noInternalPackageExported() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyManifest("noInternalPackageExported.MF", expectedMessages);
    }

    @Test
    public void testManifestJustifiedText() throws Exception {
        int lineNumber = 13;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Remove internal package export org.openhab.core.buildtools.internal", lineNumber,
                "Remove internal package export org.openhab.core.buildtools.internal.test");
        verifyManifest("manifestJustifiedText.MF", expectedMessages);
    }

    private void verifyManifest(String fileName, String[] expectedMessages) throws Exception {
        String testFileAbsolutePath = getPath(fileName);
        verify(config, testFileAbsolutePath, expectedMessages);
    }
}
