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

import static com.puppycrawl.tools.checkstyle.utils.CommonUtil.EMPTY_STRING_ARRAY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.OnlyTabIndentationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Checks whether whitespace characters are use instead of tabs in xml files
 * indentations and generates warnings in such cases.
 *
 * @author Lyubomir Papazov - Initial contribution
 * @author Kristina Simova - Added tests
 */
public class OnlyTabIndentationCheckTest extends AbstractStaticCheckTest {

    private static final String WHITESPACE_USAGE_WARNING = "There were whitespace characters used for indentation. Please use tab characters instead";
    private DefaultConfiguration config;

    @BeforeEach
    public void setUpClass() {
        config = createModuleConfig(OnlyTabIndentationCheck.class);
        config.addProperty("fileTypes", "xml,json");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/onlyTabIndentationInXmlFilesCheck";
    }

    @Test
    public void testOneLineXmlFile() throws Exception {
        verifyXmlTabIdentation("WhiteSpacesNotUsedBeforeOpeningTags.xml", noMessagesExpected());
    }

    @Test
    public void testValidJson() throws Exception {
        verifyTabIdentation("validJson.json", noMessagesExpected(), false);
    }

    @Test
    public void testBadlyFormattedJson() throws Exception {
        String[] expectedMessages = generateExpectedMessages(7, WHITESPACE_USAGE_WARNING);
        verifyTabIdentation("badlyFormattedJson.json", expectedMessages, false);
    }

    @Test
    public void testOneIncorrectLine() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInOneLine.xml", expectedMessages);
    }

    @Test
    public void testManyIncorrectLinesOnlyShowFirstWarning() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInManyLines.xml", expectedMessages);
    }

    @Test
    public void testManyIncorrectLinesShowAllWarnings() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING, 6, WHITESPACE_USAGE_WARNING);
        verifyTabIdentation("WhiteSpaceUsedBeforeOpeningTagInManyLines.xml", expectedMessages, false);
    }

    @Test
    public void testXmlWithEmptyLinesAndComments() throws Exception {
        String fileName = "BasicModuleHandlerFactory.xml";
        verifyTabIdentation(fileName, noMessagesExpected(), false);
    }

    @Test
    public void testXmlWithMultipleLinesWithSpacesEmptyLinesAndComments() throws Exception {
        String fileName = "ScriptEngineManager.xml";
        String[] expectedMessages = generateExpectedMessages(3, WHITESPACE_USAGE_WARNING, 4, WHITESPACE_USAGE_WARNING,
                6, WHITESPACE_USAGE_WARNING, 7, WHITESPACE_USAGE_WARNING, 9, WHITESPACE_USAGE_WARNING, 10,
                WHITESPACE_USAGE_WARNING, 11, WHITESPACE_USAGE_WARNING, 13, WHITESPACE_USAGE_WARNING, 17,
                WHITESPACE_USAGE_WARNING, 18, WHITESPACE_USAGE_WARNING, 19, WHITESPACE_USAGE_WARNING, 20,
                WHITESPACE_USAGE_WARNING, 21, WHITESPACE_USAGE_WARNING, 22, WHITESPACE_USAGE_WARNING);
        verifyTabIdentation(fileName, expectedMessages, false);
    }

    @Test
    public void testXmlWithOnlyTabsForIndentation() throws Exception {
        String fileName = "addon.xml";
        verifyTabIdentation(fileName, noMessagesExpected(), false);
    }

    @Test
    public void testAnotherXmlWithOnlyTabsForIndentation() throws Exception {
        String fileName = "thing-types.xml";
        verifyTabIdentation(fileName, noMessagesExpected(), false);
    }

    @Test
    public void testXmlWithCDATAAndSpacesForIndentation() throws Exception {
        String fileName = "i18n.xml";
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING, 11, WHITESPACE_USAGE_WARNING,
                12, WHITESPACE_USAGE_WARNING, 13, WHITESPACE_USAGE_WARNING, 14, WHITESPACE_USAGE_WARNING, 20,
                WHITESPACE_USAGE_WARNING, 21, WHITESPACE_USAGE_WARNING, 22, WHITESPACE_USAGE_WARNING, 23,
                WHITESPACE_USAGE_WARNING, 30, WHITESPACE_USAGE_WARNING, 31, WHITESPACE_USAGE_WARNING, 32,
                WHITESPACE_USAGE_WARNING, 33, WHITESPACE_USAGE_WARNING, 39, WHITESPACE_USAGE_WARNING, 40,
                WHITESPACE_USAGE_WARNING, 41, WHITESPACE_USAGE_WARNING, 42, WHITESPACE_USAGE_WARNING, 59,
                WHITESPACE_USAGE_WARNING, 60, WHITESPACE_USAGE_WARNING, 61, WHITESPACE_USAGE_WARNING);
        verifyTabIdentation(fileName, expectedMessages, false);
    }

    @Test
    public void testAnotherXmlWithCDATAAndSpacesForIndentation() throws Exception {
        String fileName = "bridge.xml";
        String[] expectedMessages = generateExpectedMessages(10, WHITESPACE_USAGE_WARNING, 11, WHITESPACE_USAGE_WARNING,
                12, WHITESPACE_USAGE_WARNING, 13, WHITESPACE_USAGE_WARNING, 15, WHITESPACE_USAGE_WARNING, 16,
                WHITESPACE_USAGE_WARNING, 17, WHITESPACE_USAGE_WARNING, 18, WHITESPACE_USAGE_WARNING, 19,
                WHITESPACE_USAGE_WARNING, 20, WHITESPACE_USAGE_WARNING, 22, WHITESPACE_USAGE_WARNING);
        verifyTabIdentation(fileName, expectedMessages, false);
    }

    private String[] noMessagesExpected() {
        String[] expectedMessages = EMPTY_STRING_ARRAY;
        return expectedMessages;
    }

    private void verifyTabIdentation(String fileName, String[] expectedMessages, boolean onlyShowFirstWarning)
            throws Exception {
        String testFileAbsolutePath = getPath(fileName);
        String messageFilePath = testFileAbsolutePath;
        if (onlyShowFirstWarning) {
            config.addProperty("onlyShowFirstWarning", "true");
        } else {
            config.addProperty("onlyShowFirstWarning", "false");
        }
        verify(createChecker(config), testFileAbsolutePath, messageFilePath, expectedMessages);
    }

    private void verifyXmlTabIdentation(String fileName, String[] expectedMessages) throws Exception {
        verifyTabIdentation(fileName, expectedMessages, true);
    }
}
