/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Check for unnecessary inheritDoc - As Javadoc is inherited by default from an interface definition if nothing else is
 * specified, this is redundant and should be avoided. inheritDoc should only be used if some additional
 * documentation is added to it on a specific method.
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class InheritDocCheck extends AbstractCheck {
    private static final String INHERIT_DOC = "{@inheritDoc}";
    private static final String LOG_MESSAGE = "Remove unnecessary inherit doc";

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.BLOCK_COMMENT_BEGIN };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtils.EMPTY_INT_ARRAY;
    }

    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST javadocContentAST = ast.findFirstToken(TokenTypes.COMMENT_CONTENT);
        String javadocContent = javadocContentAST.getText();
        if (javadocContent.contains(INHERIT_DOC)) {
            javadocContent = javadocContent.replace("*", "").trim();
            if (INHERIT_DOC.equals(javadocContent)) {
                log(javadocContentAST.getLineNo(), LOG_MESSAGE);
            }
        }
    }
}
