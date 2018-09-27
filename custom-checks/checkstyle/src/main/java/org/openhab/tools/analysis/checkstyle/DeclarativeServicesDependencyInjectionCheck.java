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
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;

/**
 * Checks if Declarative Service is used for dependency injection in OSGi. A message is logged if
 * usage of {@code org.osgi.util.tracker.ServiceTracker} or
 * {@code org.osgi.util.tracker.ServiceTrackerCustomizer} is found.
 *
 * @author Svilen Valkanov
 *
 */
public class DeclarativeServicesDependencyInjectionCheck extends AbstractCheck {

    public static final String SERVICE_TRACKER_CLASS_NAME = "org.osgi.util.tracker.ServiceTracker";
    public static final String SERVICE_CUSTOMIZER_CLASS_NAME = "org.osgi.util.tracker.ServiceTrackerCustomizer";

    public static final String MESSAGE_SERVICE_TRACKER_USED = "Avoid using " + getShortName(SERVICE_TRACKER_CLASS_NAME)
            + " for dependency injection, consider using Declarative Services";
    public static final String MESSAGE_SERVICE_CUSTOMIZER_IMPLEMENTED = "Avoid using "
            + getShortName(SERVICE_CUSTOMIZER_CLASS_NAME)
            + " for dependency injection, consider using Declarative Services";

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.VARIABLE_DEF, TokenTypes.CLASS_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.VARIABLE_DEF:
                checkVariable(ast);
                break;
            case TokenTypes.CLASS_DEF:
                checkClass(ast);
                break;
        }
    }

    /**
     * Checks whether a variable is of type {@link #SERVICE_TRACKER_CLASS_NAME} and logs a message
     *
     * @param astNode - the node to check
     */
    private void checkVariable(DetailAST astNode) {
        DetailAST variableTypeNode = astNode.findFirstToken(TokenTypes.TYPE);

        String variableType = getType(variableTypeNode);
        if (equals(variableType, SERVICE_TRACKER_CLASS_NAME)) {
            log(astNode.getLineNo(), MESSAGE_SERVICE_TRACKER_USED, SERVICE_TRACKER_CLASS_NAME);
        }
    }

    /**
     * Checks whether a class implements {@link #SERVICE_CUSTOMIZER_CLASS_NAME} and logs a message
     *
     * @param astNode -the node to check
     */
    private void checkClass(DetailAST astNode) {
        DetailAST implementedInterfaceNode = astNode.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);

        if (implementedInterfaceNode != null) {
            String implementedInterface = getType(implementedInterfaceNode);
            if (equals(implementedInterface, SERVICE_CUSTOMIZER_CLASS_NAME)) {
                log(astNode.getLineNo(), MESSAGE_SERVICE_CUSTOMIZER_IMPLEMENTED, SERVICE_CUSTOMIZER_CLASS_NAME);
            }
        }
    }

    private String getType(DetailAST classNode) {
        FullIdent fullType = CheckUtil.createFullType(classNode);
        return fullType.getText();
    }

    private static String getShortName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    private boolean equals(String className, String otherClassName) {
        String shortClassName = getShortName(className);
        String shortOtherClassName = getShortName(otherClassName);
        return shortClassName.equals(shortOtherClassName);
    }

}
