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
import org.openhab.tools.analysis.checkstyle.NullAnnotationsCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link NullAnnotationsCheck}
 *
 * @author Kristina Simova - Initial contribution
 */
public class NullAnnotationsCheckTest extends AbstractStaticCheckTest {

    private static final String EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION = "Classes/Interfaces/Enums should be annotated with @NonNullByDefault";
    private static final String EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION = "There is no need for a @NonNull annotation because it is set as default. Only @Nullable should be used";

    private static final String ATTRIBUTE_NAME = "checkInnerUnits";

    private DefaultConfiguration configuration = createModuleConfig(NullAnnotationsCheck.class);

    @Override
    protected String getPackageLocation() {
        return "checkstyle/nullAnnotationsCheckTest";
    }

    @Test
    public void testClassWithNoAnnotation() throws Exception {
        String fileName = "NotAnnotatedClass.java";
        String[] expectedMessage = generateExpectedMessages(3, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testClassWithProperAnnotation() throws Exception {
        String fileName = "AnnotatedClass.java";
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testInterfaceWithNoAnnotation() throws Exception {
        String fileName = "NotAnnotatedInterface.java";
        String[] expectedMessage = generateExpectedMessages(3, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testEnumWithNoAnnotation() throws Exception {
        String fileName = "NotAnnotatedEnum.java";
        String[] expectedMessage = generateExpectedMessages(3, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testClassWithNonNullAnnotationAboveMethod() throws Exception {
        String fileName = "AboveMethodAnnotation.java";
        String[] expectedMessage = generateExpectedMessages(9, EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testClassWithNonNullAnnotationBeforeVariable() throws Exception {
        String fileName = "BeforeVariableAnnotation.java";
        String[] expectedMessage = generateExpectedMessages(9, EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testMethodArgumentsWithNonNullAnnotation() throws Exception {
        String fileName = "MethodParameterNonNullAnnotation.java";
        String[] expectedMessage = generateExpectedMessages(9, EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testGenericsWithNonNullAnnotation() throws Exception {
        String fileName = "GenericsNonNullAnnotation.java";
        String[] expectedMessage = generateExpectedMessages(14, EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testMethodReturnValueWithNonNullAnnotation() throws Exception {
        String fileName = "MethodReturnValueNonNullAnnotation.java";
        String[] expectedMessage = generateExpectedMessages(10, EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessage);
    }

    @Test
    public void testClassWithoutNoNullByDefaultAnnotationAndConstructorWithNonNullAnnotation() throws Exception {
        String fileName = "DeviceHandler.java";
        String[] expectedMessages = generateExpectedMessages(5, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION, 7,
                EXPECTED_WARNING_MESSAGE_NONNULL_ANNOTATION);
        boolean checkInnerUnits = false;
        checkFile(fileName, checkInnerUnits, expectedMessages);
    }

    @Test
    public void testInnerClassesWithoutNonNullByDefaultAnnotation() throws Exception {
        String fileName = "NotAnnotatedInnerClasses.java";
        String[] expectedMessages = generateExpectedMessages(8, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION, 12,
                EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION);
        boolean checkInnerUnits = true;
        checkFile(fileName, checkInnerUnits, expectedMessages);
    }

    @Test
    public void testNotAnnotatedOuterAndInnerClasses() throws Exception {
        String fileName = "NotAnnotatedClasses.java";
        String[] expectedMessages = generateExpectedMessages(3, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION, 5,
                EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION, 9,
                EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION);
        boolean checkInnerUnits = true;
        checkFile(fileName, checkInnerUnits, expectedMessages);
    }

    @Test
    public void testClassWithMultipleAnnotations() throws Exception {
        String fileName = "SatisfiableResourceFilter.java";
        String[] expectedMessages = generateExpectedMessages(8, EXPECTED_WARNING_MESSAGE_MISSING_CLASS_ANNOTATION);
        checkFile(fileName, false, expectedMessages);
    }

    private void checkFile(String fileName, boolean checkInnerUnits, String... expectedMessages) throws Exception {
        String filePath = getPath(fileName);
        configuration.addProperty(ATTRIBUTE_NAME, String.valueOf(checkInnerUnits));
        verify(configuration, filePath, expectedMessages);
    }
}
