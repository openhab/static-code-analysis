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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.POM_XML_FILE_NAME;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.OverridingParentPomConfigurationCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link OverridingParentPomConfigurationCheck}
 *
 * @author Aleksandar Kovachev - Initial contribution
 */
public class OverridingParentPomConfigurationCheckTest extends AbstractStaticCheckTest {
    private static DefaultConfiguration config;

    @BeforeAll
    public static void createConfiguration() {
        config = createModuleConfig(OverridingParentPomConfigurationCheck.class);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/overridingParentPomConfigurationCheckTest";
    }

    @Test
    public void testInvalidPomConfiguration() throws Exception {
        int lineNumber = 9;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                "Avoid overriding a configuration inherited by the parent pom.");
        verifyPom("invalidPomConfiguration", expectedMessages);
    }

    @Test
    public void testMissingOverridingParentPomConfiguration() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyPom("missingOverridingParentPomConfiguration", expectedMessages);
    }

    @Test
    public void testEmptyPom() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber, "The pom.xml file should not be empty.");
        verifyPom("emptyPom", expectedMessages);
    }

    private void verifyPom(String pomDirectoryName, String[] expectedMessages) throws Exception {
        String pomXmlAbsolutePath = getPath(pomDirectoryName + File.separator + POM_XML_FILE_NAME);
        verify(config, pomXmlAbsolutePath, expectedMessages);
    }
}
