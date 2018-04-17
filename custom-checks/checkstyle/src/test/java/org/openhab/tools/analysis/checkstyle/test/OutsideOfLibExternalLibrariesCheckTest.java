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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.OutsideOfLibExternalLibrariesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link OutsideOfLibExternalLibrariesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class OutsideOfLibExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static String TEST_FILE_NAME = "build.properties";
    private static DefaultConfiguration config;
    


    @Override
    protected String getPackageLocation() {
        return "checkstyle/outsideOfLibExternalLibrariesCheck";
    }

    @BeforeClass
    public static void setUpClass() {
        config = createModuleConfig(OutsideOfLibExternalLibrariesCheck.class);

        String ignoredDirectoriesValue = "bin,target";
        config.addAttribute("ignoredDirectories", ignoredDirectoriesValue);
    }

    @Test
    public void shouldNotLogBundleIsValid() throws Exception {
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyBuildProperties(getBuildPropertiesPath("validBundle"), warningMessages);
    }

    @Test
    public void shouldLogWhenThereAreJarFilesOutsideOfLibFolder() throws Exception {
        final String JAR_PATH = getPath("bundleWithJarFilesOutsideOfLib");
        String message = "There is a jar outside of the lib folder %s" + File.separator + "%s";

        String[] warningMessages = generateExpectedMessages(0, String.format(message, JAR_PATH, "test3.jar"));
        verifyBuildProperties(getBuildPropertiesPath("bundleWithJarFilesOutsideOfLib"), warningMessages);
    }

    private void verifyBuildProperties(String filePath, String[] warningMessages) throws Exception {
        verify(config, filePath, warningMessages);
    }

    private String getBuildPropertiesPath(String bundlePath) throws IOException {
        return getPath(bundlePath + File.separator + TEST_FILE_NAME);
    }
}
