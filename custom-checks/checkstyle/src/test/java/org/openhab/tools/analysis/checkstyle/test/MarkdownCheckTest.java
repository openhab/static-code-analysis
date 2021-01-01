/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.README_MD_FILE_NAME;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.checkstyle.readme.MarkdownCheck;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link MarkdownCheck}
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 * @author Lyubomir Papazov - Added more tests
 */
public class MarkdownCheckTest extends AbstractStaticCheckTest {
    private static final String ADDED_README_IN_BUILD_PROPERTIES_MSG = "README.MD file must not be added to the bin.includes property";
    private static final String ADDED_DOC_FOLDER_IN_BUILD_PROPERTIES_MSG = "The doc folder must not be added to the bin.includes property";
    private DefaultConfiguration config;

    @Before
    public void setUpClass() {
        createValidConfig();
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/markdownCheckTest";
    }

    @Test
    public void testHeader() throws Exception {
        String[] expectedMessages = generateExpectedMessages(1, "Missing an empty line after the Markdown header (#).");
        verifyMarkDownFile("testHeader", expectedMessages);
    }

    @Test
    public void testForbiddNodeVisit() throws Exception {
        verifyMarkDownFile("testForbiddenNodeVisit", noMessagesExpected());
    }

    private String[] noMessagesExpected() {
        String[] expectedMessages = EMPTY_STRING_ARRAY;
        return expectedMessages;
    }

    @Test
    public void headerAtEndOfFile() throws Exception {
        String[] expectedMessages = generateExpectedMessages(6,
                "There is a header at the end of the Markdown file. Please consider adding some content below.");
        verifyMarkDownFile("testHeaderAtEndOfFile", expectedMessages);
    }

    @Test
    public void testSpecialHeader() throws Exception {
        verifyMarkDownFile("testHeaderOutOfBounds", noMessagesExpected());
    }

    @Test
    public void testEmptyLineBeforeList() throws Exception {
        String[] expectedMessages = generateExpectedMessages(2, "The line before a Markdown list must be empty.");
        verifyMarkDownFile("testEmptyLineBeforeList", expectedMessages);
    }

    @Test
    public void testEmptyLineAfterList() throws Exception {
        String[] expectedMessages = generateExpectedMessages(7, "The line after a Markdown list must be empty.");
        verifyMarkDownFile("testEmptyLineAfterList", expectedMessages);
    }

    @Test
    public void testListAtEndOfFile() throws Exception {
        verifyMarkDownFile("testListAtEndOfFile", noMessagesExpected());
    }

    @Test
    public void testCodeFormattedListBlock() throws Exception {
        verifyMarkDownFile("testCodeFormattedListBlock", noMessagesExpected());
    }

    @Test
    public void testEscapedAsterisk() throws Exception {
        verifyMarkDownFile("testEscapedAsterisk", noMessagesExpected());
    }

    @Test
    public void testEscapedUnderscore() throws Exception {
        verifyMarkDownFile("testEscapedUnderscore", noMessagesExpected());
    }

    @Test
    public void testEscapedBrackets() throws Exception {
        verifyMarkDownFile("testEscapedBrackets", noMessagesExpected());
    }

    @Test
    public void testEscapedCopyrightSymbol() throws Exception {
        verifyMarkDownFile("testEscapedCopyrightSymbol", noMessagesExpected());
    }

    @Test
    public void testEscapedHeader() throws Exception {
        verifyMarkDownFile("testEscapedHeader", noMessagesExpected());
    }

    @Test
    public void testEmptyLinedCodeBlock() throws Exception {
        verifyMarkDownFile("testEmptyLinedCodeBlock", noMessagesExpected());
    }

    @Test
    public void testCodeFormattedListElement() throws Exception {
        verifyMarkDownFile("testCodeFormattedListElement", noMessagesExpected());
    }

    @Test
    public void testCodeSectionAtBeginingOfFile() throws Exception {
        String[] expectedMessages = generateExpectedMessages(1,
                "The line before code formatting section must be empty.");
        verifyMarkDownFile("testCodeSectionAtBeginingOfFile", expectedMessages);
    }

    @Test
    public void testListAtBeginingOfFile() throws Exception {
        String[] expectedMessages = generateExpectedMessages(1, "The line before a Markdown list must be empty.");
        verifyMarkDownFile("testListAtBeginingOfFile", expectedMessages);
    }

    @Test
    public void testOneElementList() throws Exception {
        verifyMarkDownFile("testOneElementedList", noMessagesExpected());
    }

    @Test
    public void testEmphasizeItalicListElement() throws Exception {
        verifyMarkDownFile("testEmphasizeItalicListElement", noMessagesExpected());
    }

    @Test
    public void testPreCodeSection() throws Exception {
        String[] expectedMessages = generateExpectedMessages(36,
                "The line before code formatting section must be empty.");
        verifyMarkDownFile("testPreCodeSection", expectedMessages);
    }

