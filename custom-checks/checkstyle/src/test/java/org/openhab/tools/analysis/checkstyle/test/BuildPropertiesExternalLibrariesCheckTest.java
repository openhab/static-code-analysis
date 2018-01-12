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

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.BuildPropertiesExternalLibrariesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Test for the {@link BuildPropertiesExternalLibrariesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class BuildPropertiesExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CONFIGURATION = createCheckConfig(
            BuildPropertiesExternalLibrariesCheck.class);
    private static final String TEST_FILE_NAME = "build.properties";
    private static final String MAIN_DIRECTORY = "buildPropertiesExternalLibrariesCheck";

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = createCheckConfig(TreeWalker.class);
        configParent.addChild(config);
        return configParent;
    }

    @Test
    public void shouldNotLogWhenBundleIsValidFolder() throws Exception {
        final String VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator + "validBundle";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages,
                getBuildPropertiesPath(VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES));
    }

    @Test
    public void shouldNotLogWhenBundleIsValidEntries() throws Exception {
        final String VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator
                + "validBundleWithJarFilesInBuildProperties";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages,
                getBuildPropertiesPath(VALID_BUNDLE_WITH_JAR_FILES_IN_BUILD_PROPERTIES));
    }

    @Test
    public void shouldLogWhenThereAreMissingFilesInBuildProperties() throws Exception {
        final String BUNDLE_WITH_MISSING_FILES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator
                + "bundleWithMissingJarFilesFromBuildProperties";
        String message = "The jar file lib/test2.jar is present in the lib folder but is not present in the build properties";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPath(BUNDLE_WITH_MISSING_FILES_IN_BUILD_PROPERTIES));
    }

    @Test
    public void shouldNotLogWhenBundleDoesNotContainExternalDependancies() throws Exception {
        final String BUNDLE_WITHOUT_EXTERNAL_DEPENDANCIES = MAIN_DIRECTORY + File.separator + "emptyBundle";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPath(BUNDLE_WITHOUT_EXTERNAL_DEPENDANCIES));
    }

    @Test
    public void shouldLogWhenThereAreMissingJarFilesFromLib() throws Exception {
        final String BUNDLE_WITH_MISSING_JAR_FILES_FROM_LIB = MAIN_DIRECTORY + File.separator
                + "bundleWithMissingFilesFromLibFolderWithBuildProperties";
        String message = "The file lib/test2.jar is present in the build properties but not in the lib folder.";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPath(BUNDLE_WITH_MISSING_JAR_FILES_FROM_LIB));
    }

    @Test
    public void shouldNotLogWhenThereAreExcludedJarFiles() throws Exception {
        final String BUNDLE_WITH_EXCLUDED_JARS = MAIN_DIRECTORY + File.separator + "bundleWithExcludedFiles";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPath(BUNDLE_WITH_EXCLUDED_JARS));
    }

    @Test
    public void shouldNotLogWhenThereAreSpacesInBuildProperties() throws Exception {
        final String BUNDLE_WITH_SPACES_IN_BUILD_PROPERTIES = MAIN_DIRECTORY + File.separator
                + "bundleWithSpacesInBuildProperties";
        String[] warningMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPath(BUNDLE_WITH_SPACES_IN_BUILD_PROPERTIES));
    }

    private void verifyNoWarningMessages(String[] warningMessages, String filePath) throws Exception {
        verify(CONFIGURATION, filePath, warningMessages);
    }

    private String getBuildPropertiesPath(String bundlePath) throws IOException {
        return getPath(bundlePath + File.separator + TEST_FILE_NAME);
    }
}
