/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Set;

import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

/**
 * Provides a filter that determines whether to check the inner units for a
 * javadoc.
 *
 * Based on Checkstyle's MissingJavadocTypeCheck, which cannot be subclassed
 * anymore since Checkstyle 14.
 *
 * @author Petar Valchev - Initial contribution
 */
@FileStatefulCheck
public class MissingJavadocFilterCheck extends AbstractCheck {

    public static final String MSG_JAVADOC_MISSING = "javadoc.missing";

    private final List<DetailAST> javadocComments = new ArrayList<>();

    private boolean checkInnerUnits = false;
    private Scope scope = Scope.PUBLIC;
    private Scope excludeScope;
    private Set<String> skipAnnotations = Set.of("Generated");

    // A configuration property that determines whether to check the inner units
    public void setCheckInnerUnits(boolean checkInnerUnits) {
        this.checkInnerUnits = checkInnerUnits;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setExcludeScope(Scope excludeScope) {
        this.excludeScope = excludeScope;
    }

    public void setSkipAnnotations(String... userAnnotations) {
        skipAnnotations = Set.of(userAnnotations);
    }

    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
                TokenTypes.ANNOTATION_DEF, TokenTypes.RECORD_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        javadocComments.clear();
        collectJavadocComments(rootAST);
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!checkInnerUnits) {
            DetailAST astParent = ast.getParent();
            // if not outer class/interface/enum
            if (astParent != null && astParent.getType() != TokenTypes.COMPILATION_UNIT) {
                return;
            }
        }
        if (shouldCheck(ast) && !hasJavadoc(ast)) {
            log(ast, MSG_JAVADOC_MISSING);
        }
    }

    private void collectJavadocComments(DetailAST rootAST) {
        DetailAST current = rootAST;
        while (current != null) {
            if (current.getType() == TokenTypes.BLOCK_COMMENT_BEGIN && JavadocUtil.isJavadocComment(current)) {
                javadocComments.add(current);
            }
            if (current.getFirstChild() != null) {
                current = current.getFirstChild();
            } else {
                DetailAST parent = current;
                while (parent != null && current.getNextSibling() == null) {
                    current = parent;
                    parent = parent.getParent();
                }
                current = current.getNextSibling();
            }
        }
    }

    private boolean shouldCheck(DetailAST ast) {
        Scope surroundingScope = ScopeUtil.getSurroundingScope(ast).orElse(null);
        return surroundingScope != null && surroundingScope.isIn(scope)
                && (excludeScope == null || !surroundingScope.isIn(excludeScope))
                && !AnnotationUtil.containsAnnotation(ast, skipAnnotations);
    }

    private boolean hasJavadoc(DetailAST ast) {
        DetailAST closestJavadoc = null;
        for (DetailAST comment : javadocComments) {
            if (comment.getLineNo() <= ast.getLineNo()) {
                closestJavadoc = comment;
            }
        }
        return closestJavadoc != null && noInterveningCode(closestJavadoc, ast);
    }

    private static boolean noInterveningCode(DetailAST javadoc, DetailAST type) {
        int typeStartLine = type.getLineNo();
        for (DetailAST node = javadoc; node != null; node = node.getNextSibling()) {
            if (node.getLineNo() < typeStartLine && node.getType() != TokenTypes.BLOCK_COMMENT_BEGIN
                    && node.getType() != TokenTypes.SINGLE_LINE_COMMENT) {
                return false;
            }
        }
        return true;
    }
}
