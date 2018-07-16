/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import org.openhab.tools.analysis.checkstyle.api.CheckConstants;
import org.openhab.tools.analysis.utils.SatCheckUtils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTypeCheck;

/**
 * Deactivates the JavadocTypeCheck for internal packages and enables it for
 * exported packages.
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ExportedJavadocTypeCheck extends JavadocTypeCheck {
    public void beginTree(DetailAST rootAST) {
        this.setAllowMissingParamTags(true);
        this.setAllowUnknownTags(true);
        String packageDeclaration = SatCheckUtils.getPackageDeclaration(rootAST);
        this.setScope(Scope.PACKAGE);
        if (packageDeclaration.contains(CheckConstants.INTERNAL_PACKAGE)) {
            // This deactivates the check
            this.setScope(Scope.NOTHING);
        }

        super.beginTree(rootAST);
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.ANNOTATION_DEF };
    }
}
