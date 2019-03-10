/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.tools.analysis.checkstyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Checks the javadoc comment of a method or constructor and generates a message if:
 *
 * <p>
 * - There is a dash between the parameter name and the description:
 * </p>
 *
 * <pre>
 *     &#64;param parameter_name - parameter_description
 * </pre>
 *
 * <p>
 * - There are empty lines between tags:
 * </p>
 *
 * <pre>
 *     &#64;param parameter_name parameter_description
 *
 *     &#64;param parameter_name parameter_description
 * </pre>
 *
 * <p>
 * - The parameter description starts on a new line:
 * </p>
 *
 * <pre>
 *     &#64;param parameter_name
 *                           parameter_description
 * </pre>
 *
 * @author Kristina Simova - Initial contribution
 *
 */
public class JavadocMethodStyleCheck extends AbstractCheck {

    private static final Pattern CONTAINS_DASH_PATTERN = CommonUtil
            .createPattern("@(throws|exception|param|return){1}\\s+(\\w+)?\\s*\\-+");

    private static final Pattern DESCRIPTION_ON_NEW_LINE_PATTERN = CommonUtil.createPattern(
            "@(throws|exception|param|return){1}\\s*(\\w+)?\\s*\\-*\\s*(\\*{1}(\\s*\\w+)+)", Pattern.MULTILINE);

    private static final Pattern TAG_PATTERN = CommonUtil.createPattern("@(throws|exception|param|return){1}");

    private static final String MESSAGE_DASH_BETWEEN_PARAM_NAME_DESCRIPTION = "There should be no dash between the parameter name and the description in a Javadoc comment of a method or constructor.";
    private static final String MESSAGE_EMPTY_LINE_BETWEEN_TAGS = "There should be no empty lines between tags in a Javadoc comment of a method or constructor.";
    private static final String MESSAGE_PARAMETER_DESCRIPTION_NEW_LINE = "The parameter description in a Javadoc comment of a method or constructor should not start on a new line.";

    /**
     * Matches whole line with dash between parameter name and parameter description
     */
    private static final int GROUP_DASH = 0;

    /**
     * Matches the whole new line with description
     */
    private static final int GROUP_NEW_LINE = 3;

    private Map<Integer, String> lineNumberToLineText;
    private List<Integer> tagLines;

    /**
     * Controls whether to allow missing Javadoc on accessor methods for
     * properties (setters and getters). It is a configuration property
     * and can be changed through the check's configuration. By default
     * is true.
     */
    private boolean allowMissingPropertyJavadoc;

    /**
     * Controls whether to ignore if there is no javadoc for a property accessor (setter/getter methods). It is a
     * configuration property and can be changed through the check's configuration. By default is true.
     *
     * @param allowMissingPropertyJavadoc a boolean value that controls whether to ignore missing javadoc
     */
    public void setAllowMissingPropertyJavadoc(boolean allowMissingPropertyJavadoc) {
        this.allowMissingPropertyJavadoc = allowMissingPropertyJavadoc;
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        /**
         * The check will be executed for method and constructor declarations.
         */
        return new int[] { TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        boolean skipCheck = (CheckUtil.isSetterMethod(ast) || CheckUtil.isGetterMethod(ast))
                && allowMissingPropertyJavadoc;
        if (!skipCheck) {
            visit(ast);
        }
    }

    private void visit(DetailAST ast) {
        FileContents contents = getFileContents();
        int methodLineNumber = ast.getLineNo();
        TextBlock textBlock = contents.getJavadocBefore(methodLineNumber);

        if (textBlock != null) {
            String[] text = textBlock.getText();
            lineNumberToLineText = new HashMap<Integer, String>();
            tagLines = new ArrayList<>();

            for (int javadocLineIndex = 0; javadocLineIndex < text.length; javadocLineIndex++) {
                int lineNo = methodLineNumber - text.length + javadocLineIndex;
                String line = text[javadocLineIndex];
                Matcher tagMatcher = TAG_PATTERN.matcher(line);
                if (tagMatcher.find()) {
                    tagLines.add(javadocLineIndex);
                }

                lineNumberToLineText.put(lineNo, line);
            }

            checkComment(methodLineNumber, text);
        } else {
            // it should be handled by ExportedJavadocMethodCheck
        }
    }

    /**
     * Checks javadoc comment of a method or constructor for dashes between parameter name and parameter description,
     * parameter description starting on a new line and empty lines between tags.
     *
     * @param methodLineNumber the line number where the method or constructor starts
     * @param javadocComment the javadoc before a method or constructor
     */
    private void checkComment(int methodLineNumber, String[] javadocComment) {
        String joinedText = String.join("\n", javadocComment);
        Matcher containsDashMatcher = CONTAINS_DASH_PATTERN.matcher(joinedText);
        Matcher descriptionNewLineMatcher = DESCRIPTION_ON_NEW_LINE_PATTERN.matcher(joinedText);

        while (containsDashMatcher.find()) {
            String group = containsDashMatcher.group(GROUP_DASH);
            logInformation(group, MESSAGE_DASH_BETWEEN_PARAM_NAME_DESCRIPTION);
        }

        while (descriptionNewLineMatcher.find()) {
            String group = descriptionNewLineMatcher.group(GROUP_NEW_LINE);
            logInformation(group, MESSAGE_PARAMETER_DESCRIPTION_NEW_LINE);
        }

        if (tagLines.size() > 1) {
            int firstTagLine = tagLines.get(0);
            int lastTagLine = tagLines.get(tagLines.size() - 1);

            for (int javadocLineIndex = firstTagLine + 1; javadocLineIndex < lastTagLine; javadocLineIndex++) {
                String commentLine = javadocComment[javadocLineIndex];
                commentLine = commentLine.replace("*", " ").trim();

                if (commentLine.isEmpty()) {
                    int lineNo = methodLineNumber - javadocComment.length + javadocLineIndex;
                    log(lineNo, MESSAGE_EMPTY_LINE_BETWEEN_TAGS);
                }
            }
        }
    }

    /**
     * Iterates over map and logs message where the match is found.
     *
     * @param group the actual text that the regular expression matches
     * @param message the message to log
     */
    private void logInformation(String group, String message) {
        lineNumberToLineText.forEach((lineNumber, lineText) -> {
            if (lineText.contains(group)) {
                log(lineNumber, message);
                return;
            }
        });
    }
}
