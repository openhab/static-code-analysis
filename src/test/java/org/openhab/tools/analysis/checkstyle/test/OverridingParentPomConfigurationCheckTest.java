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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.OverridingParentPomConfigurationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link OverridingParentPomConfigurationCheck}
 *
 * @author Aleksandar Kovachev
 *
 */
public class OverridingParentPomConfigurationCheckTest extends AbstractStaticCheckTest {

    private static final String POM_XML_RELATIVE_PATH = File.separator + "pom.xml";
    private static final String TEST_DIRECTORY_NAME = "overridingParentPomConfigurationCheckTest";

    private static DefaultConfiguration config;

    @BeforeClass
    public static void createConfiguration() {
        config = createCheckConfig(OverridingParentPomConfigurationCheck.class);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = new DefaultConfiguration("root");
        configParent.addChild(config);
        return configParent;
    }

    @Test
    public void testInvalidPomConfiguration() throws Exception {
        int lineNumber = 9;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Avoid overriding a configuration inherited by the parent pom.");
        String pomXmlAbsolutePath = getPath(
                TEST_DIRECTORY_NAME + File.separator + "invalidPomConfiguration" + POM_XML_RELATIVE_PATH);
        verify(config, pomXmlAbsolutePath, expectedMessages);
    }

    @Test
    public void testMissingOverridingParentPomConfiguration() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        String pomXmlAbsolutePath = getPath(TEST_DIRECTORY_NAME + File.separator
                + "missingOverridingParentPomConfiguration" + POM_XML_RELATIVE_PATH);
        verify(config, pomXmlAbsolutePath, expectedMessages);
    }

    @Test
    public void testEmptyPom() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber, "The pom.xml file should not be empty.");
        String pomXmlAbsolutePath = getPath(TEST_DIRECTORY_NAME + File.separator + "emptyPom" + POM_XML_RELATIVE_PATH);
        verify(config, pomXmlAbsolutePath, expectedMessages);
    }
}
