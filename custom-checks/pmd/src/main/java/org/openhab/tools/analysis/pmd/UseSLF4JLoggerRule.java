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
package org.openhab.tools.analysis.pmd;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Checks if a logger other than the one provided by slf4j is used
 *
 * @author Lyubomir Papazov - Initial contribution
 *
 */
public class UseSLF4JLoggerRule extends AbstractJavaRule {
    private static final String LOGGER_LITERAL = "Logger";

    private Set<String> forbiddenLoggers = new HashSet<>();
    private boolean isSlf4jPackageImported;

    public UseSLF4JLoggerRule() {
        forbiddenLoggers.add("org.apache.log4j.Logger");
        forbiddenLoggers.add("java.util.logging.Logger");
        forbiddenLoggers.add("ch.qos.logback.classic.Logger");
        forbiddenLoggers.add("org.apache.commons.logging.Log");
    }

    @Override
    public Object visit(ASTImportDeclaration node, Object data) {
        String fullImportName = node.getImportedName();
        if (forbiddenLoggers.contains(fullImportName)) {
            addViolation(data, node);
        } else if ("org.slf4j.Logger".equals(fullImportName)
                || ("org.slf4j".equals(fullImportName) && node.isImportOnDemand())) {
            isSlf4jPackageImported = true;
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTVariableDeclarator node, Object data) {
        ASTType typeNode = node.getParent().getFirstChildOfType(ASTType.class);
        Node reftypeNode = typeNode.getChild(0);
        if (reftypeNode instanceof ASTReferenceType) {
            ASTClassOrInterfaceType classOrInterfaceType = reftypeNode
                    .getFirstChildOfType(ASTClassOrInterfaceType.class);
            if (classOrInterfaceType != null) {
                String className = classOrInterfaceType.getImage();

                if (isClassNameForbidden(className)) {
                    addViolation(data, typeNode);
                }
            }
        }
        return super.visit(node, data);
    }

    private boolean isClassNameForbidden(String className) {
        if (forbiddenLoggers.contains(className)) {
            return true;
        }
        // If the classname is Logger but org.slf4j is not in the imports,
        // that means the current Logger literal is not a sfl4j.Logger
        return LOGGER_LITERAL.equals(className) && !isSlf4jPackageImported;
    }
}
