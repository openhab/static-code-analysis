/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ParameterizedRegexpHeaderCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Tests for {@link ParameterizedRegexpHeaderCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class ParameterizedRegexpHeaderCheckTest extends AbstractStaticCheckTest {
    private static final String MSG_MISMATCH = "Header line doesn''t match pattern {0}";
    private static final String MSG_MISSING = "Header is missing";

    private static final String TEST_DIRECOTRY = "parameterizedRegexpHeaderCheckTest";
    private static final String TEST_JAVADOC_HEADER_PATTERN = "^/\\*\\*$\\n^ \\* Copyright \\(c\\) {0}-{1} by the respective copyright holders\\.$\\n^ \\*$";
    private static final String TEST_JAVA_COMMENT_HEADER_PATTERN = "^//$\\n^// Copyright \\(c\\) {0}-{1} by the respective copyright holders\\.$\\n^//$";
    private static final String TEST_XML_HEADER_PATTERN = "^<!-- Copyright \\(c\\) {0}-{1} by the respective copyright holders\\. -->$";
    private static DefaultConfiguration config;

    @Before
    public void createConfiguration() {
        // The default configuration for the tests
        config = createTestConfiguration(TEST_JAVADOC_HEADER_PATTERN, "2010,2017", null);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = new DefaultConfiguration("root");
        configParent.addChild(config);
        return configParent;
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
        String path = getPath(TEST_DIRECOTRY + File.separator + name);
        verify(createChecker(config), path, messages);
    }

    private DefaultConfiguration createTestConfiguration(String headerPattern, String values, String headerFormat) {
        DefaultConfiguration configuration = createCheckConfig(ParameterizedRegexpHeaderCheck.class);
        if (headerPattern != null) {
            configuration.addAttribute("header", headerPattern);
        }
        if (values != null) {
            configuration.addAttribute("values", values);
        }
        if (headerFormat != null) {
            configuration.addAttribute("headerFormat", headerFormat);
        }
        return configuration;
    }
}
