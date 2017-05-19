/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.POM_XML_FILE_NAME;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.PomXmlCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link PomXmlCheck}
 *
 * @author Petar Valchev - Initial Implementation
 * @author Svilen Valkanov - Replaced headers, added new tests, verify absolute path instead of relative
 */
public class PomXmlCheckTest extends AbstractStaticCheckTest {
    private static final String POM_XML_CHECK_TEST_DIRECTORY_NAME = "pomXmlCheckTest";
    private static final String VERSION_REGULAR_EXPRESSION = "^\\d+[.]\\d+[.]\\d+";

    private static final String MISSING_VERSION_MSG = "Missing /project/parent/version in the pom.xml file.";
    private static final String MISSING_ARTIFACT_ID_MSG = "Missing /project/artifactId in the pom.xml file.";
    private static final String WRONG_VERSION_MSG = "Wrong /project/parent/version in the pom.xml file. "
            + "The version should match the one in the MANIFEST.MF file.";
    private static final String WRONG_ARTIFACT_ID_MSG = "Wrong /project/artifactId in the pom.xml file. "
            + "The artifactId should match the bundle symbolic name in the MANIFEST.MF file.";
    private static final String MISSING_PARENT_ARTIFACT_ID_MSG = "Missing /project/parent/artifactId of the parent pom";
    private static final String WRONG_PARENT_ARTIFACT_ID_MSG = "Wrong /project/parent/artifactId. Expected {0} but was {1}";

    private static DefaultConfiguration config;

    @BeforeClass
    public static void setUpClass() {
        config = createCheckConfig(PomXmlCheck.class);
        config.addAttribute("pomVersionRegularExpression", VERSION_REGULAR_EXPRESSION);
        config.addAttribute("manifestVersionRegularExpression", VERSION_REGULAR_EXPRESSION);
    }

    @Test
    public void testValidPom() throws Exception {
        verifyPomXmlFile("valid_pom_xml_directory", 0, null);
    }

    @Test
    public void testInvalidVersionInPom() throws Exception {
        verifyPomXmlFile("invalid_version_in_pom_xml_directory", 9, WRONG_VERSION_MSG);
    }

    @Test
    public void testInvalidartifactIdInPom() throws Exception {
        verifyPomXmlFile("invalid_artifactId_in_pom_xml_directory", 14, WRONG_ARTIFACT_ID_MSG);
    }

    @Test
    public void testMissingVersionInPom() throws Exception {
        verifyPomXmlFile("missing_version_in_pom_xml_directory", 0, MISSING_VERSION_MSG);
    }

    @Test
    public void testMissingArtifactIdInPom() throws Exception {
        verifyPomXmlFile("missing_artifactId_in_pom_xml_directory", 0, MISSING_ARTIFACT_ID_MSG);
    }

    @Test
    public void testMissingParentPomId() throws Exception {
        verifyPomXmlFile("missing_parent_pom_id_directory", 0, MISSING_PARENT_ARTIFACT_ID_MSG);
    }

    @Test
    public void testInvalidParentPomId() throws Exception {
        String expectedParentPomId = "pom";
        String actualParentPomId = "invalid";
        int parentPomIdlineNumber = 8;
        String formattedMessage = MessageFormat.format(WRONG_PARENT_ARTIFACT_ID_MSG, expectedParentPomId,
                actualParentPomId);
        verifyPomXmlFile("invalid_parent_pom_id_directory", parentPomIdlineNumber, formattedMessage);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration("root");
        defaultConfiguration.addChild(config);
        return defaultConfiguration;
    }

    private void verifyPomXmlFile(String testDirectoryName, int expectedLine, String expectedMessage) throws Exception {
        String testDirectoryAbsolutePath = getPath(POM_XML_CHECK_TEST_DIRECTORY_NAME + "/" + testDirectoryName);
        File testDirectory = new File(testDirectoryAbsolutePath);
        File[] testFiles = listFilesForDirectory(testDirectory, new ArrayList<File>());
        String testFilePath = testDirectory.getPath() + File.separator + POM_XML_FILE_NAME;

        String[] expectedMessages;
        if (expectedMessage != null) {
            expectedMessages = generateExpectedMessages(expectedLine, expectedMessage);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verify(createChecker(config), testFiles, testFilePath, expectedMessages);
    }
}
