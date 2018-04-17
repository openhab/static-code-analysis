/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_FILE_NAME;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ManifestPackageVersionCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ManifestPackageVersionCheck}
 *
 * @author Petar Valchev - Initial contribution with tests for imported packages.
 * @author Aleksandar Kovachev - Added tests for exported packages.
 * @author Svlien Valkanov - Renamed the test class
 */
public class ManifestPackageVersionCheckTest extends AbstractStaticCheckTest {
    private static final String VERSION_USED_MSG = "The version of the package %s should not be specified";

    private static DefaultConfiguration config;

    @BeforeClass
    public static void createConfiguration() {
        config = createModuleConfig(ManifestPackageVersionCheck.class);
        config.addAttribute("ignoreImportedPackages", "org.apache.*, org.junit.*");
        config.addAttribute("ignoreExportedPackages", "org.eclipse.smarthome.tool.*");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/manifestPackageVersionCheckTest";
    }

    @Test
    public void testImportWithVersion() throws Exception {
        String[] expectedMessages = generateExpectedMessages(10, String.format(VERSION_USED_MSG, "javax.xml.bind"));
        verifyManifest("invalidImportsManifest", expectedMessages);
    }

    @Test
    public void testExportWithVersion() throws Exception {
        String[] expectedMessages = generateExpectedMessages(13,
                String.format(VERSION_USED_MSG, "org.eclipse.smarthome.buildtools.other"));
        verifyManifest("invalidExportsManifest", expectedMessages);
    }

    @Test
    public void testMultipleImportsWithVersion() throws Exception {
        // @formatter:off
        String[] expectedMessages = generateExpectedMessages(
                10, String.format(VERSION_USED_MSG, "javax.xml.bind"),
                13,String.format(VERSION_USED_MSG, "org.joda.time"),
                14, String.format(VERSION_USED_MSG, "org.osgi.framework"));
        verifyManifest("testMultipleImportsWithVersion", expectedMessages);
    }

    @Test
    public void testMultipleExportsWithVersion() throws Exception {
        // @formatter:off
        String[] expectedMessages = generateExpectedMessages(
                13, String.format(VERSION_USED_MSG, "org.eclipse.smarthome.buildtools.other"),
                15, String.format(VERSION_USED_MSG, "org.eclipse.smarthome.build"),
                17, String.format(VERSION_USED_MSG, "org.eclipse.smarthome.ui"));
        verifyManifest("testMultipleExportsWithVersion", expectedMessages);
    }

    @Test
    public void testConfiguratedRegexPackages() throws Exception {
        // @formatter:off
        String[] expectedMessages = generateExpectedMessages(
                11,  String.format(VERSION_USED_MSG, "com.apache.commons.io"),
                13, String.format(VERSION_USED_MSG, "com.apache.commons"),
                15, String.format(VERSION_USED_MSG, "com.junit"),
                21, String.format(VERSION_USED_MSG, "org.eclipse.smarthome.checkstyle"));
        verifyManifest("testConfiguratedRegexPackages", expectedMessages);
    }

    @Test
    public void testValidManifest() throws Exception {
        verifyManifest("validManifest", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testImportPackageAndRequireBundle() throws Exception {
        // @formatter:off
        String[] expectedMessages = generateExpectedMessages(
                19,  String.format(VERSION_USED_MSG, "org.osgi.framework"),
                20, String.format(VERSION_USED_MSG, "org.osgi.service.cm"),
                27, String.format(VERSION_USED_MSG, "org.openhab.io.transport.mqtt"));
        verifyManifest("testImportPackageAndRequireBundle", expectedMessages);
    }

    private void verifyManifest(String testFileDirectory, String[] expectedMessages) throws Exception {
        String filePath = getPath(testFileDirectory + File.separator + MANIFEST_FILE_NAME);

        verify(config, filePath, expectedMessages);
    }
}
