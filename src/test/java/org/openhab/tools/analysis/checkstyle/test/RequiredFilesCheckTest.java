/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.tools.analysis.checkstyle.RequiredFilesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.junit.BeforeClass;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link RequiredFilesCheck}
 *
 * @author Petar Valchev
 *
 */
public class RequiredFilesCheckTest extends AbstractStaticCheckTest {
    private static final String TEST_DIRECTORY_NAME = "requiredFilesCheckTest";

    private static final String ABOUT_HTML_FILE_NAME = "about.html";
    private static final String BUILD_PROPERTIES_FILE_NAME = "build.properties";
    private static final String POM_XML_FILE_NAME = "pom.xml";
    private static final String MANIFEST_MF_FILE_NAME = "MANIFEST.MF";
    private static final String README_MD_FILE_NAME = "README.md";
    
    private static final String MISSING_FILE_MSG = "Missing %s file.";
    
    private static DefaultConfiguration config;

    @BeforeClass
    public static void setUpClass() {
        config = createCheckConfig(RequiredFilesCheck.class);

        String extenstionsPropertyValue = String.format("%s,%s,%s,%s,%s", ".html", ".properties",
                ".xml", ".MF", ".md");
        config.addAttribute("extensions", extenstionsPropertyValue);

        String requiredFilesPropertyValue = String.format("%s,%s,%s,%s,%s", ABOUT_HTML_FILE_NAME,
                BUILD_PROPERTIES_FILE_NAME, POM_XML_FILE_NAME, MANIFEST_MF_FILE_NAME, README_MD_FILE_NAME);
        config.addAttribute("requiredFiles", requiredFilesPropertyValue);
    }

    @Test
    public void testMissingAboutHtmlFile() throws Exception {
        verifyDirectory("missing_about_html_directory", ABOUT_HTML_FILE_NAME,
                String.format(MISSING_FILE_MSG, ABOUT_HTML_FILE_NAME));
    }

    @Test
    public void testPresentAboutHtmlFile() throws Exception {
        verifyDirectory("valid_directory", ABOUT_HTML_FILE_NAME, null);
    }

    @Test
    public void testMissingBuildPropertiesFile() throws Exception {
        verifyDirectory("missing_build_properties_directory", BUILD_PROPERTIES_FILE_NAME,
                String.format(MISSING_FILE_MSG, BUILD_PROPERTIES_FILE_NAME));
    }

    @Test
    public void testPresentBuildPropertiesFile() throws Exception {
        verifyDirectory("valid_directory", BUILD_PROPERTIES_FILE_NAME, null);
    }

    @Test
    public void testMissingPomXmlFile() throws Exception {
        verifyDirectory("missing_pom_xml_directory", POM_XML_FILE_NAME,
                String.format(MISSING_FILE_MSG, POM_XML_FILE_NAME));
    }

    @Test
    public void testPresentPomXmlFile() throws Exception {
        verifyDirectory("valid_directory", POM_XML_FILE_NAME, null);
    }

    @Test
    public void testMissingManifestMfFile() throws Exception {
        verifyDirectory("missing_manifest_mf_directory", MANIFEST_MF_FILE_NAME,
                String.format(MISSING_FILE_MSG, MANIFEST_MF_FILE_NAME));
    }

    @Test
    public void testPresentManifestMfFile() throws Exception {
        verifyDirectory("valid_directory", MANIFEST_MF_FILE_NAME, null);
    }

    @Test
    public void testMissingReadmeMdFile() throws Exception {
        verifyDirectory("missing_readme_md_directory", README_MD_FILE_NAME,
                String.format(MISSING_FILE_MSG, README_MD_FILE_NAME));
    }

    @Test
    public void testPresentReadmeMdFile() throws Exception {
        verifyDirectory("valid_directory", README_MD_FILE_NAME, null);
    }

    @Test
    public void testAllRequiredFilesMissing() throws Exception {
        File[] testFiles = getFilesForDirectory("all_required_files_missing");

        Map<String, List<String>> expectedViolations = new HashMap<>();

        addExpectedViolation(expectedViolations, MANIFEST_MF_FILE_NAME);
        addExpectedViolation(expectedViolations, ABOUT_HTML_FILE_NAME);
        addExpectedViolation(expectedViolations, BUILD_PROPERTIES_FILE_NAME);
        addExpectedViolation(expectedViolations, POM_XML_FILE_NAME);
        addExpectedViolation(expectedViolations, README_MD_FILE_NAME);

        verify(createChecker(config), testFiles, expectedViolations);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration("root");
        defaultConfiguration.addChild(config);
        return defaultConfiguration;
    }

    private void verifyDirectory(String testDirectoryName, String fileName, String expectedMessage) throws Exception {
        File[] testFiles = getFilesForDirectory(testDirectoryName);

        String testDirectoryAbsolutePath = getDirectoryAbsolutePath(testDirectoryName);
        String messageFilePath = testDirectoryAbsolutePath + File.separator + fileName;

        String[] expectedMessages;
        if (expectedMessage != null) {
            expectedMessages = generateExpectedMessages(0, expectedMessage);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verify(createChecker(config), testFiles, messageFilePath, expectedMessages);
    }

    private void addExpectedViolation(Map<String, List<String>> expectedViolations, String fileName) {
        String[] expectedMessages = generateExpectedMessages(0,
                String.format(MISSING_FILE_MSG, fileName));
        expectedViolations.put(File.separator + fileName, Arrays.asList(expectedMessages));
    }

    private File[] getFilesForDirectory(String directoryName) throws IOException {
        String directoryAbsolutePath = getDirectoryAbsolutePath(directoryName);
        File directory = new File(directoryAbsolutePath);
        File[] files = listFilesForDirectory(directory, new ArrayList<File>());
        return files;
    }

    private String getDirectoryAbsolutePath(String directoryName) throws IOException {
        String directoryRelativePath = TEST_DIRECTORY_NAME + File.separator + directoryName;
        String directoryAbsolutePath = getPath(directoryRelativePath);
        return directoryAbsolutePath;
    }
}
