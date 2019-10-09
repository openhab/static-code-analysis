/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.ESH_INF_PATH;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.EshInfXmlValidationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.utils.CachingHttpClient;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Test for {@link EshInfXmlValidationCheck}
 *
 * @author Aleksandar Kovachev - Initial implementation
 * @author Svilen Valkanov - Added new test cases, message constants and done some refactoring
 *
 */
public class EshInfXmlValidationCheckTest extends AbstractStaticCheckTest {

    private static final String RELATIVE_PATH_TO_THING = File.separator + ESH_INF_PATH + File.separator
            + EshInfXmlValidationCheck.THING_DIRECTORY + File.separator + "thing-types.xml";
    private static final String RELATIVE_PATH_TO_BINDING = File.separator + ESH_INF_PATH + File.separator
            + EshInfXmlValidationCheck.BINDING_DIRECTORY + File.separator + "bind.xml";
    private static final String RELATIVE_PATH_TO_CONFIG = File.separator + ESH_INF_PATH + File.separator
            + EshInfXmlValidationCheck.CONFIGURATION_DIRECTORY + File.separator + "conf.xml";

    private static final String SCHEMA_ROOT_URL = "https://openhab.org/schemas/";
    private static final String THING_SCHEMA_URL = SCHEMA_ROOT_URL + "thing-description-1.0.0.xsd";
    private static final String BINDING_SCHEMA_URL = SCHEMA_ROOT_URL + "binding-1.0.0.xsd";
    private static final String CONFIG_SCHEMA_URL = SCHEMA_ROOT_URL + "config-description-1.0.0.xsd";

    private static final String MESSAGE_EMPTY_FILE = "The file {0} should not be empty.";
    private static final String MESSAGE_NOT_INCLUDED_XML_FILE = "The file {0} isn't included in the build.properties file. Good approach is to include all files by adding `ESH-INF/` value to the bin.includes property.";

    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(EshInfXmlValidationCheck.class);

    @BeforeClass
    public static void createConfiguration() {
        CONFIGURATION.addAttribute("thingSchema", THING_SCHEMA_URL);
        CONFIGURATION.addAttribute("bindingSchema", BINDING_SCHEMA_URL);
        CONFIGURATION.addAttribute("configSchema", CONFIG_SCHEMA_URL);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/eshInfXmlValidationCheckTest";
    }

    private boolean isResourceAvailable;

    @Before
    public void checkConnection() {
        Locale.setDefault(new Locale("en", "US"));
        try {
            URL url = new URL(THING_SCHEMA_URL);
            CachingHttpClient<String> cachingClient = new CachingHttpClient<>(c -> new String(c));
            isResourceAvailable = cachingClient.get(url) != null;
        } catch (IOException e) {
            isResourceAvailable = false;
        }
    }

    @Test
    public void testValidSupportedBridgeRef() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithPath("validSupportedBridgeRef", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testMissingChannelTypeContent() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 16;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "The content of element channel-type is not complete. One of {item-type, kind, label} is expected.");
        verifyWithPath("missingChannelTypeContent", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testSequenceThingTypesCheck() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 8;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Invalid content was found starting with element description. One of {supported-bridge-type-refs, label} is expected.");
        verifyWithPath("sequenceThingTypesCheck", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testValidThingTypeXml() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithPath("validThingTypeXml", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testMissingPropertyContent() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 14;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "The content of element properties is not complete. One of {property} is expected.");
        verifyWithPath("missingPropertyContent", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testSequenceChannelTypeCheck() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 18;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Invalid content was found starting with element item-type. One of {category, tags, state, command, event, autoUpdatePolicy, config-description, config-description-ref} is expected.");
        verifyWithPath("sequenceChannelTypeCheck", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testSequenceBridgeTypeCheck() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 7;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Invalid content was found starting with element description. One of {supported-bridge-type-refs, label} is expected.");
        verifyWithPath("sequenceBridgeTypeCheck", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testWrongDirectory() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithPath("wrongDirectory", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testValidBridgeType() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyWithPath("validBridgeType", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testInvalidBinding() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 7;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "The content of element binding:binding is not complete. One of {name} is expected.");
        verifyWithPath("invalidBinding", RELATIVE_PATH_TO_BINDING, expectedMessages);
    }

    @Test
    public void testInvalidConfig() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 8;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "The content of element config-description:config-descriptions is not complete. One of {config-description} is expected.");
        verifyWithPath("invalidConfig", RELATIVE_PATH_TO_CONFIG, expectedMessages);
    }

    @Test
    public void testMissingThingDescriptionsContent() throws Exception {
        Assume.assumeTrue(isResourceAvailable);

        int lineNumber = 6;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "The content of element thing:thing-descriptions is not complete. One of {thing-type, bridge-type, channel-type, channel-group-type} is expected.");
        verifyWithPath("missingThingDescriptionsContent", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testEmptyThingTypeXml() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(MESSAGE_EMPTY_FILE, "thing-types.xml"));
        verifyWithPath("emptyThingTypeXml", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testMissingXmlFileInBuildProperties() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(MESSAGE_NOT_INCLUDED_XML_FILE,
                        "ESH-INF" + File.separator + "thing" + File.separator + "thing-types.xml"));
        verifyWithPath("missingXmlFileInBuildProperties", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    private void verifyWithPath(String testSubDirectory, String testFilePath, String[] expectedMessages)
            throws Exception {
        String directoryPath = getPath(testSubDirectory);
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
