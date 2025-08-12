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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Check that generates <b> WARNING </b> when {@link java.util.Optional} is used as a field type.
 *
 * @author Jacob Laursen - Initial contribution
 */
public class OptionalFieldCheck extends AbstractCheck {

    private static final String WARNING_MESSAGE_OPTIONAL_FIELD_USAGE = "Avoid using Optional as a field type";

    private boolean importedOptional = false;
    private boolean starImportJavaUtil = false;

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.IMPORT, TokenTypes.VARIABLE_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        importedOptional = false;
        starImportJavaUtil = false;
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                handleImport(ast);
                break;
            case TokenTypes.VARIABLE_DEF:
                handleVariableDef(ast);
                break;
            default:
                // No action
        }
    }

    private void handleImport(DetailAST ast) {
        DetailAST dot = ast.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return;
        }

        String importText = flattenName(dot);
        if ("java.util.Optional".equals(importText)) {
            importedOptional = true;
        } else if ("java.util.*".equals(importText)) {
            starImportJavaUtil = true;
        }
    }

    private void handleVariableDef(DetailAST ast) {
        // Ensure this is a field (not a local variable in a method)
        if (!isField(ast)) {
            return;
        }

        DetailAST typeAst = ast.findFirstToken(TokenTypes.TYPE);
        if (typeAst == null) {
            return;
        }

        DetailAST firstChild = typeAst.getFirstChild();
        if (firstChild == null) {
            return;
        }

        String typeName = flattenName(firstChild);

        // Fully qualified
        if ("java.util.Optional".equals(typeName)) {
            log(ast, WARNING_MESSAGE_OPTIONAL_FIELD_USAGE);
            return;
        }

        // Simple name with import
        if ("Optional".equals(typeName) && (importedOptional || starImportJavaUtil)) {
            log(ast, WARNING_MESSAGE_OPTIONAL_FIELD_USAGE);
        }
    }

    private boolean isField(DetailAST variableDefAst) {
        DetailAST parent = variableDefAst.getParent();
        return parent != null && parent.getType() == TokenTypes.OBJBLOCK && parent.getParent() != null
                && parent.getParent().getType() == TokenTypes.CLASS_DEF;
    }

    /**
     * Flatten a DOT/IDENT/STAR subtree into a dotted name, collecting only identifier parts and '*'.
     *
     * Examples:
     * - IDENT -> "Optional"
     * - DOT(java, util, Optional, TYPE_ARGUMENTS) -> "java.util.Optional"
     * - DOT(java, util, STAR) -> "java.util.*"
     * 
     * Example tree for fully qualified:
     *
     * <pre>
     * --DOT -> .
     *    |--DOT -> .
     *        |--IDENT -> java
     *        |--IDENT -> util
     *    |--IDENT -> Optional
     *    |--ARGUMENTS
     * </pre>
     */
    private String flattenName(DetailAST node) {
        List<String> parts = new ArrayList<>();
        collectNameParts(node, parts);
        if (parts.isEmpty()) {
            return "";
        }
        return String.join(".", parts);
    }

    private void collectNameParts(DetailAST node, List<String> parts) {
        if (node == null) {
            return;
        }

        final int type = node.getType();

        if (type == TokenTypes.IDENT) {
            parts.add(node.getText());
            return;
        }

        if (type == TokenTypes.STAR) {
            // Wildcard import
            parts.add("*");
            return;
        }

        if (type == TokenTypes.DOT) {
            // Iterate children in order and collect IDENT / DOT / STAR, ignore TYPE_ARGUMENTS, etc.
            DetailAST child = node.getFirstChild();
            while (child != null) {
                final int childType = child.getType();
                if (childType == TokenTypes.IDENT) {
                    parts.add(child.getText());
                } else if (childType == TokenTypes.DOT) {
                    // Recurse to expand nested package parts
                    collectNameParts(child, parts);
                } else if (childType == TokenTypes.STAR) {
                    parts.add("*");
                } // Else ignore TYPE_ARGUMENTS, TYPE_PARAMETERS, etc.
                child = child.getNextSibling();
            }
        }
    }
}
