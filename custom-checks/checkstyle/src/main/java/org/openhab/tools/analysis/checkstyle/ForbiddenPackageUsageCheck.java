/**
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
package org.openhab.tools.analysis.checkstyle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * A check that verifies that there are no forbidden packages in use.
 *
 * @author Velin Yordanov - Initial contribution
 */
public class ForbiddenPackageUsageCheck extends AbstractCheck {
    private static final String MESSAGE = "The package %s should not be used.";
    private Collection<String> forbiddenPackages;
    private Collection<String> exceptions;
    private Map<String, Integer> importsToLineNumbers = new HashMap<>();

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.IMPORT };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    /**
     * Sets a configuration property that sets all the packages that need to be
     * avoided.
     *
     * @param value
     *            The value of the forbiddenPackages array that we want to set
     */
    public void setForbiddenPackages(String[] value) {
        forbiddenPackages = Arrays.asList(value);
    }

    /**
     * Sets a configuration property that sets exceptions to the forbidden packages.
     * Usable when we want to ban all subpackages but one.
     *
     * @param value
     *            - The value of the exceptions array that we want to set
     */
    public void setExceptions(String[] value) {
        exceptions = Arrays.asList(value);
    }

    @Override
    public void visitToken(DetailAST ast) {
        importsToLineNumbers.put(FullIdent.createFullIdent(ast.getFirstChild()).getText(), ast.getLineNo());
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        importsToLineNumbers.clear();
    }

    @Override
    public void finishTree(DetailAST ast) {
        importsToLineNumbers.entrySet().stream()
                .filter(entry -> forbiddenPackages.stream().anyMatch(entry.getKey()::contains))
                .filter(entry -> exceptions.stream().noneMatch(entry.getKey()::contains))
                .forEach(entry -> log(entry.getValue(), String.format(MESSAGE, entry.getKey())));
    }
}
