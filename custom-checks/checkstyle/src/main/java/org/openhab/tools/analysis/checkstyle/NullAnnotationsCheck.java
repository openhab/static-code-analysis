/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.tools.analysis.utils.SatCheckUtils;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Check that generates <b> WARNING </b> if: <br>
 *
 * - Class/Interface/Enum is not annotated with <code>@NonNullByDefault</code> <br>
 *
 * - Return types, parameter types, generic types etc. are annotated with <code>@NonNull</code>, because there is no
 * need for it as it is set as default. Only <code>@Nullable</code> should be used.
 *
 * @author Kristina Simova - Initial contribution
 * @author Fabian Wolter - Add Enum
 */
public class NullAnnotationsCheck extends AbstractCheck {

    private List<String> imports = new ArrayList<String>();

    private static final String NONNULL_ANNOTATION = NonNull.class.getSimpleName();
    private static final String NULLABLE_ANNOTATION = Nullable.class.getSimpleName();
    private static final String NONNULLBYDEFAULT_ANNOTATION = NonNullByDefault.class.getSimpleName();

    private static final String WARNING_MESSAGE_MISSING_ANNOTATION = String
            .format("Classes/Interfaces/Enums should be annotated with @%s", NONNULLBYDEFAULT_ANNOTATION);
    private static final String WARNING_MESSAGE_NONNULL_ANNOTATION = String.format(
            "There is no need for a @%s annotation because it is set as default. Only @%s should be used",
            NONNULL_ANNOTATION, NULLABLE_ANNOTATION);

    /**
     * Indicates whether the inner classes/interfaces/enums (briefly called units) should be checked for a
     * <code>@NonNullByDefault</code>
     * annotation. It is a configuration property and can be changed through the check's configuration.
     */
    private boolean checkInnerUnits;

    public void setCheckInnerUnits(boolean checkInnerUnits) {
        this.checkInnerUnits = checkInnerUnits;
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.IMPORT, TokenTypes.AT, TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        int tokenType = ast.getType();
        switch (tokenType) {
            case TokenTypes.IMPORT:
                String packageImport = CheckUtil.createFullType(ast).getText();
                imports.add(packageImport);
                break;
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
                visit(ast);
                break;
            case TokenTypes.AT:
                checkForNonNullAnnotation(ast);
                break;
        }
    }

    private void visit(DetailAST ast) {
        if (!checkInnerUnits) {
            DetailAST astParent = ast.getParent();
            // if outer class/interface
            if (astParent == null) {
                checkForNonNullByDefaultAnnotation(ast);
            }
        } else {
            checkForNonNullByDefaultAnnotation(ast);
        }
    }

    /**
     * Method that checks for missing <code>@NonNullByDefault</code> annotation before class/interface/enum
     *
     * @param ast the ast
     */
    private void checkForNonNullByDefaultAnnotation(DetailAST ast) {
        DetailAST modifiers = ast.getFirstChild();
        int numberOfAnnotations = modifiers.getChildCount(TokenTypes.ANNOTATION);
        int lineNo = modifiers.getLineNo();
        if (numberOfAnnotations == 0) {
            log(lineNo, WARNING_MESSAGE_MISSING_ANNOTATION);
        } else {
            if (!isAnnotationPresent(modifiers)) {
                log(lineNo + numberOfAnnotations, WARNING_MESSAGE_MISSING_ANNOTATION);
            }
        }
    }

    /**
     * Method that checks if the <code>@NonNullByDefault</code> annotation is present
     *
     * @param ast the ast
     * @return whether the <code>@NonNullByDefault</code> annotation is present or not
     */
    private boolean isAnnotationPresent(DetailAST ast) {
        List<DetailAST> annotations = SatCheckUtils.getAllChildrenNodesOfType(ast, TokenTypes.ANNOTATION);
        for (DetailAST annotationAST : annotations) {
            // first child is '@' (the at-clause) and its sibling is the annotation name we are looking for
            String annotationName = annotationAST.getFirstChild().getNextSibling().getText();
            if (NONNULLBYDEFAULT_ANNOTATION.equals(annotationName)
                    && imports.contains(NonNullByDefault.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that checks if the annotation is <code>@NonNull</code>
     *
     * @param ast the ast
     */
    private void checkForNonNullAnnotation(DetailAST ast) {
        DetailAST atClause = CheckUtil.getFirstNode(ast);
        String annotationName = atClause.getNextSibling().getText();
        if (NONNULL_ANNOTATION.equals(annotationName) && imports.contains(NonNull.class.getName())) {
            log(atClause.getLineNo(), WARNING_MESSAGE_NONNULL_ANNOTATION);
        }
    }
}
