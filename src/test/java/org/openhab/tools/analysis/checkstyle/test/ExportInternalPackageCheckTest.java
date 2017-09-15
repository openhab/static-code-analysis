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
import org.openhab.tools.analysis.checkstyle.ExportInternalPackageCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ExportInternalPackageCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Petar Valchev - Added the general verifyManifest() method
 *
 */
public class ExportInternalPackageCheckTest extends AbstractStaticCheckTest {
    private static final String TEST_DIRECTORY_NAME = "exportInternalPackageCheckTest";

    private static DefaultConfiguration config;

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = createCheckConfig(TreeWalker.class);
        configParent.addChild(config);
        return configParent;
    }

    @BeforeClass
    public static void setUpClass() {
        config = createCheckConfig(ExportInternalPackageCheck.class);
    }

    @Test
    public void testManifestFileExportsSingleInternalPackage() throws Exception {
        int lineNumber = 12;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Remove internal package export org.eclipse.smarthome.buildtools.internal");
        verifyManifest("singleInternalPackageExported.MF", expectedMessages);
    }

    @Test
    public void testManifestFileExportsMultipleInternalPackages() throws Exception {
        int lineNumber = 12;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Remove internal package export org.eclipse.smarthome.buildtools.internal", lineNumber,
                "Remove internal package export org.eclipse.smarthome.buildtools.internal.test");
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
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyManifest("noInternalPackageExported.MF", expectedMessages);
    }

    @Test
    public void testManifestJustifiedText() throws Exception {
        int lineNumber = 13;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Remove internal package export org.eclipse.smarthome.buildtools.internal", lineNumber,
                "Remove internal package export org.eclipse.smarthome.buildtools.internal.test");
        verifyManifest("manifestJustifiedText.MF", expectedMessages);
    }

    private void verifyManifest(String fileName, String[] expectedMessages) throws Exception {
        String testFileRelativePath = TEST_DIRECTORY_NAME + File.separator + fileName;
        String testFileAbsolutePath = getPath(testFileRelativePath);
        verify(config, testFileAbsolutePath, expectedMessages);
    }
}
