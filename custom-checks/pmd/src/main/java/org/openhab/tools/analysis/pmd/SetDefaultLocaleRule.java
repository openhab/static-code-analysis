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
package org.openhab.tools.analysis.pmd;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher;

/**
 * Checks if the default locale is being set.
 *
 * @author Ravi Nadahar - Initial contribution
 */
public class SetDefaultLocaleRule extends AbstractJavaRulechainRule {

    private static final List<InvocationMatcher> METHODS = List.of( //
            InvocationMatcher.parse("java.util.Locale#setDefault(java.util.Locale)"), //
            InvocationMatcher.parse("java.util.Locale#setDefault(java.util.Locale.Category,java.util.Locale)") //
    );

    public SetDefaultLocaleRule() {
        super(ASTConstructorCall.class, ASTMethodCall.class);
    }

    @Override
    public String getDescription() {
        return "Applications should not set the default Locale since it affects the whole JVM.";
    }

    @Override
    public Object visit(ASTConstructorCall node, Object data) {
        checkInvocation(node, data);
        return data;
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        checkInvocation(node, data);
        return data;
    }

    private void checkInvocation(JavaNode node, Object data) {
        for (InvocationMatcher matcher : METHODS) {
            if (matcher.matchesCall(node)) {
                asCtx(data).addViolationWithPosition(node, node.getBeginLine(), node.getEndLine(),
                        "Avoid setting the default Locale: {0}", node.getText());
            }
        }
    }
}
