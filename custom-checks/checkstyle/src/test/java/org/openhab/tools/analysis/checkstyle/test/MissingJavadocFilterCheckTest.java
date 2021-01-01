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
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.MissingJavadocFilterCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocStyleCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link JavadocInnerClassesFilterCheck}
 *
 * @author Petar Valchev - Initial contribution
 */
public class MissingJavadocFilterCheckTest extends AbstractStaticCheckTest {

    @Override
    protected String getPackageLocation() {
        return "checkstyle/missingJavadocFilterCheckTest";
    }

    @Test
    public void testOuterClassWithJavadoc() throws Exception {
        int[] lineNumbers = new int[] { 10 };
        verifyJavadoc("MissingJavadocOuterAndInnnerClass.java", false, lineNumbers);
    }

    @Test
    public void testOuterAndInnerClassesWithJavadoc() throws Exception {
        int[] lineNumbers = new int[] { 10, 12 };
        verifyJavadoc("MissingJavadocOuterAndInnnerClass.java", true, lineNumbers);
    }

    @Test
    public void testOuterClassWithPresentJavadoc() throws Exception {
        verifyJavadoc("PresentJavadocOuterAndInnerClass.java", false, null);
    }

    @Test
    public void testOuterAndInnerClassesWithPresentJavadoc() throws Exception {
        verifyJavadoc("PresentJavadocOuterAndInnerClass.java", true, null);
    }

    private void verifyJavadoc(String testFileName, boolean checkInnerClasses, int[] lineNumbers) throws Exception {
        DefaultConfiguration config = createModuleConfig(MissingJavadocFilterCheck.class);
        String checkInnerClassesPropertyName = "checkInnerUnits";
        config.addAttribute(checkInnerClassesPropertyName, String.valueOf(checkInnerClasses));

        String filePath = getPath(testFileName);

        String[] expectedMessages = null;
        if (lineNumbers != null) {
            expectedMessages = new String[lineNumbers.length];
            for (int i = 0; i < lineNumbers.length; i++) {
                expectedMessages[i] = lineNumbers[i] + ": " + JavadocStyleCheck.MSG_JAVADOC_MISSING;
            }
        } else {
            expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        }

        verify(config, filePath, expectedMessages);
    }
}
