/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Checks if there is proper contribution description of the first javadoc author and
 * random(just not empty) contribution description of every other javadoc author in
 * class/interface/enumeration and generates a warning if they are missing
 *
 * @author Kristina Simova - Initial contribution
 *
 */
public class AuthorContributionDescriptionCheck extends AbstractCheck {

    /**
     * Indicates whether the inner classes/interfaces/enumerations (briefly
     * called units) should be checked for an author tag. It is a configuration
     * property and can be changed through the check's configuration.
     */
    private boolean checkInnerUnits;

    private static final String AUTHOR_TAG = "@author";
    private static final String WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION = "Javadoc author should not have empty contribution description.";
    private static final String WARNING_MESSAGE_PREFIX = "First javadoc author should have \"";
    private static final String WARNING_MESSAGE_SUFFIX = "\" contribution description.";
    private static final String WARNING_MESSAGE_DELIMITER = "\", \"";

    /**
     * A list of contribution descriptions of the first javadoc author. It is a
     * configuration property and can be changed through the check's configuration.
     */
    private List<String> requiredContributionDescriptions;

    private String warningMessageFirstAuthorDescription;

    /**
     * We split a commentLine with an author tag by space(" "). Minimum possible length of the array
     * is 4 as we have: "*", "@author", "AUTHOR_FIRST_NAME", "AUTHOR_LAST_NAME". If length is
     * less than 4 or 4 then the javadoc author has no contribution description.
     */
    private static final int MIN_TOKENS_BY_WHITESPACE = 4;

    /**
     * We split a commentLine with an author tag by " - ". We get at least 2 Strings as we have "* @author
     * AUTHOR_FIRST_NAME AUTHOR_LAST_NAME" as first String and "any contribution description" as second String. if
     * length is less than 2 then the javadoc author has no contribution description.
     */
    private static final int MIN_TOKENS_BY_DASH = 2;

    /**
     * After splitting a commentLine with an author tag by " - " the position of the contribution
     * description is 1.
     */
    private static final int CONTRIBUTION_DESCRIPTION_POSITION = 1;

    public void setCheckInnerUnits(boolean checkInnerUnits) {
        this.checkInnerUnits = checkInnerUnits;
    }

    public void setRequiredContributionDescriptions(String[] contributionDescriptions) {
        this.requiredContributionDescriptions = new ArrayList<String>(Arrays.asList(contributionDescriptions));
        setWarningMessageFirstAuthorDescription(requiredContributionDescriptions);
    }

    /**
     * A method that sets the warning message if proper author contribution description
     * of the first javadoc author is missing. As we get the required contribution
     * descriptions of the first javadoc author from the check's configuration, we
     * have to set the warning message with given below prefix, suffix and proper
     * delimiter
     *
     * @param contributionDescriptions the list with possible
     *            contribution descriptions of the first javadoc author
     */
    private void setWarningMessageFirstAuthorDescription(List<String> contributionDescriptions) {
        warningMessageFirstAuthorDescription = contributionDescriptions.stream().map(Object::toString)
                .collect(Collectors.joining(WARNING_MESSAGE_DELIMITER, WARNING_MESSAGE_PREFIX, WARNING_MESSAGE_SUFFIX))
                .toString();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!checkInnerUnits) {
            DetailAST astParent = ast.getParent();
            if (astParent == null) {
                visit(ast);
            }
        } else {
            visit(ast);
        }
    }

    public void visit(DetailAST ast) {
        FileContents contents = getFileContents();
        int typeDefinitionLineNumber = ast.getLineNo();
        TextBlock textBlock = contents.getJavadocBefore(typeDefinitionLineNumber);
        if (textBlock != null) {
            checkIfAuthorTagHasDescription(typeDefinitionLineNumber, textBlock.getText());
        } else {
            // it should be handled by AuthorTagCheck
        }
    }

    /**
     * Checks if an author tag has contribution description
     *
     * @param typeDefinitionLineNumber the line number where type definition starts
     * @param javadocComment the JavaDoc comment for the type definition.
     */
    private void checkIfAuthorTagHasDescription(int typeDefinitionLineNumber, String... javadocComment) {
        int authorTagCount = 0;
        for (int javadocLineIndex = 0; javadocLineIndex < javadocComment.length; javadocLineIndex++) {
            String commentLine = javadocComment[javadocLineIndex];
            if (commentLine.contains(AUTHOR_TAG)) {
                int authorTagLineNumber = typeDefinitionLineNumber - javadocComment.length + javadocLineIndex;
                authorTagCount += 1;
                // check first author contribution message
                if (authorTagCount == 1) {
                    checkAuthorContributionDescription(authorTagLineNumber, commentLine, true,
                            warningMessageFirstAuthorDescription);
                } else {
                    checkAuthorContributionDescription(authorTagLineNumber, commentLine, false,
                            WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
                }
            }
        }
    }

    /**
     * Checks for contribution description of javadoc author
     *
     * @param authorTagLineNumber the line number where the author tag is located
     * @param commentLine the whole line of comment in which the author tag is located
     * @param isFirstAuthor indicates whether this is the first author of the current javadoc comment so that it can
     *            generate a proper warning message
     * @param warningMessage the warning message based on whether we call this method
     *            on the first author or on any other author of the current javadoc comment
     */
    private void checkAuthorContributionDescription(int authorTagLineNumber, String commentLine, boolean isFirstAuthor,
            String warningMessage) {
        String[] wordsInLine = commentLine.split(" ");
        if (wordsInLine.length < MIN_TOKENS_BY_WHITESPACE || !(commentLine.contains(" - "))) {
            log(authorTagLineNumber, warningMessage);
        } else {
            String[] linePartsByDash = commentLine.split(" - ");
            if (isFirstAuthor) {
                checkFirstAuthorContributionDescription(linePartsByDash, authorTagLineNumber, warningMessage);
            } else {
                checkOtherAuthorContributionDescription(linePartsByDash, authorTagLineNumber, warningMessage);
            }
        }
    }

    /**
     * Checks if the first author of the current javadoc comment has proper contribution
     * description
     *
     * @param linePartsByDash array of String that we get after splitting by " - "
     * @param authorTagLineNumber the line number where the author tag is located
     * @param warningMessage the warning message to generate if proper contribution
     *            description of the first author of the current javadoc comment is missing
     */
    private void checkFirstAuthorContributionDescription(String[] linePartsByDash, int authorTagLineNumber,
            String warningMessage) {
        String firstAuthorDescription = linePartsByDash[CONTRIBUTION_DESCRIPTION_POSITION].trim();
        boolean isDescriptionValid = false;
        for (String contributionDescription : requiredContributionDescriptions) {
            if (StringUtils.containsIgnoreCase(firstAuthorDescription, contributionDescription)) {
                isDescriptionValid = true;
                break;
            }
        }
        if (!isDescriptionValid) {
            log(authorTagLineNumber, warningMessage);
        }
    }

    /**
     * Checks if any other author of the current javadoc comment has empty contribution
     * description
     *
     * @param linePartsByDash array of String that we get after splitting by " - "
     * @param authorTagLineNumber the line number where the author tag is located
     * @param warningMessage the warning message to generate if the contribution
     *            description of any other author of the current javadoc comment is empty
     */
    private void checkOtherAuthorContributionDescription(String[] linePartsByDash, int authorTagLineNumber,
            String warningMessage) {
        if (linePartsByDash.length < MIN_TOKENS_BY_DASH
                || linePartsByDash[CONTRIBUTION_DESCRIPTION_POSITION].trim().isEmpty()) {
            log(authorTagLineNumber, warningMessage);
        }
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }
}
