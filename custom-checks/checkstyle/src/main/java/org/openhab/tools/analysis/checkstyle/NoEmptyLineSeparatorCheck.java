/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Checks tokens with body for an empty lines after their opening and before their
 * closing brace.
 *
 * The following example illustrates a block with an empty line after the opening brace
 * that will be reported by this check:
 *
 * <pre>
 *      for (::) {
 *
 *          int i=0;
 *      }
 * </pre>
 *
 * Case blocks without braces are checked too by default. The check will report an empty line
 * for the code snippet below between the two case tokens:
 *
 * <pre>
 *      switch (someConst) {
 *          case:
 *
 *          case:
 *          default: break;
 *      }
 * </pre>
 *
 *
 * By default the following tokens are checked:
 * <ul>
 * <li>{@link TokenTypes#STATIC_INIT},
 * <li>{@link TokenTypes#INSTANCE_INIT},
 * <li>{@link TokenTypes#METHOD_DEF},
 * <li>{@link TokenTypes#CTOR_DEF}
 *
 * <li>{@link TokenTypes#LITERAL_WHILE},
 * <li>{@link TokenTypes#LITERAL_DO},
 * <li>{@link TokenTypes#LITERAL_TRY},
 * <li>{@link TokenTypes#LITERAL_CATCH}
 * <li>{@link TokenTypes#LITERAL_FINALLY},
 * <li>{@link TokenTypes#LITERAL_IF},
 * <li>{@link TokenTypes#LITERAL_ELSE},
 * <li>{@link TokenTypes#LITERAL_SYNCHRONIZED},
 * <li>{@link TokenTypes#LITERAL_SWITCH},
 * <li>{@link TokenTypes#LITERAL_CASE},
 * <li>{@link TokenTypes#LITERAL_DEFAULT},
 * <li>{@link TokenTypes#CASE_GROUP}
 * </ul>
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Tanya Georgieva - Added check for switch statement without braces
 */
public class NoEmptyLineSeparatorCheck extends AbstractCheck {

    private static final String MSG_LINE_AFTER_OPENING_BRACE_EMPTY = "Remove empty line after opening brace";
    private static final String MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY = "Remove empty line before closing brace";
    private static final String MSG_FOR_EMPTY_LINE = "Remove empty line";

    private boolean isCaseGroupWithoutBraces = false;
    private boolean isCaseGroupWithoutListOfStatements = false;
    private boolean isCaseGroupWithBraces = false;

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    // @formatter:off
    @Override
    public int[] getAcceptableTokens(){
        return new int[] {
                // Class members
                TokenTypes.STATIC_INIT,
                TokenTypes.INSTANCE_INIT,
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,

                TokenTypes.LITERAL_WHILE,
                TokenTypes.LITERAL_DO,
                TokenTypes.LITERAL_TRY,
                TokenTypes.LITERAL_CATCH,
                TokenTypes.LITERAL_FINALLY,
                TokenTypes.LITERAL_IF,
                TokenTypes.LITERAL_ELSE,
                TokenTypes.LITERAL_SYNCHRONIZED,
                TokenTypes.LITERAL_SWITCH,
                TokenTypes.LITERAL_CASE,
                TokenTypes.LITERAL_DEFAULT,
                TokenTypes.CASE_GROUP
            };
    }
    //@formatter:on

    @Override
    public int[] getRequiredTokens() {
        // we can configure the check to visit no tokens
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST leftCurly = findLeftCurly(ast);
        DetailAST rightCurly = findRightCurly(ast);

        if (leftCurly != null && rightCurly != null) {
            int leftCurlyLine = leftCurly.getLineNo();
            int rightCurlyLine = rightCurly.getLineNo();

            if (leftCurlyLine == rightCurlyLine) {
                // The block is a one liner
                return;
            }
            int lineAfterLeftCurly = leftCurlyLine + 1;
            int lineBeforeRightCurly = rightCurlyLine - 1;

            if (isBlank(lineAfterLeftCurly)) {
                if (isCaseGroupWithoutBraces) {
                    boolean isLineAfterOpeningBrace = lineAfterLeftCurly == lineBeforeRightCurly;
                    /*
                     * if the case clause is without braces the empty line could be after the return
                     * and we should not log warning message
                     *
                     * case 1:
                     * ....return number;
                     * (empty line)
                     */
                    boolean isEmptyLineWithWarningForCaseGroup = !(isLineAfterOpeningBrace
                            && (isCaseGroupWithBraces || isCaseGroupWithoutListOfStatements));
                    /*
                     * if the case clause is without braces the empty line could be after the colon(:)
                     * we should log warning message
                     *
                     * case 1:
                     * (empty line)
                     * ....return number;
                     */
                    if (isEmptyLineWithWarningForCaseGroup) {
                        log(lineAfterLeftCurly, MSG_FOR_EMPTY_LINE);
                    }
                } else {
                    /*
                     * if the case clause is with braces the line after the opening brace could be empty or
                     * the line after opening brace for other list of statements
                     *
                     * case 1: {
                     * (empty line)
                     * ....int number
                     * }
                     *
                     * public void method(){
                     * (empty line)
                     * .....
                     */
                    log(lineAfterLeftCurly, MSG_LINE_AFTER_OPENING_BRACE_EMPTY);
                }
            }
            boolean isLineBeforeClosingBrace = lineAfterLeftCurly < lineBeforeRightCurly;
            /*
             * if the case clause is with braces the line before the closing one could be empty or
             * the line before the closing brace for other list of statements
             *
             * case 1: {
             * .....int number
             * (empty line)
             * }
             * (empty line)
             * default:
             * (empty line)
             * }(closing brace of switch definition)
             */
            boolean isLineBeforeClosingBraceEmpty = isBlank(lineBeforeRightCurly) && isLineBeforeClosingBrace
                    && !isCaseGroupWithoutBraces;

            if (isLineBeforeClosingBraceEmpty) {
                log(lineBeforeRightCurly, MSG_LINE_BEFORE_CLOSING_BRACE_EMPTY);
            }
        }
    }

    /**
     * Calculates the left curly corresponding to the block/case to be checked.
     *
     * @param ast a {@code DetailAST} value
     * @return the left curly corresponding to the block to be checked
     */
    private DetailAST findLeftCurly(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.LITERAL_CASE:
            case TokenTypes.LITERAL_DEFAULT: {
                DetailAST nextNode = ast.getParent().getNextSibling();
                return (nextNode != null) ? ast : null;
            }
            default:
                return findLeftCurlyInBlock(ast);
        }
    }

    /**
     * Calculates the right curly corresponding to the block/case to be checked.
     *
     * @param ast a {@code DetailAST} value
     * @return the right curly corresponding to the block to be checked
     */
    private DetailAST findRightCurly(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.LITERAL_DEFAULT:
            case TokenTypes.LITERAL_CASE: {
                /*
                 * cases are nested in case groups(a group of case clauses),
                 * we are searching for the case in the next case group or
                 * for the next SLIST(a list of statements) if there is no case group after the current one
                 */
                DetailAST nextNode = ast.getParent().getNextSibling();

                if (nextNode != null) {
                    DetailAST nextCaseClause = nextNode.getFirstChild();
                    DetailAST nextSlist = ast.getNextSibling();
                    DetailAST getNextSlist = nextSlist != null ? nextSlist : nextNode;
                    DetailAST getNextCaseClause = nextCaseClause != null ? nextCaseClause : getNextSlist;
                    return getNextCaseClause;
                }
            }
            case TokenTypes.CASE_GROUP: {
                DetailAST slistAST = ast.findFirstToken(TokenTypes.SLIST);

                if (slistAST != null) {
                    /*
                     * if the current case group is not followed by node
                     * of type SLIST(a list of statements) the case is without braces
                     *
                     * case 1:
                     * ....int number
                     */
                    isCaseGroupWithoutBraces = slistAST.getFirstChild().getType() != TokenTypes.SLIST;
                    /*
                     * case 1: {
                     * .....int number
                     */
                    isCaseGroupWithBraces = ast.getLineNo() == slistAST.getFirstChild().getLineNo();
                    return findRightCurlyInBlock(slistAST);
                } else {
                    /*
                     * the current case group could be without list of statement
                     * so the next node is another case group or
                     * closing curly brace of the switch definition
                     *
                     * default:
                     *
                     * }
                     */
                    isCaseGroupWithoutListOfStatements = true;
                    return findRightCurlyInBlock(ast);
                }
            }
            default:
                return findRightCurlyInBlock(ast);
        }
    }

    private DetailAST findLeftCurlyInBlock(DetailAST ast) {
        final DetailAST leftCurly;
        final DetailAST slistAST = ast.findFirstToken(TokenTypes.SLIST);

        if (slistAST == null) {
            // elements that do not contain statements list, contain directly left curly
            leftCurly = ast.findFirstToken(TokenTypes.LCURLY);
        } else {
            // the statement list begins from the left curly
            leftCurly = slistAST;
        }
        return leftCurly;
    }

    private DetailAST findRightCurlyInBlock(DetailAST ast) {
        final DetailAST rightCurly;
        final DetailAST slistAST = ast.findFirstToken(TokenTypes.SLIST);

        if (slistAST == null) {
            // elements that do not contain statements list, contain directly right curly
            rightCurly = ast.findFirstToken(TokenTypes.RCURLY);
        } else {
            // the statement list ends with right curly
            rightCurly = slistAST.getLastChild();
        }
        return rightCurly;
    }

    private boolean isBlank(int lineNumber) {
        if (lineNumber < 1) {
            throw new IllegalArgumentException("Line numbering starts from 1");
        }
        return getFileContents().lineIsBlank(lineNumber - 1);
    }
}