    @Test
    public void testCodeSectionLineNumberError() throws Exception {
        verifyMarkDownFile("testCodeSectionLineNumberError", noMessagesExpected());
    }

    @Test
    public void testListFirstLineSameAsParagraph() throws Exception {
        verifyMarkDownFile("testListFirstLineSameAsParagraph", noMessagesExpected());
    }

    @Test
    public void testListBeginingSameAsAnotherLineBegining() throws Exception {
        verifyMarkDownFile("testListBeginingSameAsAnotherLineBegining", noMessagesExpected());
    }

    @Test
    public void testListLastLineSameAsParagraph() throws Exception {
        verifyMarkDownFile("testListLastLineSameAsParagraph", noMessagesExpected());
    }

    @Test
    public void testEmptyCodeSection() throws Exception {
        String[] expectedMessages = generateExpectedMessages(3,
                "There is an empty or unclosed code formatting section. Please correct it.");
        verifyMarkDownFile("testEmptyCodeSection", expectedMessages);
    }

    @Test
    public void testEmptyLineAfterCodeSection() throws Exception {
        String[] expectedMessages = generateExpectedMessages(13,
                "The line after code formatting section must be empty.");
        verifyMarkDownFile("testEmptyLineAfterCodeSection", expectedMessages);
    }

    @Test
    public void testComplicatedCodeBlocks() throws Exception {
        verifyMarkDownFile("testComplicatedCodeBlocks", noMessagesExpected());
    }

    @Test
    public void testMultilineListItemsSeparation() throws Exception {
        verifyMarkDownFile("testListSeparation", noMessagesExpected());
    }

    @Test
    public void testCodeSectionAtEndOfFile() throws Exception {
        verifyMarkDownFile("testCodeSectionAtEndOfFile", noMessagesExpected());
    }

    @Test
    public void testMultiLineListItem() throws Exception {
        verifyMarkDownFile("testMultiLineListItems", noMessagesExpected());
    }

    @Test
    public void testValidMarkDown() throws Exception {
        verifyMarkDownFile("testValidMarkDown", noMessagesExpected());
    }

    @Test
    public void testLinkAsHeader() throws Exception {
        verifyMarkDownFile("testLinkAsHeader", noMessagesExpected());
    }

    @Test
    public void testReadMeAddedInBuildProperties() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0, ADDED_README_IN_BUILD_PROPERTIES_MSG);
        String testDirectoryName = "testAddedReadMeInBuildProperties";

        // The message is logged for the build.properties file
        verifyBuildProperties(expectedMessages, testDirectoryName);
    }

    @Test
    public void testAddedDummyDocInBuildProperties() throws Exception {
        String testDirectoryName = "testAddedDummyDocInBuildProperties";
        verifyBuildProperties(noMessagesExpected(), testDirectoryName);
    }

    @Test
    public void testDocFolderAddedInBuildProperties() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0, ADDED_DOC_FOLDER_IN_BUILD_PROPERTIES_MSG);
        String testDirectoryName = "testAddedDocFolderInBuildProperties";

        // The message is logged for the build.properties file
        verifyBuildProperties(expectedMessages, testDirectoryName);
    }

    @Test
    public void testAddedReadmeAndDocInBuildProperties() throws Exception {
        String[] expectedMessages = generateExpectedMessages(0, ADDED_README_IN_BUILD_PROPERTIES_MSG, 0,
                ADDED_DOC_FOLDER_IN_BUILD_PROPERTIES_MSG);
        String testDirectoryName = "testAddedReadmeAndDocInBuildProperties";

        // The message is logged for the build.properties file
        verifyBuildProperties(expectedMessages, testDirectoryName);
    }

    @Test
    public void testOpenhabBindingExec() throws Exception {
        String testDirectoryName = "org.openhab.binding.exec";
        verifyMarkDownFile(testDirectoryName, noMessagesExpected());
    }

    @Test
    public void testHeaderCreatedByDashesOnNextRow() throws Exception {
        verifyMarkDownFile("testHeaderCreatedByDashesOnNextRow", noMessagesExpected());
    }

    private void verifyBuildProperties(String[] expectedMessages, String testDirectoryName)
            throws IOException, Exception {
        String testDirectoryAbsolutePath = getPath(testDirectoryName);
        String messageFilePath = testDirectoryAbsolutePath + File.separator + "build.properties";
        verify(createChecker(config), messageFilePath, expectedMessages);
    }

    private void createValidConfig() {
        config = createModuleConfig(MarkdownCheck.class);
    }

    private void verifyMarkDownFile(String testDirectoryName, String[] expectedMessages) throws Exception {
        String testDirectoryRelativePath = testDirectoryName + File.separator + README_MD_FILE_NAME;
        String testDirectoryAbsolutePath = getPath(testDirectoryRelativePath);
        String messageFilePath = testDirectoryAbsolutePath;
        verify(createChecker(config), testDirectoryAbsolutePath, messageFilePath, expectedMessages);
    }
}
