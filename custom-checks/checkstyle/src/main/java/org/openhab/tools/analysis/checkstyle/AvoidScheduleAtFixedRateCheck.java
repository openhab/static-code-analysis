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
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Checks the code and generates a warning if the instance method scheduleAtFixedRate
 * of the ScheduledExecutorService interface is used.
 * The preferred method to be used instead is scheduleWithFixedDelay.
 *
 * @author Lyubomir Papazov - Initial Contribution
 *
 */
public class AvoidScheduleAtFixedRateCheck extends AbstractCheck {

    private static final String WARNING_MESSAGE = "For periodically executed jobs that do not require a fixed rate scheduleWithFixedDelay should be preferred over scheduleAtFixedRate.";
    private static final String METHOD_TO_BE_AVOIDED = "scheduleAtFixedRate";

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.METHOD_CALL };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtils.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST fullCall = ast.findFirstToken(TokenTypes.DOT);
        if (fullCall != null) {
            DetailAST methodName = fullCall.getLastChild();
            if (METHOD_TO_BE_AVOIDED.equals(methodName.getText())) {
                log(methodName.getLineNo(), WARNING_MESSAGE);
            }
        }
    }
}
