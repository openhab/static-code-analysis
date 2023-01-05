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

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.AuthorContributionDescriptionCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link AuthorContributionDescriptionCheck}
 *
 * @author Kristina Simova - Initial contribution
 */
public class AuthorContributionDescriptionCheckTest extends AbstractStaticCheckTest {

    private static final String EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION = "First javadoc author should have \"Initial contribution\" contribution description.";
    private static final String EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION = "Javadoc author should not have empty contribution description.";

    /**
     * The needed attributes for the configuration. They should be the same as their
     * corresponding properties defined in rulesets.checkstyle/rules.xml file
     */
    private static final String ATTRIBUTE_REQUIRED_DESCRIPTIONS_NAME = "requiredContributionDescriptions";
    private static final String ATTRIBUTE_REQUIRED_DESCRIPTIONS_VALUE = "Initial contribution";
    private static final String ATTRIBUTE_CHECK_UNITS_NAME = "checkInnerUnits";

    private Map<Integer, String> lineNumberToWarningMessageExpected;

    private DefaultConfiguration configuration;

    @Override
    protected String getPackageLocation() {
        return "checkstyle/authorContributionDescriptionCheckTest";
    }

    @BeforeEach
    public void setUp() {
        lineNumberToWarningMessageExpected = new TreeMap<>();
        configuration = createModuleConfig(AuthorContributionDescriptionCheck.class);
        configuration.addProperty(ATTRIBUTE_REQUIRED_DESCRIPTIONS_NAME, ATTRIBUTE_REQUIRED_DESCRIPTIONS_VALUE);
    }

