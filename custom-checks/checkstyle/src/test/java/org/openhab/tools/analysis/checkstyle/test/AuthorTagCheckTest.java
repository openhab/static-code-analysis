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
import org.openhab.tools.analysis.checkstyle.AuthorTagCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link AuthorTagCheck}
 *
 * @author Mihaela Memova - Initial contribution
 */
public class AuthorTagCheckTest extends AbstractStaticCheckTest {

    private static final String EXPECTED_WARNING_MESSAGE = "An author tag is missing";

    @Override
    protected String getPackageLocation() {
        return "checkstyle/authorTagCheckTest";
    }

    @Test
    public void testOuterClassWithNoAuthorTag() throws Exception {
        String fileName = "NoAuthorOuterAndInnerClasses.java";
        /*
         * an error is expected at the line where the outer class is declared
         * in the file
         */
        int warningLine = 4;
        boolean checkInnerClasses = false;
        checkFileForAuthorTags(checkInnerClasses, fileName, warningLine);
    }

    @Test
    public void testOuterAndInnerClassesWithNoAuthorTag() throws Exception {
        String fileName = "NoAuthorOuterAndInnerClasses.java";
        /*
         * errors are expected at the lines where the classes are declared in
         * the file
         */
        int firstWarningLine = 4;
        int secondWarningLine = 9;
        boolean checkInnerClasses = true;
        checkFileForAuthorTags(checkInnerClasses, fileName, firstWarningLine, secondWarningLine);
    }

    @Test
    public void testOuterClasWithNoJavaDoc() throws Exception {
        String fileName = "NoJavaDocOuterAndInnerClasses.java";
        /*
         * an error is expected at the line where the outer class is declared
         * in the file
         */
        int warningLine = 1;
        boolean checkInnerClasses = false;
        checkFileForAuthorTags(checkInnerClasses, fileName, warningLine);
    }

    @Test
    public void testOuterAndInnerClassesWithNoJavaDoc() throws Exception {
        String fileName = "NoJavaDocOuterAndInnerClasses.java";
        /*
         * errors are expected at the lines where the classes are declared in
         * the file
         */
        int firstWarningLine = 1;
        int secondWarningLine = 3;
        boolean checkInnerClasses = true;
        checkFileForAuthorTags(checkInnerClasses, fileName, firstWarningLine, secondWarningLine);
    }

    @Test
    public void testOuterAndInnerClassesWithPresentAuthorTag() throws Exception {
        String fileName = "PresentAuthorTagOuterAndInnerClasses.java";
        boolean checkInnerClasses = true;
        // no errors are expected so we don't pass any warning lines
        checkFileForAuthorTags(checkInnerClasses, fileName);
    }

    private void checkFileForAuthorTags(boolean checkInnerUnits, String fileName, Integer... warningLine)
            throws Exception {
        String filePath = getPath(fileName);
        String[] expected = null;
        if (warningLine.length > 0) {
            expected = new String[warningLine.length];
            for (int i = 0; i < warningLine.length; i++) {
                expected[i] = warningLine[i] + ": " + EXPECTED_WARNING_MESSAGE;
            }
        } else {
            expected = CommonUtil.EMPTY_STRING_ARRAY;
        }

        DefaultConfiguration configuration = createConfiguration(checkInnerUnits);
        verify(configuration, filePath, expected);
    }

    private DefaultConfiguration createConfiguration(boolean checkInnerUnits) {
        DefaultConfiguration configuration = createModuleConfig(AuthorTagCheck.class);
        /*
         * Modify the configuration with the needed attributes and message. They
         * should be the same as their corresponding properties defined in
         * rulesets.checkstyle/rules.xml file
         */
        configuration.addProperty("tag", "@author");
        configuration.addProperty("tagFormat", "\\S");
        configuration.addProperty("tagSeverity", "ignore");
        configuration.addProperty("checkInnerUnits", String.valueOf(checkInnerUnits));
        configuration.addMessage("type.missingTag", EXPECTED_WARNING_MESSAGE);

        return configuration;
    }
}
