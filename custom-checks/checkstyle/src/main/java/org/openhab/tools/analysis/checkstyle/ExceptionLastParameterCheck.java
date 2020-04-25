/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.utils.SatCheckUtils;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Checks the code and generates a warning if an exception is caught and logged
 * and it is not set as a last parameter to the logging.
 *
 * @author Tanya Georgieva - Initial Contribution
 */
public class ExceptionLastParameterCheck extends AbstractCheck {

    private static final Class EXCEPTION = Exception.class;
    private static final Class RUNTIME_EXCEPTION = RuntimeException.class;

    private static final String EXCEPTION_AS_LAST_PARAMETER_MSG = "Please set exception as last parameter";
    private static final String FULL_STACK_TRACE_MSG = "Log exception full stack trace";
    private static final String FULL_STACK_TRACE_EXCEPTION_AS_LAST_PARAMETER_MSG = EXCEPTION_AS_LAST_PARAMETER_MSG
            .concat(" and ").concat(FULL_STACK_TRACE_MSG);

    private static final String EXCEPTION_PACKAGE = "java.lang.";
    private static final String LOG_LEVEL_ERROR = "error";
    private static final String LOGGER = "logger";
    private static final String THIS = "this";

    private final Log logger = LogFactory.getLog(ExceptionLastParameterCheck.class);

    private List<String> imports = new LinkedList<>();
    private List<DetailAST> exceptions = new LinkedList<>();

