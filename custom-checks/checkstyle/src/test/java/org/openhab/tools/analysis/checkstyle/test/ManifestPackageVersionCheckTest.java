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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_FILE_NAME;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ManifestPackageVersionCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

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
        config.addAttribute("ignoreExportedPackages", "org.openhab.core.tool.*");
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
                String.format(VERSION_USED_MSG, "org.openhab.core.buildtools.other"));
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
                13, String.format(VERSION_USED_MSG, "org.openhab.core.buildtools.other"),
                15, String.format(VERSION_USED_MSG, "org.openhab.core.build"),
                17, String.format(VERSION_USED_MSG, "org.openhab.core.ui"));
        verifyManifest("testMultipleExportsWithVersion", expectedMessages);
    }

    @Test
    public void testConfiguratedRegexPackages() throws Exception {
        // @formatter:off
        String[] expectedMessages = generateExpectedMessages(
                11,  String.format(VERSION_USED_MSG, "com.apache.commons.io"),
                13, String.format(VERSION_USED_MSG, "com.apache.commons"),
                15, String.format(VERSION_USED_MSG, "com.junit"),
                21, String.format(VERSION_USED_MSG, "org.openhab.core.checkstyle"));
        verifyManifest("testConfiguratedRegexPackages", expectedMessages);
    }

    @Test
    public void testValidManifest() throws Exception {
        verifyManifest("validManifest", CommonUtil.EMPTY_STRING_ARRAY);
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
