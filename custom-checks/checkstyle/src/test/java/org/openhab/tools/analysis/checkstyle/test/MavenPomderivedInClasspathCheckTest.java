/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.CLASSPATH_FILE_NAME;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.MavenPomderivedInClasspathCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link MavenPomderivedInClasspathCheck}
 *
 * @author Aleksandar Kovachev
 *
 */
public class MavenPomderivedInClasspathCheckTest extends AbstractStaticCheckTest {
    private static final String TEST_DIRECTORY_NAME = "mavenPomDerivedInClasspathCheckTest";

    private static DefaultConfiguration config;

    @BeforeClass
    public static void createConfiguration() {
        config = createCheckConfig(MavenPomderivedInClasspathCheck.class);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = new DefaultConfiguration("root");
        configParent.addChild(config);
        return configParent;
    }

    @Test
    public void testEmptyClassPath() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber, "The .classpath file should not be empty.");
        verifyClasspath("emptyClasspath", expectedMessages);
    }

    @Test
    public void testValidClasspathConfigurationTest() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyClasspath("validClasspathConfiguration", expectedMessages);
    }

    @Test
    public void testMissingPomderivedAttributeInClassPath() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
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
        String pomXmlAbsolutePath = getPath(
                TEST_DIRECTORY_NAME + File.separator + classpathDirectoryName + File.separator + CLASSPATH_FILE_NAME);
        verify(config, pomXmlAbsolutePath, expectedMessages);
    }

}
