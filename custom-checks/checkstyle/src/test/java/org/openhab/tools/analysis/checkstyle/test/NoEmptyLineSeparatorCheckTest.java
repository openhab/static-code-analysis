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

import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.NoEmptyLineSeparatorCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link NoEmptyLineSeparatorCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Tanya Georgieva - Added tests for switch statement without braces
 */
public class NoEmptyLineSeparatorCheckTest extends AbstractStaticCheckTest {

    private static final String MSG_LINE_AFTER_OPENING_BRACE_EMPTY = "Remove empty line after opening brace";
    private static final String MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY = "Remove empty line before closing brace";
    private static final String MSG_FOR_EMPTY_LINE = "Remove empty line";

    private static DefaultConfiguration config = createModuleConfig(NoEmptyLineSeparatorCheck.class);

    @Override
    protected String getPackageLocation() {
        return "checkstyle/noEmptyLineSeparatorCheck";
    }

    @Test
    public void verifyValidSwitchDefinitionWithoutBraces() throws Exception {
        verifyJavaFileNoErrors("ValidSwitchDefinitionWithoutBraces.java");
    }

    @Test
    public void verifyEmptyLineAfterInvalidDefaultCaseWithoutBraces() throws Exception {
        verifyJavaFile("EmptyLineAfterInvalidDefaultCaseWithoutBraces.java", generateExpectedMessages(10,
                MSG_FOR_EMPTY_LINE, 23, MSG_FOR_EMPTY_LINE, 27, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyEmptyLineInSwitchDefinitionWithBraces() throws Exception {
        String[] expectedMessages = generateExpectedMessages(11, MSG_FOR_EMPTY_LINE, 15,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 17, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY, 19,
                MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInSwitchDefinitionWithBraces.java", expectedMessages);
    }

    @Test
    public void verifyInvalidSwitchDefinitionWithoutBrace() throws Exception {
        verifyJavaFile("InvalidSwitchDefinitionWithoutBrace.java",
                generateExpectedMessages(22, MSG_FOR_EMPTY_LINE, 24, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyEmptyLineInDefaultCaseWithBraces() throws Exception {
        String[] expectedMessages = generateExpectedMessages(6, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 8,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 25, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 27,
                MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInDefaultCaseWithBraces.java", expectedMessages);
    }

    @Test
    public void verifyEmptyLineAfterDefaultCaseWithoutBracesAtTheEndOfSwitch() throws Exception {
        verifyJavaFile("EmptyLineAfterDefaultCaseWithoutBracesAtTheEndOfSwitch.java",
                generateExpectedMessages(23, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyEmptyLineInValidSwitchDefinitionWithoutBraces() throws Exception {
        verifyJavaFile("EmptyLineInValidSwitchDefinitionWithoutBraces.java",
                generateExpectedMessages(25, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyMultipleEmptyLinesInValidSwitchDefinitionWithoutBraces() throws Exception {
        verifyJavaFile("MultipleEmptyLinesInValidSwitchDefinitionWithoutBraces.java",
                generateExpectedMessages(22, MSG_FOR_EMPTY_LINE, 26, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyEmptyLinesInValidSwitchDefinitionWithoutBracesWithoutDefault() throws Exception {
        verifyJavaFile("EmptyLinesInValidSwitchDefinitionWithoutBracesWithoutDefault.java",
                generateExpectedMessages(22, MSG_FOR_EMPTY_LINE, 24, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyEmptyLineAfterInvalidCaseWithoutBraces() throws Exception {
        verifyJavaFile("EmptyLineAfterInvalidCaseWithoutBraces.java",
                generateExpectedMessages(22, MSG_FOR_EMPTY_LINE, 25, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY));
    }

    @Test
    public void verifyValidMethodDefinitions() throws Exception {
        verifyJavaFileNoErrors("ValidMethodDefinition.java");
    }

    @Test
    public void verifyEmptyLinesInMethodDefinitions() throws Exception {
        String[] expectedMessages = generateExpectedMessages(6, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 8,
                MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInMethodDefinition.java", expectedMessages);
    }

    @Test
    public void verifyValidConstuctorDefinition() throws Exception {
        verifyJavaFileNoErrors("ValidConstructorDefinition.java");
    }

    @Test
    public void verifyEmptyLinesInConstructorDefinition() throws Exception {
        String[] expectedMessages = generateExpectedMessages(9, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInConstructorDefinition.java", expectedMessages);
    }

    @Test
    public void verifyValidInitBlocks() throws Exception {
        verifyJavaFileNoErrors("ValidInitBlocks.java");
    }

    @Test
    public void verifyEmptyLinesInInitBlocks() throws Exception {
        String[] expectedMessages = generateExpectedMessages(8, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 13,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 15, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInInitBlocks.java", expectedMessages);
    }

    @Test
    public void verifyValidWhileLoop() throws Exception {
        verifyJavaFileNoErrors("ValidWhileLoop.java");
    }

    @Test
    public void verifyEmptyLinesInWhileLoop() throws Exception {
        String[] expectedMessages = generateExpectedMessages(11, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY, 15,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInWhileLoop.java", expectedMessages);
    }

    @Test
    public void verifyValidTryBlock() throws Exception {
        verifyJavaFileNoErrors("ValidTryBlock.java");
    }

    @Test
    public void verifyEmptyLineInTryBlock() throws Exception {
        String[] expectedMessages = generateExpectedMessages(13, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 19,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 21, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInTryBlock.java", expectedMessages);
    }

    @Test
    public void verifyValidIfElseBlock() throws Exception {
        verifyJavaFileNoErrors("ValidIfElseBlock.java");
    }

    @Test
    public void verifyEmptyLineInIfElseBlock() throws Exception {
        String[] expectedMessages = generateExpectedMessages(10, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY, 20,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 22, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInIfElseBlock.java", expectedMessages);
    }

    @Test
    public void verifyValidSynchronizedBlock() throws Exception {
        verifyJavaFileNoErrors("ValidSynchronizedBlock.java");
    }

    @Test
    public void verifyEmptyLineInSynchronizedBlock() throws Exception {
        verifyEmptyLineAfterOpeningBraces("EmptyLineInSynchronizedBlock.java", 7);
    }

    @Test
    public void verifyNotProperlyFormattedWhileBlock() throws Exception {
        verifyJavaFileNoErrors("NotProperlyFormattedWhileBlock.java");
    }

    @Test
    public void verifyOneLineFormattedWhileBlock() throws Exception {
        verifyJavaFileNoErrors("OneLineFormattedWhileBlock.java");
    }

    @Test
    public void verifyEmptySingleLineBodyBlock() throws Exception {
        verifyEmptyLineAfterOpeningBraces("EmptySingleLineBodyBlock.java", 9);
    }

    @Test
    public void verifyValidSwitchDefinition() throws Exception {
        verifyJavaFileNoErrors("ValidSwitchDefinition.java");
    }

    @Test
    public void verifyEmptyLineInSwitchDefinition() throws Exception {
        String[] expectedMessages = generateExpectedMessages(12, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 27,
                MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInSwitchDefinition.java", expectedMessages);
    }

    @Test
    public void verifyEmptyLineInCase() throws Exception {
        String[] expectedMessages = generateExpectedMessages(13, MSG_FOR_EMPTY_LINE);
        verifyJavaFile("EmptyLineInCase.java", expectedMessages);
    }

    @Test
    public void verifyEmptyLineInDefault() throws Exception {
        verifyJavaFileNoErrors("EmptyLineInDefault.java");
    }

    @Test
    public void verifyEmptyLineInCaseWithBraces() throws Exception {
        String[] expectedMessages = generateExpectedMessages(13, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 19,
                MSG_LINE_AFTER_OPENING_BRACE_EMPTY);
        verifyJavaFile("EmptyLineInCaseWithBraces.java", expectedMessages);
    }

    @Test
    public void verifyMultipleEmptyLinesInSwitchWithCases() throws Exception {
        String[] expectedMessages = generateExpectedMessages(12, MSG_LINE_AFTER_OPENING_BRACE_EMPTY, 29,
                MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY, 16, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY, 19, MSG_FOR_EMPTY_LINE,
                27, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
        verifyJavaFile("MutlitpleEmptyLinesInSwitchWithCases.java", expectedMessages);
    }

    @Test
    public void verifyValidAnnotationInterface() throws Exception {
        verifyJavaFileNoErrors("ValidAnnotationInterface.java");
    }

    @Test
    public void verifyValidEmptyDefault() throws Exception {
        verifyJavaFileNoErrors("ValidEmptyDefaultDefinition.java");
    }

    private void verifyJavaFile(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }

    private void verifyJavaFileNoErrors(String testFileName) throws Exception {
        verifyJavaFile(testFileName, CommonUtil.EMPTY_STRING_ARRAY);
    }

    private void verifyEmptyLineAfterOpeningBraces(String testFileName, int line) throws Exception {
        String[] expectedMessages = generateExpectedMessages(line, MSG_LINE_AFTER_OPENING_BRACE_EMPTY);
        verifyJavaFile(testFileName, expectedMessages);
    }
}
