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
import java.text.MessageFormat;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.PomXmlCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link PomXmlCheck}
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Replaced headers, added new tests, verify absolute
 *         path instead of relative
 * @author Velin Yordanov - Added new tests
 */
public class PomXmlCheckTest extends AbstractStaticCheckTest {
    private static final String MISSING_VERSION_MSG = "Missing /project/version in the pom.xml file.";
    private static final String MISSING_ARTIFACT_ID_MSG = "Missing /project/artifactId in the pom.xml file.";
    private static final String MISSING_PARENT_ARTIFACT_ID_MSG = "Missing /project/parent/artifactId of the parent pom";
    private static final String WRONG_PARENT_ARTIFACT_ID_MSG = "Wrong /project/parent/artifactId. Expected {0} but was {1}";
    private static final String DIFFERENT_POM_VERSION = "The pom version is different from the parent pom version";

    private static DefaultConfiguration config;

    @BeforeAll
    public static void setUpClass() {
        config = createModuleConfig(PomXmlCheck.class);
        config.addProperty("checkPomVersion", "true");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/pomXmlCheckTest";
    }

    @Test
    public void testValidPom() throws Exception {
        verifyPomXmlFile("valid_pom_xml_directory", CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testMissingVersionInPom() throws Exception {
        verifyPomXmlFile("missing_version_in_pom_xml_directory",
                generateExpectedMessages(0, MISSING_VERSION_MSG, 0, DIFFERENT_POM_VERSION));
    }

    @Test
    public void shouldLogWhenPomHasDifferentVersionThanParentPom() throws Exception {
        verifyPomXmlFile("pom_xml_with_different_version_than_parent",
                generateExpectedMessages(0, DIFFERENT_POM_VERSION));
    }

    @Test
    public void testMissingArtifactIdInPom() throws Exception {
        verifyPomXmlFile("missing_artifactId_in_pom_xml_directory",
                generateExpectedMessages(0, MISSING_ARTIFACT_ID_MSG));
    }

    @Test
    public void testMissingParentPomId() throws Exception {
        verifyPomXmlFile("missing_parent_pom_id_directory",
                generateExpectedMessages(0, MISSING_PARENT_ARTIFACT_ID_MSG));
    }

    @Test
    public void testInvalidParentPomId() throws Exception {
        String expectedParentPomId = "pom";
        String actualParentPomId = "invalid";
        int parentPomIdlineNumber = 8;
        String formattedMessage = MessageFormat.format(WRONG_PARENT_ARTIFACT_ID_MSG, expectedParentPomId,
                actualParentPomId);
        verifyPomXmlFile("invalid_parent_pom_id_directory",
                generateExpectedMessages(parentPomIdlineNumber, formattedMessage));
    }

    @Test
    public void testMasterPom() throws Exception {
        String testFileAbsolutePath = getPath("pom.xml");
        verify(createChecker(config), testFileAbsolutePath, CommonUtil.EMPTY_STRING_ARRAY);
    }

    private void verifyPomXmlFile(String testDirectoryName, String[] expectedMessages) throws Exception {
        File testDirectory = getTestDirectory(testDirectoryName);
        File[] testFiles = listFilesForDirectory(testDirectory, new ArrayList<File>());
        String testFilePath = testDirectory.getPath() + File.separator + POM_XML_FILE_NAME;

        verify(createChecker(config), testFiles, testFilePath, expectedMessages);
    }

    private File getTestDirectory(String testDirectoryName) throws Exception {
        String testDirectoryAbsolutePath = getPath(testDirectoryName);
        return new File(testDirectoryAbsolutePath);
    }
}
