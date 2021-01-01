/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocTypeCheck;

/**
 * Provides a filter that determines whether to check the inner units for a
 * javadoc.
 *
 * @author Petar Valchev - Initial contribution
 */
public class MissingJavadocFilterCheck extends MissingJavadocTypeCheck {
    private boolean checkInnerUnits = false;

    // A configuration property that determines whether to check the inner units
    public void setCheckInnerUnits(boolean checkInnerUnits) {
        this.checkInnerUnits = checkInnerUnits;
    }

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
