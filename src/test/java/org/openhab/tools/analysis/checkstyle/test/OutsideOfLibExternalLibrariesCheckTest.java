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
import java.io.IOException;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.OutsideOfLibExternalLibrariesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link OutsideOfLibExternalLibrariesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class OutsideOfLibExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CONFIGURATION = createCheckConfig(
            OutsideOfLibExternalLibrariesCheck.class);
    private static String TEST_FILE_NAME = "build.properties";
    private static final String MAIN_DIRECTORY = "outsideOfLibExternalLibrariesCheck";

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = createCheckConfig(TreeWalker.class);
        configParent.addChild(config);

        return configParent;
    }

    @Test
    public void shouldNotLogBundleIsValid() throws Exception {
        final String VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator + "validBundle";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyBuildProperties(getBuildPropertiesPath(VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES), warningMessages);
    }

    @Test
    public void shouldLogWhenThereAreJarFilesOutsideOfLibFolder() throws Exception {
        final String BUNDLE_WITH_JAR_FILES_OUTSIDE_OF_LIB_FOLDER = MAIN_DIRECTORY + File.separator
                + "bundleWithJarFilesOutsideOfLib";
        String message = "All jar files need to be placed inside a lib folder and added to MANIFEST.MF and build.properties";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyBuildProperties(getBuildPropertiesPath(BUNDLE_WITH_JAR_FILES_OUTSIDE_OF_LIB_FOLDER), warningMessages);
    }

    private void verifyBuildProperties(String filePath, String[] warningMessages) throws Exception {
        verify(CONFIGURATION, filePath, warningMessages);
    }

    private String getBuildPropertiesPath(String bundlePath) throws IOException {
        return getPath(bundlePath + File.separator + TEST_FILE_NAME);
    }
}