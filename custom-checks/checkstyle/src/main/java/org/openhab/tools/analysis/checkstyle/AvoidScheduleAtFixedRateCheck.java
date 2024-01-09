/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Checks the code and generates a warning if the instance method scheduleAtFixedRate
 * of the ScheduledExecutorService interface is used.
 * The preferred method to be used instead is scheduleWithFixedDelay.
 *
 * @author Lyubomir Papazov - Initial contribution
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
        return CommonUtil.EMPTY_INT_ARRAY;
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
