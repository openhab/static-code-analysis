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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.javadoc.WriteTagCheck;

/**
 * Checks if a class/interface/enumeration has an author tag
 *
 * @author Mihaela Memova
 *
 */
public class AuthorTagCheck extends WriteTagCheck {

    /**
     * Indicates whether the inner classes/interfaces/enumerations (briefly
     * called units) should be checked for an author tag. It is a configuration
     * property and can be changed through the check's configuration.
     */
    private boolean checkInnerUnits;

    public void setCheckInnerUnits(boolean checkInnerUnits) {
        this.checkInnerUnits = checkInnerUnits;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calls the {@link WriteTagCheck#visitToken(DetailAST)} taking into
     * consideration the check configuration
     */
    @Override
    public void visitToken(DetailAST ast) {
        if (!checkInnerUnits) {
            DetailAST astParent = ast.getParent();
            // if outer class/interface/enum
            if (astParent == null) {
                super.visitToken(ast);
            }
        } else {
            super.visitToken(ast);
        }
    }
}
