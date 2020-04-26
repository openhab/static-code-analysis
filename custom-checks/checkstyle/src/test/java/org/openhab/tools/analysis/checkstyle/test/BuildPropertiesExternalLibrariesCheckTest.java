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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.BuildPropertiesExternalLibrariesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Test for the {@link BuildPropertiesExternalLibrariesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class BuildPropertiesExternalLibrariesCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(
            BuildPropertiesExternalLibrariesCheck.class);
    private static final String TEST_FILE_NAME = "build.properties";

    @Override
    protected String getPackageLocation() {
        return "checkstyle/buildPropertiesExternalLibrariesCheck";
    }

    @Test
    public void shouldNotLogWhenBundleIsValidFolder() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPathForDirectory("validBundle"));
    }

    @Test
    public void shouldNotLogWhenBundleIsValidEntries() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages,
                getBuildPropertiesPathForDirectory("validBundleWithJarFilesInBuildProperties"));
    }

    @Test
    public void shouldLogWhenThereAreMissingFilesInBuildProperties() throws Exception {
        String message = "The jar file lib/test2.jar is present in the lib folder but is not present in the build properties";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyNoWarningMessages(warningMessages,
                getBuildPropertiesPathForDirectory("bundleWithMissingJarFilesFromBuildProperties"));
    }

    @Test
    public void shouldNotLogWhenBundleDoesNotContainExternalDependancies() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPathForDirectory("emptyBundle"));
    }

    @Test
    public void shouldLogWhenThereAreMissingJarFilesFromLib() throws Exception {
        String message = "The file lib/test2.jar is present in the build properties but not in the lib folder.";
        String[] warningMessages = generateExpectedMessages(0, message);
        verifyNoWarningMessages(warningMessages,
                getBuildPropertiesPathForDirectory("bundleWithMissingFilesFromLibFolderWithBuildProperties"));
    }

    @Test
    public void shouldNotLogWhenThereAreExcludedJarFiles() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages, getBuildPropertiesPathForDirectory("bundleWithExcludedFiles"));
    }

    @Test
    public void shouldNotLogWhenThereAreSpacesInBuildProperties() throws Exception {
        String[] warningMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyNoWarningMessages(warningMessages,
                getBuildPropertiesPathForDirectory("bundleWithSpacesInBuildProperties"));
    }

    private void verifyNoWarningMessages(String[] warningMessages, String filePath) throws Exception {
        verify(CONFIGURATION, filePath, warningMessages);
    }

    private String getBuildPropertiesPathForDirectory(String directoryName) throws IOException {
        return getPath(directoryName + File.separator + TEST_FILE_NAME);
    }
}
