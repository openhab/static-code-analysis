/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.ESH_INF_DIRECTORY;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.EshInfXmlUsageCheck;
import org.openhab.tools.analysis.checkstyle.EshInfXmlValidationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Test for {@link EshInfXmlValidationCheck}
 *
 * @author Aleksandar Kovachev - Initial implementation
 * @author Svilen Valkanov - Added new test cases, message constants and done some refactoring
 *
 */
public class EshInfXmlUsageCheckTest extends AbstractStaticCheckTest {

    private static final String TEST_CHECK_DIRECTORY = "eshInfXmlUsageCheckTest" + File.separator;

    private static final String RELATIVE_PATH_TO_THING = File.separator + ESH_INF_DIRECTORY + File.separator
            + EshInfXmlValidationCheck.THING_DIRECTORY + File.separator + "thing-types.xml";
    private static final String RELATIVE_PATH_TO_BINDING = File.separator + ESH_INF_DIRECTORY + File.separator
            + EshInfXmlValidationCheck.BINDING_DIRECTORY + File.separator + "bind.xml";
    private static final String RELATIVE_PATH_TO_CONFIG = File.separator + ESH_INF_DIRECTORY + File.separator
            + EshInfXmlValidationCheck.CONFIGURATION_DIRECTORY + File.separator + "conf.xml";

    private static final String MESSAGE_MISSING_URI_CONFIGURATION = "Missing configuration for the configuration reference with uri - {0}";
    private static final String MESSAGE_MISSING_SUPPORTED_BRIDGE = "Missing the supported bridge with id {0}";
    private static final String MESSAGE_UNUSED_URI_CONFIGURATION = "Unused configuration reference with uri - {0}";
    private static final String MESSAGE_UNUSED_BRIDGE = "Unused bridge reference with id - {0}";

    private static final DefaultConfiguration CONFIGURATION = createCheckConfig(EshInfXmlUsageCheck.class);

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = new DefaultConfiguration("root");
        configParent.addChild(config);
        return configParent;
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
        String directoryPath = getPath(TEST_CHECK_DIRECTORY + testSubDirectory);
        File testDirectoryPath = new File(directoryPath);

        File[] testFiles = listFilesForFolder(testDirectoryPath, new ArrayList<File>());
        verify(createChecker(CONFIGURATION), testFiles, directoryPath + testFilePath, expectedMessages);
    }

    private File[] listFilesForFolder(File folder, ArrayList<File> files) {
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
