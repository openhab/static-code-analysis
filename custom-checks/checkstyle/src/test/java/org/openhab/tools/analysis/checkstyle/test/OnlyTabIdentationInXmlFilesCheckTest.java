/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.openhab.tools.analysis.checkstyle.OnlyTabIdentationInXmlFilesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.checkstyle.api.CheckConstants;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;    


/**
 * Checks whether whitespace characters are use instead of tabs in xml files 
 * indentations and generates warnings in such cases.
 * 
 * @author Lyubomir Papazov - initial contribution
 */
public class OnlyTabIdentationInXmlFilesCheckTest extends AbstractStaticCheckTest {
    
    private static final String WHITESPACE_USAGE_WARNING = "There were whitespace characters used for indentation. Please use tab characters instead";
    private static final String TABIDENT_CHECK_TEST_DIRECTORY_NAME = "onlyTabIdentationInXmlFilesCheck";
    private DefaultConfiguration config;
    
    @Before
    public void setUpClass() {
        config = createCheckConfig(OnlyTabIdentationInXmlFilesCheck.class);
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
    
    private String[] noMessagesExpected() {
        String[] expectedMessages = EMPTY_STRING_ARRAY;
        return expectedMessages;
    }
    
    private void verifyXmlTabIdentation(String fileName, String[] expectedMessages, boolean onlyShowFirstWarning) throws Exception {
        String testFileRelativePath = TABIDENT_CHECK_TEST_DIRECTORY_NAME + File.separator + fileName + "." + CheckConstants.XML_EXTENSION;
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
