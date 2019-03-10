/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.CLASSPATH_FILE_NAME;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.MavenPomderivedInClasspathCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link MavenPomderivedInClasspathCheck}
 *
 * @author Aleksandar Kovachev
 *
 */
public class MavenPomderivedInClasspathCheckTest extends AbstractStaticCheckTest {
    private static DefaultConfiguration config;

    @BeforeClass
    public static void createConfiguration() {
        config = createModuleConfig(MavenPomderivedInClasspathCheck.class);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/mavenPomDerivedInClasspathCheckTest";
    }

    @Test
    public void testEmptyClassPath() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber, "The .classpath file should not be empty.");
        verifyClasspath("emptyClasspath", expectedMessages);
    }

    @Test
    public void testValidClasspathConfigurationTest() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyClasspath("validClasspathConfiguration", expectedMessages);
    }

    @Test
    public void testMissingPomderivedAttributeInClassPath() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyClasspath("missingPomderivedAttributeInClassPath", expectedMessages);
    }

    @Test
    public void testInvalidClasspathConfiguration() throws Exception {
        int lineNumber = 7;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "The classpath file contains maven.pomderived attribute. "
                        + "This attribute should be used only if you have problems downloading your maven dependencies.");
        verifyClasspath("invalidClasspathConfiguration", expectedMessages);
    }

    private void verifyClasspath(String classpathDirectoryName, String[] expectedMessages) throws Exception {
        String pomXmlAbsolutePath = getPath(classpathDirectoryName + File.separator + CLASSPATH_FILE_NAME);
        verify(config, pomXmlAbsolutePath, expectedMessages);
    }
}
