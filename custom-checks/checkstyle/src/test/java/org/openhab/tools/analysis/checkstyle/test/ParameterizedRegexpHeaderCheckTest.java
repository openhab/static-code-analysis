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

import java.text.MessageFormat;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.ParameterizedRegexpHeaderCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link ParameterizedRegexpHeaderCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class ParameterizedRegexpHeaderCheckTest extends AbstractStaticCheckTest {
    private static final String MSG_MISMATCH = "Header line doesn''t match pattern {0}";
    private static final String MSG_MISSING = "Header is missing";

    private static final String TEST_JAVADOC_HEADER_PATTERN = "^/\\*\\*$\\n^ \\* Copyright \\(c\\) {0}-{1} by the respective copyright holders\\.$\\n^ \\*$";
    private static final String TEST_JAVA_COMMENT_HEADER_PATTERN = "^//$\\n^// Copyright \\(c\\) {0}-{1} by the respective copyright holders\\.$\\n^//$";
    private static final String TEST_XML_HEADER_PATTERN = "^<!-- Copyright \\(c\\) {0}-{1} by the respective copyright holders\\. -->$";
    private static DefaultConfiguration config;

    @BeforeEach
    public void createConfiguration() {
        // The default configuration for the tests
        config = createTestConfiguration(TEST_JAVADOC_HEADER_PATTERN, "2010,2017", null);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/parameterizedRegexpHeaderCheckTest";
    }

    @Test
    public void testValidHeader() throws Exception {
        verifyJavaFileNoErrors("ValidHeaderJavaFile.java");
    }

    @Test
    public void testHeaderWithWrongYear() throws Exception {
        String[] expectedMessages = generateExpectedMessages(2, MessageFormat.format(MSG_MISMATCH,
                "^ \\* Copyright \\(c\\) 2010-2017 by the respective copyright holders\\.$"));
        verifyJavaFile("HeaderWithWrongYear.java", expectedMessages);
    }

    @Test
    public void testEmptyFile() throws Exception {
        String[] expectedMessages = generateExpectedMessages(1, MSG_MISSING);
        verifyJavaFile("EmptyFile.java", expectedMessages);
    }

    @Test
    public void testMissingHeader() throws Exception {
        String[] expectedMessages = generateExpectedMessages(1, MSG_MISSING);
        verifyJavaFile("MissingHeader.java", expectedMessages);
    }

    @Test
    public void testCustomHeaderFormat() throws Exception {
        config = createTestConfiguration(TEST_JAVA_COMMENT_HEADER_PATTERN, "2010,2017", "//");
        verifyJavaFileNoErrors("CustomHeaderFormat.java");
    }

    @Test
    public void testValidXmlHeader() throws Exception {
        config = createTestConfiguration(TEST_XML_HEADER_PATTERN, "2010,2017", null);
        verifyJavaFileNoErrors("ValidHeaderXmlFile.xml");
    }

    private void verifyJavaFileNoErrors(String name) throws Exception {
        verifyJavaFile(name, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private void verifyJavaFile(String name, String[] messages) throws Exception {
        String path = getPath(name);
        verify(createChecker(config), path, messages);
    }

    private DefaultConfiguration createTestConfiguration(String headerPattern, String values, String headerFormat) {
        DefaultConfiguration configuration = createModuleConfig(ParameterizedRegexpHeaderCheck.class);
        if (headerPattern != null) {
            configuration.addProperty("header", headerPattern);
        }
        if (values != null) {
            configuration.addProperty("values", values);
        }
        if (headerFormat != null) {
            configuration.addProperty("headerFormat", headerFormat);
        }
        return configuration;
    }
}
