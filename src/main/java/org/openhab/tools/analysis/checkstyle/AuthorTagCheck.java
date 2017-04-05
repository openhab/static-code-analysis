/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
