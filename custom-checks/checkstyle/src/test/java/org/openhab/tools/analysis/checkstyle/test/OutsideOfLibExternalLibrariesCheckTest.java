/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.OutsideOfLibExternalLibrariesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link OutsideOfLibExternalLibrariesCheck}
 *
 * @author Velin Yordanov - Initial contribution
 */
public class OutsideOfLibExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static final String TEST_FILE_NAME = "build.properties";
    private static DefaultConfiguration config;

    @Override
    protected String getPackageLocation() {
        return "checkstyle/outsideOfLibExternalLibrariesCheck";
    }

    @BeforeAll
    public static void setUpClass() {
        config = createModuleConfig(OutsideOfLibExternalLibrariesCheck.class);

        String ignoredDirectoriesValue = "bin,target";
        config.addProperty("ignoredDirectories", ignoredDirectoriesValue);
    }

    @Test
    public void shouldNotLogBundleIsValid() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyBuildProperties(getBuildPropertiesPath("validBundle"), warningMessages);
    }

    @Test
    public void shouldLogWhenThereAreJarFilesOutsideOfLibFolder() throws Exception {
        final String jarPath = getPath("bundleWithJarFilesOutsideOfLib");
        String message = "There is a jar outside of the lib folder %s" + File.separator + "%s";

        String[] warningMessages = generateExpectedMessages(0, String.format(message, jarPath, "test3.jar"));
        verifyBuildProperties(getBuildPropertiesPath("bundleWithJarFilesOutsideOfLib"), warningMessages);
    }

    private void verifyBuildProperties(String filePath, String[] warningMessages) throws Exception {
        verify(config, filePath, warningMessages);
    }

    private String getBuildPropertiesPath(String bundlePath) throws IOException {
        return getPath(bundlePath + File.separator + TEST_FILE_NAME);
    }
}
