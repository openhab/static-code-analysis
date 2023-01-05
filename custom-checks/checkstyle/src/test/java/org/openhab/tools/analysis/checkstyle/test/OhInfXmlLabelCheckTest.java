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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.OhInfXmlLabelCheck;
import org.openhab.tools.analysis.checkstyle.OhInfXmlValidationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Test for {@link OhInfXmlLabelCheck}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class OhInfXmlLabelCheckTest extends AbstractStaticCheckTest {

    private static final String RELATIVE_PATH_TO_THING = File.separator + OH_INF_PATH + File.separator
            + OhInfXmlValidationCheck.THING_DIRECTORY + File.separator + "thing-types.xml";
    private static final String RELATIVE_PATH_TO_CONFIG = File.separator + OH_INF_PATH + File.separator
            + OhInfXmlValidationCheck.CONFIGURATION_DIRECTORY + File.separator + "conf.xml";

    private DefaultConfiguration configuration;

    @Before
    public void before() {
        configuration = createModuleConfig(OhInfXmlLabelCheck.class);
        configuration.addProperty("checkWordCasing", "true");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/ohInfXmlLabelCheckTest";
    }

    @Test
    public void testInvalidParameterLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(13, MessageFormat
                .format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "parameter", "name", "check", "Sample parameter"));
        verifyWithPath("invalidParameterLabel", RELATIVE_PATH_TO_CONFIG, expectedMessages);
    }

    @Test
    public void testInvalidParameterGroupLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(11,
                MessageFormat.format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "parameter-group", "name", "check",
                        "Sample parameter group"));
        verifyWithPath("invalidParameterGroupLabel", RELATIVE_PATH_TO_CONFIG, expectedMessages);
    }

    @Test
    public void testInvalidChannelGroupLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(12, MessageFormat.format(
                OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "channel-group", "id", "check", "Sample channel Group"));
        verifyWithPath("invalidChannelGroupLabel", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testInvalidChannelGroupTypeLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(8,
                MessageFormat.format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "channel-group-type", "id", "check",
                        "Sample channel group Type"));
        verifyWithPath("invalidChannelGroupTypeLabel", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testInvalidChannelLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(12, MessageFormat
                .format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "channel", "id", "check", "Sample channel"));
        verifyWithPath("invalidChannelLabel", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testInvalidChannelTypeLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(8, MessageFormat.format(
                OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "channel-type", "id", "check", "Sample channel Type"));
        verifyWithPath("invalidChannelTypeLabel", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testInvalidThingTypeLabel() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(36,
                MessageFormat.format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "thing-type", "id", "check1",
                        "Sample thing"),
                40,
                MessageFormat.format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "thing-type", "id", "check2",
                        "Sample in"),
                44, MessageFormat.format(OhInfXmlLabelCheck.MESSAGE_LABEL_UPPERCASE, "parameter", "name", "check3",
                        "Sample Parameter Cfg in"));
        verifyWithPath("invalidThingTypeLabel", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    @Test
    public void testExceedsMaxLabelLength() throws Exception {
        final String[] expectedMessages = generateExpectedMessages(16,
                MessageFormat.format(String.format(OhInfXmlLabelCheck.MESSAGE_MAX_LABEL_LENGTH, 25), "thing-type", "id",
                        "check", "L" + "o".repeat(21) + "ng Label", 30));
        configuration.addProperty("maxLabelLength", "25");
        verifyWithPath("exceedsMaxLabelLength", RELATIVE_PATH_TO_THING, expectedMessages);
    }

    private void verifyWithPath(final String testSubDirectory, final String testFilePath,
            final String[] expectedMessages) throws Exception {
        final String directoryPath = getPath(testSubDirectory);
        final File testDirectoryPath = new File(directoryPath);
        final File[] testFiles = listFilesForFolder(testDirectoryPath, new ArrayList<File>());
        verify(createChecker(configuration), testFiles, directoryPath + testFilePath, Stream.of(expectedMessages)
                .map(s -> s.replace("''", "'")).collect(Collectors.toList()).toArray(new String[0]));
    }

    private File[] listFilesForFolder(final File folder, final ArrayList<File> files) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files);
            } else {
                files.add(fileEntry);
            }
        }
        return files.toArray(new File[] {});
    }
}
