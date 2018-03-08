/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static com.puppycrawl.tools.checkstyle.utils.CommonUtils.EMPTY_STRING_ARRAY;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.OnlyTabIndentationInXmlFilesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.checkstyle.api.CheckConstants;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Checks whether whitespace characters are use instead of tabs in xml files
 * indentations and generates warnings in such cases.
 *
 * @author Lyubomir Papazov - initial contribution
 * @author Kristina Simova - Added tests
 */
public class OnlyTabIndentationInXmlFilesCheckTest extends AbstractStaticCheckTest {

    private static final String WHITESPACE_USAGE_WARNING = "There were whitespace characters used for indentation. Please use tab characters instead";
    private static final String TABIDENT_CHECK_TEST_DIRECTORY_NAME = "onlyTabIndentationInXmlFilesCheck";
    private DefaultConfiguration config;

    @Before
    public void setUpClass() {
        config = createCheckConfig(OnlyTabIndentationInXmlFilesCheck.class);
    }

    @Test
    public void testOneLineXmlFile() throws Exception {
        verifyXmlTabIdentation("WhiteSpacesNotUsedBeforeOpeningTags", noMessagesExpected());
    }

    @Test
    public void testOneIncorrectLine() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInOneLine", expectedMessages);
    }

    @Test
    public void testManyIncorrectLinesOnlyShowFirstWarning() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInManyLines", expectedMessages);
    }

    @Test
    public void testManyIncorrectLinesShowAllWarnings() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING, 6, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInManyLines", expectedMessages, false);
    }

    @Test
    public void testXmlWithEmptyLinesAndComments() throws Exception {
        String fileName = "BasicModuleHandlerFactory";
        verifyXmlTabIdentation(fileName, noMessagesExpected(), false);
    }

    @Test
    public void testXmlWithMultipleLinesWithSpacesEmptyLinesAndComments() throws Exception {
        String fileName = "ScriptEngineManager";
        String[] expectedMessages = generateExpectedMessages(3, WHITESPACE_USAGE_WARNING, 4, WHITESPACE_USAGE_WARNING,
                6, WHITESPACE_USAGE_WARNING, 7, WHITESPACE_USAGE_WARNING, 9, WHITESPACE_USAGE_WARNING, 10,
                WHITESPACE_USAGE_WARNING, 11, WHITESPACE_USAGE_WARNING, 13, WHITESPACE_USAGE_WARNING, 17,
                WHITESPACE_USAGE_WARNING, 18, WHITESPACE_USAGE_WARNING, 19, WHITESPACE_USAGE_WARNING, 20,
                WHITESPACE_USAGE_WARNING, 21, WHITESPACE_USAGE_WARNING, 22, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation(fileName, expectedMessages, false);
    }

    @Test
    public void testXmlWithOnlyTabsForIndentation() throws Exception {
        String fileName = "binding";
        verifyXmlTabIdentation(fileName, noMessagesExpected(), false);
    }

    @Test
    public void testAnotherXmlWithOnlyTabsForIndentation() throws Exception {
        String fileName = "thing-types";
        verifyXmlTabIdentation(fileName, noMessagesExpected(), false);
    }

    @Test
    public void testXmlWithCDATAAndSpacesForIndentation() throws Exception {
        String fileName = "i18n";
        String[] expectedMessages = generateExpectedMessages(5, WHITESPACE_USAGE_WARNING, 11, WHITESPACE_USAGE_WARNING,
                12, WHITESPACE_USAGE_WARNING, 13, WHITESPACE_USAGE_WARNING, 14, WHITESPACE_USAGE_WARNING, 20,
                WHITESPACE_USAGE_WARNING, 21, WHITESPACE_USAGE_WARNING, 22, WHITESPACE_USAGE_WARNING, 23,
                WHITESPACE_USAGE_WARNING, 30, WHITESPACE_USAGE_WARNING, 31, WHITESPACE_USAGE_WARNING, 32,
                WHITESPACE_USAGE_WARNING, 33, WHITESPACE_USAGE_WARNING, 39, WHITESPACE_USAGE_WARNING, 40,
                WHITESPACE_USAGE_WARNING, 41, WHITESPACE_USAGE_WARNING, 42, WHITESPACE_USAGE_WARNING, 59,
                WHITESPACE_USAGE_WARNING, 60, WHITESPACE_USAGE_WARNING, 61, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation(fileName, expectedMessages, false);
    }

    @Test
    public void testAnotherXmlWithCDATAAndSpacesForIndentation() throws Exception {
        String fileName = "bridge";
        String[] expectedMessages = generateExpectedMessages(10, WHITESPACE_USAGE_WARNING, 11, WHITESPACE_USAGE_WARNING,
                12, WHITESPACE_USAGE_WARNING, 13, WHITESPACE_USAGE_WARNING, 15, WHITESPACE_USAGE_WARNING, 16,
                WHITESPACE_USAGE_WARNING, 17, WHITESPACE_USAGE_WARNING, 18, WHITESPACE_USAGE_WARNING, 19,
                WHITESPACE_USAGE_WARNING, 20, WHITESPACE_USAGE_WARNING, 22, WHITESPACE_USAGE_WARNING);
        verifyXmlTabIdentation(fileName, expectedMessages, false);
    }

    private String[] noMessagesExpected() {
        String[] expectedMessages = EMPTY_STRING_ARRAY;
        return expectedMessages;
    }

    private void verifyXmlTabIdentation(String fileName, String[] expectedMessages, boolean onlyShowFirstWarning)
            throws Exception {
        String testFileRelativePath = TABIDENT_CHECK_TEST_DIRECTORY_NAME + File.separator + fileName + "."
                + CheckConstants.XML_EXTENSION;
        String testFileAbsolutePath = getPath(testFileRelativePath);
        String messageFilePath = testFileAbsolutePath;
        if (onlyShowFirstWarning) {
            config.addAttribute("onlyShowFirstWarning", "true");
        } else {
            config.addAttribute("onlyShowFirstWarning", "false");
        }
        verify(createChecker(config), testFileAbsolutePath, messageFilePath, expectedMessages);
    }

    private void verifyXmlTabIdentation(String fileName, String[] expectedMessages) throws Exception {
        verifyXmlTabIdentation(fileName, expectedMessages, true);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration("root");
        defaultConfiguration.addChild(config);
        return defaultConfiguration;
    }
}
