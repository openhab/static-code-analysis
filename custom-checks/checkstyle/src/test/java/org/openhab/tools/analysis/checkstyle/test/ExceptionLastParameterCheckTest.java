/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ExceptionLastParameterCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ExceptionLastParameterCheck}
 *
 * @author Tanya Georgieva - Initial Contribution
 */
public class ExceptionLastParameterCheckTest extends AbstractStaticCheckTest {

    private static final String EXCEPTION_AS_LAST_PARAMETER_MSG = "Please set exception as last parameter";
    private static final String FULL_STACK_TRACE_MSG = "Log exception full stack trace";
    private static final String FULL_STACK_TRACE_EXCEPTION_AS_LAST_PARAMETER_MSG = EXCEPTION_AS_LAST_PARAMETER_MSG
            .concat(" and ").concat(FULL_STACK_TRACE_MSG);

    private static DefaultConfiguration config = createModuleConfig(ExceptionLastParameterCheck.class);

    @Test
    public void verifInvalidLoggerWithLiteralThis() throws Exception {
        verifyJavaFile("InvalidLoggerWithLiteralThis.java", generateExpectedMessages(25, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifCaughtCheckedExceptionWithInvalidParameter() throws Exception {
        verifyJavaFile("CaughtCheckedExceptionWithInvalidParameter.java",
                generateExpectedMessages(14, FULL_STACK_TRACE_MSG, 27, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifCaughtCheckedExceptionWithInvalidLogLevel() throws Exception {
        verifyJavaFile("CaughtCheckedExceptionWithInvalidLogLevel.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifCaughtCheckedExceptionWithValidLogLevel() throws Exception {
        verifyJavaFile("CaughtCheckedExceptionWithValidLogLevel.java", generateExpectedMessages(14,
                EXCEPTION_AS_LAST_PARAMETER_MSG, 27, FULL_STACK_TRACE_EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifCaughtUncheckedExceptionWithValidLogger() throws Exception {
        verifyJavaFile("CaughtUncheckedExceptionWithValidLogger.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifCaughtUncheckedExceptionWithInvalidLogger() throws Exception {
        verifyJavaFile("CaughtUncheckedExceptionWithInvalidLogger.java",
                generateExpectedMessages(9, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyValidIndirectExceptionOfRuntimeException() throws Exception {
        verifyJavaFile("ValidIndirectExceptionOfRuntimeException.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifyCaughtUncheckedExceptionForMultipleLoggers() throws Exception {
        verifyJavaFile("CaughtUncheckedExceptionForMultipleLoggers.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifyClassWithoutCatchBlock() throws Exception {
        verifyJavaFile("ClassWithoutCatchBlock.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifyInvalidLoggedUncheckedException() throws Exception {
        verifyJavaFile("InvalidLoggedUncheckedException.java",
                generateExpectedMessages(14, FULL_STACK_TRACE_MSG, 16, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyValidLoggerMultipleParameters() throws Exception {
        verifyJavaFile("ValidLoggerMultipleParameters.java", generateExpectedMessages(16, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyInvalidLoggerWithMultipleExceptions() throws Exception {
        verifyJavaFile("InvalidLoggerWithMultipleExceptions.java", generateExpectedMessages(26, FULL_STACK_TRACE_MSG,
                30, FULL_STACK_TRACE_MSG, 34, FULL_STACK_TRACE_MSG, 38, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyLoggerInIfElseStatement() throws Exception {
        verifyJavaFile("LoggerInIfElseStatement.java", generateExpectedMessages(18, EXCEPTION_AS_LAST_PARAMETER_MSG, 25,
                FULL_STACK_TRACE_EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyInvalidIndirectExceptionOfRuntimeException() throws Exception {
        verifyJavaFile("InvalidIndirectExceptionOfRuntimeException.java",
                generateExpectedMessages(12, EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyInvalidLoggerMultipleParameters() throws Exception {
        verifyJavaFile("InvalidLoggerMultipleParameters.java",
                generateExpectedMessages(20, EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyCaughtUncheckedExceptionWithMethodCall() throws Exception {
        verifyJavaFile("CaughtUncheckedExceptionWithMethodCall.java",
                generateExpectedMessages(34, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyValidMultipleExceptions() throws Exception {
        verifyJavaFile("ValidMultipleExceptions.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifyMultipleCatchBlocksWithInvalidLogger() throws Exception {
        verifyJavaFile("MultipleCatchBlocksWithInvalidLogger.java",
                generateExpectedMessages(12, FULL_STACK_TRACE_EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyInvalidLoggerWithMultipleExceptionParameters() throws Exception {
        verifyJavaFile("InvalidLoggerWithMultipleExceptionParameters.java",
                generateExpectedMessages(27, EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyValidLoggerWihtMultipleCatch() throws Exception {
        verifyJavaFile("ValidLoggerWihtMultipleCatch.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifyInvalidLogger() throws Exception {
        verifyJavaFile("InvalidLogger.java", generateExpectedMessages(13, EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyInvalidLoggerUpperCase() throws Exception {
        verifyJavaFile("InvalidLoggerUpperCase.java", generateExpectedMessages(24, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyExceptionsWithLogger() throws Exception {
        verifyJavaFile("ExceptionsWithLogger.java",
                generateExpectedMessages(12, FULL_STACK_TRACE_MSG, 14, FULL_STACK_TRACE_MSG, 24, FULL_STACK_TRACE_MSG));
    }

    @Test
    public void verifyCheckedExceptionWithInvalidLogger() throws Exception {
        verifyJavaFile("CheckedExceptionWithInvalidLogger.java",
                generateExpectedMessages(40, EXCEPTION_AS_LAST_PARAMETER_MSG));
    }

    @Test
    public void verifyLoggerWithMultipleParametersWithoutException() throws Exception {
        verifyJavaFile("LoggerWithMultipleParametersWithoutException.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void verifCaughtAndLoggedError() throws Exception {
        verifyJavaFile("CaughtAndLoggedError.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    private void verifyJavaFile(String fileName, String[] expected) throws Exception {
        String absolutePath = getPath(fileName);
        verify(config, absolutePath, expected);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/exceptionLastParameterCheckTest";
    }
}