    private String exceptionName = "";

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.LITERAL_CATCH, TokenTypes.IMPORT };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtils.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        // imports are always prior to catch blocks
        if (ast.getType() == TokenTypes.IMPORT) {
            String currentImport = SatCheckUtils.createFullyQualifiedName(ast);
            this.imports.add(currentImport);
        } else {
            findLoggedExceptions(ast);
        }
    }

    private void findLoggedExceptions(DetailAST catchAst) {
        extractCaughtExceptions(catchAst);

        List<DetailAST> loggers = SatCheckUtils.getAllChildrenNodesOfType(catchAst.getLastChild(), TokenTypes.EXPR);
        if (loggers.isEmpty()) {
            LinkedList<DetailAST> requiredNodes = new LinkedList<>();
            // if the logger is in if-else-statement the list of loggers would be empty.
            // the logger nodes we are looking for are nested, so we use recursion to find them
            loggers = SatCheckUtils.getAllNodesOfType(requiredNodes, catchAst.getLastChild(), TokenTypes.EXPR);
        }
        for (DetailAST loggerNode : loggers) {
            DetailAST currentLogger = getLogger(loggerNode);

            if (currentLogger != null) {
                String logLevel = "";
                // when literal THIS is NOT used the log level is a sibling node of the logger
                // logger.error(...)
                // otherwise the log level will be a sibling node of the logger's parent
                // this.logger.error(...)
                boolean isLogLevelNextNode = currentLogger.getNextSibling() != null;
                if (isLogLevelNextNode) {
                    logLevel = currentLogger.getNextSibling().getText();
                } else {
                    logLevel = currentLogger.getParent().getNextSibling().getText();
                }
                Class<?> currentExceptionClass = getExceptionClass();
                boolean isExceptionUnchecked = RUNTIME_EXCEPTION.isAssignableFrom(currentExceptionClass);
                boolean isExceptionChecked = !isExceptionUnchecked && EXCEPTION.isAssignableFrom(currentExceptionClass);
                boolean isExceptionValid = isExceptionUnchecked
                        || (isExceptionChecked && LOG_LEVEL_ERROR.equals(logLevel));
                if (isExceptionValid) {
                    // ELIST node contains the logger's parameters
                    DetailAST elistAst = loggerNode.getFirstChild().findFirstToken(TokenTypes.ELIST);
                    List<DetailAST> parameters = SatCheckUtils.getAllChildrenNodesOfType(elistAst, TokenTypes.EXPR);
                    validateLogger(parameters);
                }
            }
        }
    }

    private void validateLogger(List<DetailAST> parameters) {
        int lastParameterPosition = parameters.size();
        int logLineNumber = 0;
        // checking if exception parameter is with full stack trace
        boolean isFullStackTraceExceptionFound = false;
        // checking if at some position in the logger
        // there is exception parameter calling one of its methods
        boolean isExceptionCallingMethodFound = false;
        int fullStackTraceExceptionPosition = 0;
        int exceptionCallingMethodPosition = 0;

        for (int i = 0; i < lastParameterPosition; i++) {
            DetailAST currentParameter = SatCheckUtils.getFirstNodeOfType(parameters.get(i).getFirstChild(),
                    TokenTypes.IDENT);
            // The current parameter would be null if it's token type is anything different than IDENT.
            // For example the parameter could be of type STRING_LITERAL or NUM_INT.
            // In this case: logger.trace("\"{}\" is not a valid integer number.", e, value)
            // the current parameters will be "value" and "e"
            if (currentParameter != null) {
                String currentParameterName = currentParameter.getText();
                logLineNumber = currentParameter.getLineNo();

                if (this.exceptionName.equals(currentParameterName)) {
                    // if a parameter is calling a method - e.getMessage()
                    // the abstract syntax tree will represent the method call as its sibling
                    // so we are checking the childCount of the parameter's parent
                    boolean isParameterCallingMethod = currentParameter.getParent().getChildCount() > 1;
                    int parameterPosition = i + 1;

                    if (!isParameterCallingMethod) {
                        isFullStackTraceExceptionFound = true;
                        fullStackTraceExceptionPosition = parameterPosition;
                    } else {
                        isExceptionCallingMethodFound = true;
                        exceptionCallingMethodPosition = parameterPosition;
                    }
                }
            }
        }
        // Log message when the exception parameter is not set as last:
        // logger.trace("StringExample", e, e.getMessage());
        if (isFullStackTraceExceptionFound && fullStackTraceExceptionPosition != lastParameterPosition) {
            log(logLineNumber, EXCEPTION_AS_LAST_PARAMETER_MSG);
        }

        if (!isFullStackTraceExceptionFound && isExceptionCallingMethodFound) {
            if (exceptionCallingMethodPosition == lastParameterPosition) {
                // Log message when the exception parameter is not logged with the full stack trace:
                // logger.trace("StringExample", e.getMessage());
                log(logLineNumber, FULL_STACK_TRACE_MSG);
            } else {
                // Log message when the exception parameter is not logged with the full stack trace
                // and is not set as last: logger.trace("StringExample", e.getMessage(), value);
                log(logLineNumber, FULL_STACK_TRACE_EXCEPTION_AS_LAST_PARAMETER_MSG);
            }
        }
    }

    private Class<?> getExceptionClass() {
        for (DetailAST exceptionNode : this.exceptions) {
            String exception = exceptionNode.getText();
            // get the package and the class name for the exception:
            // "java.lang.annotation.AnnotationTypeMismatchException"
            String fullyQualifiedExceptionName = this.imports.stream().filter(e -> e.endsWith("." + exception))
                    .collect(Collectors.joining());
            // if the fullyQualifiedExceptionName is empty
            // the caught exception is from package "java.lang" which is not imported
            if (fullyQualifiedExceptionName.isEmpty()) {
                fullyQualifiedExceptionName = EXCEPTION_PACKAGE + exception;
            }

            try {
                Class<?> exceptionClass = Class.forName(fullyQualifiedExceptionName);
                return exceptionClass;
            } catch (ClassNotFoundException e) {
                String message = MessageFormat.format("Exception class {0} not found.", fullyQualifiedExceptionName);
                logger.info(message, e);
            }
        }
        return null;
    }

    private void extractCaughtExceptions(DetailAST catchAst) {
        DetailAST catchBlockParameters = catchAst.findFirstToken(TokenTypes.PARAMETER_DEF);
        DetailAST parametersType = catchBlockParameters.findFirstToken(TokenTypes.TYPE);
        DetailAST currentException = parametersType.getFirstChild();

        this.exceptionName = catchBlockParameters.getLastChild().getText();
        // if there are more than one caught exceptions:
        // catch (ArithmeticException | NumberFormatException e)
        // the current exception node will be the binary or operator "|"
        if (currentException.getType() != TokenTypes.IDENT) {
            this.exceptions = SatCheckUtils.getAllChildrenNodesOfType(currentException, TokenTypes.IDENT);
        } else {
            this.exceptions = SatCheckUtils.getAllChildrenNodesOfType(parametersType, TokenTypes.IDENT);
        }
    }

    private DetailAST getLogger(DetailAST ast) {
        if (ast == null) {
            return null;
        }
        if (THIS.equals(ast.getText())) {
            return getLogger(ast.getNextSibling());
        }
        if (LOGGER.equalsIgnoreCase(ast.getText())) {
            return ast;
        }
        return getLogger(ast.getFirstChild());
    }
}
