/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.OH_INF_PATH;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.OhInfXmlUsageCheck;
import org.openhab.tools.analysis.checkstyle.OhInfXmlValidationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Test for {@link OhInfXmlValidationCheck}
 *
 * @author Aleksandar Kovachev - Initial contribution
 * @author Svilen Valkanov - Added new test cases, message constants and done some refactoring
 */
public class OhInfXmlUsageCheckTest extends AbstractStaticCheckTest {

    private static final String RELATIVE_PATH_TO_THING = File.separator + OH_INF_PATH + File.separator
            + OhInfXmlValidationCheck.THING_DIRECTORY + File.separator + "thing-types.xml";
    private static final String RELATIVE_PATH_TO_CONFIG = File.separator + OH_INF_PATH + File.separator
            + OhInfXmlValidationCheck.CONFIGURATION_DIRECTORY + File.separator + "conf.xml";

    private static final String MESSAGE_MISSING_URI_CONFIGURATION = "Missing configuration for the configuration reference with uri - {0}";
    private static final String MESSAGE_MISSING_SUPPORTED_BRIDGE = "Missing the supported bridge with id {0}";
    private static final String MESSAGE_UNUSED_URI_CONFIGURATION = "Unused configuration reference with uri - {0}";
    private static final String MESSAGE_UNUSED_BRIDGE = "Unused bridge reference with id - {0}";

    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(OhInfXmlUsageCheck.class);

    @Override
    protected String getPackageLocation() {
        return "checkstyle/ohInfXmlUsageCheckTest";
    }

    @Test
    public void testConfigurableService() throws Exception {
        verifyWithPath("configurableService", RELATIVE_PATH_TO_THING, new String[0]);
    }

    @Test
    public void testMissingSupportedBridgeRef() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0,
                MessageFormat.format(MESSAGE_MISSING_SUPPORTED_BRIDGE, "bridge"));
        verifyWithPath("missingSupportedBridgeRef", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testMissingConfigRef() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0,
                MessageFormat.format(MESSAGE_MISSING_URI_CONFIGURATION, "binding:ge:config"));
        verifyWithPath("missingConfigDesciptionRef", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testProfileConfigDescription() throws Exception {
        verifyWithPath("profileConfigDescription", RELATIVE_PATH_TO_THING, new String[0]);
    }

    @Test
    public void testUnusedConfig() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0,
                MessageFormat.format(MESSAGE_UNUSED_URI_CONFIGURATION, "thing-type:bindingID:thing"));
        verifyWithPath("unusedConfigDescription", RELATIVE_PATH_TO_CONFIG, expectedMessages);
    }

    @Test
    public void testUnusedBridge() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0, MessageFormat.format(MESSAGE_UNUSED_BRIDGE, "bridge"));
        verifyWithPath("unusedBridge", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    private void verifyWithPath(String testSubDirectory, String testFilePath, String[] expectedMessages)
            throws Exception {
        String directoryPath = getPath(testSubDirectory);
        File testDirectoryPath = new File(directoryPath);

        File[] testFiles = listFilesForFolder(testDirectoryPath, new ArrayList<>());
        verify(createChecker(CONFIGURATION), testFiles, directoryPath + testFilePath, expectedMessages);
    }

    private File[] listFilesForFolder(File folder, List<File> files) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files);
            } else {
                files.add(fileEntry);
            }
        }
        return files.toArray(new File[] {});
    }
}
