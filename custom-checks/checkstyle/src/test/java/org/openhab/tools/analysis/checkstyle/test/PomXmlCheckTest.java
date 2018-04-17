/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

import org.apache.commons.lang.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.PomXmlCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.checkstyle.api.CheckConstants;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link PomXmlCheck}
 *
 * @author Petar Valchev - Initial Implementation
 * @author Svilen Valkanov - Replaced headers, added new tests, verify absolute path instead of relative
 * @author Velin Yordanov - Added new tests
 */
public class PomXmlCheckTest extends AbstractStaticCheckTest {
    private static final String VERSION_REGULAR_EXPRESSION = "^\\d+\\.\\d+\\.\\d+";

    private static final String MISSING_VERSION_MSG = "Missing /project/version in the pom.xml file.";
    private static final String MISSING_ARTIFACT_ID_MSG = "Missing /project/artifactId in the pom.xml file.";
    private static final String WRONG_VERSION_MSG = "Wrong /project/parent/version in the pom.xml file. "
            + "The version should match the one in the MANIFEST.MF file.";
    private static final String WRONG_ARTIFACT_ID_MSG = "Wrong /project/artifactId in the pom.xml file. "
            + "The artifactId should match the bundle symbolic name in the MANIFEST.MF file.";
    private static final String MISSING_PARENT_ARTIFACT_ID_MSG = "Missing /project/parent/artifactId of the parent pom";
    private static final String WRONG_PARENT_ARTIFACT_ID_MSG = "Wrong /project/parent/artifactId. Expected {0} but was {1}";
    private static final String MANIFEST_REGEX = "^2\\.1\\.0";
    private static String INCORRECT_VERSION = "The manifest version did not match the requirements %s";

    private static DefaultConfiguration config;

    @BeforeClass
    public static void setUpClass() {
        config = createModuleConfig(PomXmlCheck.class);
        config.addAttribute("pomVersionRegularExpression", VERSION_REGULAR_EXPRESSION);
        config.addAttribute("manifestVersionRegularExpression", MANIFEST_REGEX);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/pomXmlCheckTest";
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

    @Test
    public void testMasterPom() throws Exception {
        String testFileAbsolutePath = getPath("pom.xml");
        verify(createChecker(config), testFileAbsolutePath, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testDifferentVersion() throws Exception {
        verifyManifestFile("different_version_directory", 0, String.format(INCORRECT_VERSION, MANIFEST_REGEX));
    }

    @Test
    public void testDifferentManifestVersion() throws Exception {
        verifyManifestFile("different_version_manifest_directory", 0, String.format(INCORRECT_VERSION, MANIFEST_REGEX));
    }

    @Test
    public void testDifferentPomVersion() throws Exception {
        verifyPomXmlFile("different_version_pom_directory", 9, WRONG_VERSION_MSG);
    }

    private void verifyPomXmlFile(String testDirectoryName, int expectedLine, String expectedMessage) throws Exception {
        File testDirectory = getTestDirectory(testDirectoryName);
        File[] testFiles = listFilesForDirectory(testDirectory, new ArrayList<File>());
        String testFilePath = testDirectory.getPath() + File.separator + POM_XML_FILE_NAME;
        String[] expectedMessages = getExpectedMessages(expectedMessage, expectedLine);

        verify(createChecker(config), testFiles, testFilePath, expectedMessages);
    }

    private void verifyManifestFile(String testDirectoryName, int expectedLine, String expectedMessage)
            throws Exception {
        File testDirectory = getTestDirectory(testDirectoryName);
        File[] testFiles = listFilesForDirectory(testDirectory, new ArrayList<File>());
        String testFilePath = testDirectory.getPath() + File.separator + CheckConstants.META_INF_DIRECTORY_NAME
                + File.separator + CheckConstants.MANIFEST_FILE_NAME;
        String[] expectedMessages = getExpectedMessages(expectedMessage, expectedLine);

        verify(createChecker(config), testFiles, testFilePath, expectedMessages);
    }

    private String[] getExpectedMessages(String expectedMessage, int expectedLine) {
        if (expectedMessage != null) {
            return generateExpectedMessages(expectedLine, expectedMessage);
        } else {
            return CommonUtils.EMPTY_STRING_ARRAY;
        }
    }

    private File getTestDirectory(String testDirectoryName) throws Exception {
        String testDirectoryAbsolutePath = getPath(testDirectoryName);
        return new File(testDirectoryAbsolutePath);
    }
}
