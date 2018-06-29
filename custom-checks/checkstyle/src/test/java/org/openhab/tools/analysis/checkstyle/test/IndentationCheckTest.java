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

import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.IndentationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Checks whether whitespace characters are use instead of tabs in xml files
 * indentations and generates warnings in such cases.
 *
 * @author Lyubomir Papazov - initial contribution
 * @author Kristina Simova - Added tests
 * @author Velin Yordanov - Added tests
 */
public class IndentationCheckTest extends AbstractStaticCheckTest {

    private static final String BAD_INDENTATION_MESSAGE = "Not supported indentation characters used";
    private DefaultConfiguration config;

    @Before
    public void setUpClass() {
        config = createModuleConfig(IndentationCheck.class);
        config.addAttribute("fileExtensionsToIndentation", "xml-tabs,json-tabs");        
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
    public void shouldLogWhenFileIsAddedToExceptionsButNotFormattedProperly() throws Exception {
        config.addAttribute("exceptions", "not-valid.xml");
        
        String[] expectedMessages = generateExpectedMessages(3,BAD_INDENTATION_MESSAGE);
        verifyTabIdentation("not-valid.xml", expectedMessages, true);
    }
    
    @Test
    public void shouldNotLogWhenFileIsAddedToExceptionsAndIsFormattedProperly() throws Exception {
        config.addAttribute("exceptions", "valid.xml");
        verifyTabIdentation("valid.xml", noMessagesExpected(), true);
    }
    
    @Test
    public void testBadlyFormattedJson() throws Exception {
        String[] expectedMessages = generateExpectedMessages(7, BAD_INDENTATION_MESSAGE);
        verifyTabIdentation("badlyFormattedJson.json", expectedMessages, false);
    }

    @Test
    public void testOneIncorrectLine() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, BAD_INDENTATION_MESSAGE);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInOneLine.xml", expectedMessages);
    }

    @Test
    public void testManyIncorrectLinesOnlyShowFirstWarning() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, BAD_INDENTATION_MESSAGE);
        verifyXmlTabIdentation("WhiteSpaceUsedBeforeOpeningTagInManyLines.xml", expectedMessages);
    }

    @Test
    public void testManyIncorrectLinesShowAllWarnings() throws Exception {
        String[] expectedMessages = generateExpectedMessages(5, BAD_INDENTATION_MESSAGE, 6, BAD_INDENTATION_MESSAGE);
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
        String[] expectedMessages = generateExpectedMessages(3, BAD_INDENTATION_MESSAGE, 4, BAD_INDENTATION_MESSAGE,
                6, BAD_INDENTATION_MESSAGE, 7, BAD_INDENTATION_MESSAGE, 9, BAD_INDENTATION_MESSAGE, 10,
                BAD_INDENTATION_MESSAGE, 11, BAD_INDENTATION_MESSAGE, 13, BAD_INDENTATION_MESSAGE, 17,
                BAD_INDENTATION_MESSAGE, 18, BAD_INDENTATION_MESSAGE, 19, BAD_INDENTATION_MESSAGE, 20,
                BAD_INDENTATION_MESSAGE, 21, BAD_INDENTATION_MESSAGE, 22, BAD_INDENTATION_MESSAGE);
        verifyTabIdentation(fileName, expectedMessages, false);
    }

    @Test
    public void testXmlWithOnlyTabsForIndentation() throws Exception {
        String fileName = "binding.xml";
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
        String[] expectedMessages = generateExpectedMessages(5, BAD_INDENTATION_MESSAGE, 11, BAD_INDENTATION_MESSAGE,
                12, BAD_INDENTATION_MESSAGE, 13, BAD_INDENTATION_MESSAGE, 14, BAD_INDENTATION_MESSAGE, 20,
                BAD_INDENTATION_MESSAGE, 21, BAD_INDENTATION_MESSAGE, 22, BAD_INDENTATION_MESSAGE, 23,
                BAD_INDENTATION_MESSAGE, 30, BAD_INDENTATION_MESSAGE, 31, BAD_INDENTATION_MESSAGE, 32,
                BAD_INDENTATION_MESSAGE, 33, BAD_INDENTATION_MESSAGE, 39, BAD_INDENTATION_MESSAGE, 40,
                BAD_INDENTATION_MESSAGE, 41, BAD_INDENTATION_MESSAGE, 42, BAD_INDENTATION_MESSAGE, 59,
                BAD_INDENTATION_MESSAGE, 60, BAD_INDENTATION_MESSAGE, 61, BAD_INDENTATION_MESSAGE);
        verifyTabIdentation(fileName, expectedMessages, false);
    }

    @Test
    public void testAnotherXmlWithCDATAAndSpacesForIndentation() throws Exception {
        String fileName = "bridge.xml";
        String[] expectedMessages = generateExpectedMessages(10, BAD_INDENTATION_MESSAGE, 11, BAD_INDENTATION_MESSAGE,
                12, BAD_INDENTATION_MESSAGE, 13, BAD_INDENTATION_MESSAGE, 15, BAD_INDENTATION_MESSAGE, 16,
                BAD_INDENTATION_MESSAGE, 17, BAD_INDENTATION_MESSAGE, 18, BAD_INDENTATION_MESSAGE, 19,
                BAD_INDENTATION_MESSAGE, 20, BAD_INDENTATION_MESSAGE, 22, BAD_INDENTATION_MESSAGE);
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
            config.addAttribute("onlyShowFirstWarning", "true");
        } else {
            config.addAttribute("onlyShowFirstWarning", "false");
        }
        verify(createChecker(config), testFileAbsolutePath, messageFilePath, expectedMessages);
    }

    private void verifyXmlTabIdentation(String fileName, String[] expectedMessages) throws Exception {
        verifyTabIdentation(fileName, expectedMessages, true);
    }
}