    /*
     * outer class has no first author contribution description
     */
    @Test
    public void testOuterClassWithNoFirstAuthorContributionDescription() throws Exception {
        String fileName = "NoFirstAuthorContributionDescriptions.java";
        /*
         * a warning message is expected at the line where the first author of the outer class is
         * located, because first author contribution description is missing there
         */
        int warningLine = 2;
        lineNumberToWarningMessageExpected.put(warningLine, EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = false;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer class has no other author contribution description
     */
    @Test
    public void testOuterClassWithNoOtherAuthorContributionDescription() throws Exception {
        String fileName = "NoOtherAuthorContributionDescriptions.java";
        /*
         * a warning message is expected at the line where the other author of the outer class is
         * located, because other author contribution description is missing there
         */
        int warningLine = 3;
        lineNumberToWarningMessageExpected.put(warningLine, EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = false;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * inner class has no first author contribution description
     */
    @Test
    public void testInnerClassWithNoFirstAuthorContributionDescription() throws Exception {
        String fileName = "NoFirstAuthorContributionDescriptionInnerClass.java";
        /*
         * a warning message is expected at the line where the first author of the inner class is
         * located, because first author contribution description is missing there
         */
        int warningLine = 9;
        lineNumberToWarningMessageExpected.put(warningLine, EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * inner class has no other author contribution description
     */
    @Test
    public void testInnerClassWithNoOtherAuthorContributionDescription() throws Exception {
        String fileName = "NoOtherAuthorContributionDescriptionInnerClass.java";
        /*
         * a warning message is expected at the line where the other author of the inner class is
         * located, because other author contribution description is missing there
         */
        int warningLine = 10;
        lineNumberToWarningMessageExpected.put(warningLine, EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer and inner classes with no first author contribution description
     */
    @Test
    public void testOuterAndInnerClassesWithNoFirstAuthorContributionDescription() throws Exception {
        String fileName = "NoFirstAuthorContributionDescriptions.java";
        /*
         * warning messages are expected at the lines where first authors of outer and inner
         * classes are located, because first author contribution descriptions are missing there
         */
        int firstWarningLineFirstAuthor = 2;
        int secondWarningLineFirstAuthor = 9;
        lineNumberToWarningMessageExpected.put(firstWarningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(secondWarningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer and inner classes with no other author contribution description
     */
    @Test
    public void testOuterAndInnerClassesWithNoOtherAuthorContributionDescription() throws Exception {
        String fileName = "NoOtherAuthorContributionDescriptions.java";
        /*
         * warning messages are expected at the lines where other authors of outer and inner
         * classes are located, because other author contribution descriptions are missing there
         */
        int firstWarningLineOthertAuthor = 3;
        int secondWarningLineOtherAuthor = 10;
        lineNumberToWarningMessageExpected.put(firstWarningLineOthertAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(secondWarningLineOtherAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer class with no first author contribution description and
     * inner class with no other author contribution description
     */
    @Test
    public void testOuterClassWithNoFirstAuthorContributionDescriptionAndInnerClassWithNoOtherAuthorContributionDescription()
            throws Exception {
        String fileName = "NoContributionDescriptionFirstAuthorOtherAuthor.java";
        /*
         * warning messages are expected at the lines where the first author of the outer class and other
         * author of the inner class are located, because author contribution descriptions are missing there
         */
        int warningLineFirstAuthor = 2;
        int warningLineOtherAuthor = 10;
        lineNumberToWarningMessageExpected.put(warningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(warningLineOtherAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer class with no other author contribution description and
     * inner class with no first author contribution description
     */
    @Test
    public void testOuterClassWithNoOtherAuthorContributionDescriptionAndInnerClassWithNoFirstAuthorContributionDescription()
            throws Exception {
        String fileName = "NoContributionDescriptionOtherAuthorFirstAuthor.java";
        /*
         * warning messages are expected at the lines where the other author of the outer class and first
         * author of the inner class are located, because author contribution descriptions are missing there
         */
        int warningLineFirstAuthor = 9;
        int warningLineOtherAuthor = 3;
        lineNumberToWarningMessageExpected.put(warningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(warningLineOtherAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer class with wrong first author contribution description
     */
    @Test
    public void testOuterClassWithWrongFirstAuthorContributionDescription() throws Exception {
        String fileName = "WrongFirstAuthorContributionDescription.java";
        /*
         * warning message is expected at the line where the first author of outer class
         * is located, because author contribution description is wrong
         */
        int warningLineFirstAuthor = 2;
        lineNumberToWarningMessageExpected.put(warningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = false;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer class with wrong other author contribution description - empty
     */
    @Test
    public void testOuterClassWithWrongOtherAuthorContributionDescription() throws Exception {
        String fileName = "WrongOtherAuthorContributionDescription.java";
        /*
         * warning message is expected at the line where the other author of outer class
         * is located, because author contribution description is wrong
         */
        int warningLineOtherAuthor = 3;
        lineNumberToWarningMessageExpected.put(warningLineOtherAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = false;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer and inner classes with no author contribution descriptions
     */
    @Test
    public void testOuterAndInnerClassesWithNoAuthorContributionDescriptions() throws Exception {
        String fileName = "NoContributionDescriptions.java";
        /*
         * warning messages are expected at the lines where all the authors of outer and inner classes
         * are located, because author contribution descriptions are missing there
         */
        int firstWarningLineFirstAuthor = 2;
        int secondWarningLineFirstAuthor = 9;
        int firstWarningLineOtherAuthor = 3;
        int secondWarningLineOtherAuthor = 10;
        lineNumberToWarningMessageExpected.put(firstWarningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(secondWarningLineFirstAuthor,
                EXPECTED_WARNING_MESSAGE_FIRST_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(firstWarningLineOtherAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        lineNumberToWarningMessageExpected.put(secondWarningLineOtherAuthor,
                EXPECTED_WARNING_MESSAGE_OTHER_AUTHOR_DESCRIPTION);
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    /*
     * outer and inner classes with all author contribution descriptions
     */
    @Test
    public void testOuterAndInnerClassesWithAuthorContributionDescriptions() throws Exception {
        String fileName = "PresentContributionDescriptions.java";
        /*
         * no warnings are expected so we pass an empty map
         */
        boolean checkInnerClasses = true;
        checkFileForAuthorContributionDescription(checkInnerClasses, fileName);
    }

    private void checkFileForAuthorContributionDescription(boolean checkInnerUnits, String fileName) throws Exception {
        String filePath = getPath(fileName);
        String[] expected = null;

        if (lineNumberToWarningMessageExpected.isEmpty()) {
            expected = CommonUtil.EMPTY_STRING_ARRAY;
        } else {
            expected = lineNumberToWarningMessageExpected.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .toArray(size -> new String[lineNumberToWarningMessageExpected.size()]);
        }

        configuration.addProperty(ATTRIBUTE_CHECK_UNITS_NAME, String.valueOf(checkInnerUnits));
        verify(configuration, filePath, expected);
    }
}
