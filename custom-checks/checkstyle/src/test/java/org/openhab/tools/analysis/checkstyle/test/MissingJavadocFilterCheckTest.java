/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.tools.analysis.checkstyle.MissingJavadocFilterCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocTypeCheck;
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
        String[] locations = new String[] { "10:1" };
        verifyJavadoc("MissingJavadocOuterAndInnnerClass.java", false, locations);
    }

    @Test
    public void testOuterAndInnerClassesWithJavadoc() throws Exception {
        String[] locations = new String[] { "10:1", "12:5" };
        verifyJavadoc("MissingJavadocOuterAndInnnerClass.java", true, locations);
    }

    @Test
    public void testOuterClassWithPresentJavadoc() throws Exception {
        verifyJavadoc("PresentJavadocOuterAndInnerClass.java", false, null);
    }

    @Test
    public void testOuterAndInnerClassesWithPresentJavadoc() throws Exception {
        verifyJavadoc("PresentJavadocOuterAndInnerClass.java", true, null);
    }

    private void verifyJavadoc(String testFileName, boolean checkInnerClasses, String[] locations) throws Exception {
        DefaultConfiguration config = createModuleConfig(MissingJavadocFilterCheck.class);
        String checkInnerClassesPropertyName = "checkInnerUnits";
        config.addProperty(checkInnerClassesPropertyName, String.valueOf(checkInnerClasses));

        String filePath = getPath(testFileName);

        String[] expectedMessages = null;
        if (locations != null) {
            expectedMessages = new String[locations.length];
            for (int i = 0; i < locations.length; i++) {
                expectedMessages[i] = locations[i] + ": " + MissingJavadocTypeCheck.MSG_JAVADOC_MISSING;
            }
        } else {
            expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        }

        verify(config, filePath, expectedMessages);
    }
}
