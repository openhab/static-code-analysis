/*
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
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.OptionalFieldCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link OptionalFieldCheck}
 *
 * @author Jacob Laursen - Initial contribution
 */
public class OptionalFieldCheckTest extends AbstractStaticCheckTest {

    @Override
    protected String getPackageLocation() {
        return "checkstyle/optionalFieldCheckTest";
    }

    @Test
    public void testOptionalFieldFromImportDetected() throws Exception {
        final String[] expected = { "4:5: Avoid using Optional as a field type" };
        verify(createModuleConfig(OptionalFieldCheck.class), getPath("OptionalFieldFromImport.java"), expected);
    }

    @Test
    public void testOptionalFieldFromStarImportDetected() throws Exception {
        final String[] expected = { "4:5: Avoid using Optional as a field type" };
        verify(createModuleConfig(OptionalFieldCheck.class), getPath("OptionalFieldFromStarImport.java"), expected);
    }

    @Test
    public void testOptionalFieldFullyQualifiedDetected() throws Exception {
        final String[] expected = { "2:5: Avoid using Optional as a field type" };
        verify(createModuleConfig(OptionalFieldCheck.class), getPath("OptionalFieldFullyQualified.java"), expected);
    }

    @Test
    public void testOptionalFieldFromImportOtherPackageNotDetected() throws Exception {
        verify(createModuleConfig(OptionalFieldCheck.class), getPath("OptionalFieldFromImportOtherPackage.java"),
                CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testOptionalLocalVariableNotDetected() throws Exception {
        verify(createModuleConfig(OptionalFieldCheck.class), getPath("OptionalLocalVariable.java"),
                CommonUtil.EMPTY_STRING_ARRAY);
    }
}
