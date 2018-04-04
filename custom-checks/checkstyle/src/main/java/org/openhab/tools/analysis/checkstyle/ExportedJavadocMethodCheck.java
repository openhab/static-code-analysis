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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.AccessModifier;
import com.puppycrawl.tools.checkstyle.utils.CheckUtils;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Deactivates the JavadocMethodCheck for internal packages and enables it for
 * exported packages.
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ExportedJavadocMethodCheck extends AbstractCheck {
    private String packageDeclaration;

    public void beginTree(DetailAST root) {
        this.packageDeclaration = SatCheckUtils.getPackageDeclaration(root);
        super.beginTree(root);
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtils.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        String modifier = CheckUtils.getAccessModifierFromModifiersToken(ast.getFirstChild()).toString();
        if (this.packageDeclaration.contains(CheckConstants.INTERNAL_PACKAGE) || "private".equals(modifier)
                || CheckUtils.isGetterMethod(ast) || CheckUtils.isSetterMethod(ast)) {
            return;
        }

        int lineNumber = ast.getLineNo();
        FileContents fileContents = this.getFileContents();
        if (fileContents.getLine(lineNumber - 1).contains("@Override")) {
            return;
        }

        if (fileContents.getJavadocBefore(lineNumber) == null) {
            String methodName = ast.getFirstChild().getNextSibling().getNextSibling().getText();
            log(lineNumber, "Missing javadoc for method " + methodName);
        }
    }
}
