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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.RequiredFilesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link RequiredFilesCheck}
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Use relative path for required files
 */
public class RequiredFilesCheckTest extends AbstractStaticCheckTest {
    private static final String MISSING_FILE_MSG = "Missing %s file.";

    private static DefaultConfiguration config;

    @BeforeAll
    public static void setUpClass() {
        config = createModuleConfig(RequiredFilesCheck.class);

        String extensionsPropertyValue = String.format("%s,%s,%s,%s", HTML_EXTENSION, PROPERTIES_EXTENSION,
                XML_EXTENSION, MARKDOWN_EXTENSION);
        config.addProperty("extensions", extensionsPropertyValue);

        String requiredFilesPropertyValue = String.format("%s,%s", POM_XML_FILE_NAME, README_MD_FILE_NAME);
        config.addProperty("requiredFiles", requiredFilesPropertyValue);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/requiredFilesCheckTest";
    }

    @Disabled("Checkstyle can't check on files without extension. Therefore ignore for now.")
    @Test
    public void testMissingNoticeFile() throws Exception {
        verifyDirectory("missing_notice_html_directory", NOTICE_FILE_NAME,
                String.format(MISSING_FILE_MSG, NOTICE_FILE_NAME));
    }

    @Disabled("Checkstyle can't check on files without extension. Therefore ignore for now.")
    @Test
    public void testPresentNoticeFile() throws Exception {
        verifyDirectory("valid_directory", NOTICE_FILE_NAME, null);
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

        // Checkstyle can't check on files without extension. Therefore ignore for now:
        // addExpectedViolation(expectedViolations, NOTICE_FILE_NAME, NOTICE_FILE_NAME);
        addExpectedViolation(expectedViolations, POM_XML_FILE_NAME, POM_XML_FILE_NAME);
        addExpectedViolation(expectedViolations, README_MD_FILE_NAME, README_MD_FILE_NAME);

        verify(createChecker(config), testFiles, expectedViolations);
    }

    private void verifyDirectory(String testDirectoryName, String fileName, String expectedMessage) throws Exception {
        File[] testFiles = getFilesForDirectory(testDirectoryName);
        String testDirectoryAbsolutePath = getPath(testDirectoryName);
        String messageFilePath = testDirectoryAbsolutePath + File.separator + fileName;

        String[] expectedMessages;
        if (expectedMessage != null) {
            expectedMessages = generateExpectedMessages(0, expectedMessage);
        } else {
            expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        }

        verify(createChecker(config), testFiles, messageFilePath, expectedMessages);
    }

    private void addExpectedViolation(Map<String, List<String>> expectedViolations, String filePath, String fileName) {
        String[] expectedMessages = generateExpectedMessages(0, String.format(MISSING_FILE_MSG, fileName));
        expectedViolations.put(File.separator + filePath, Arrays.asList(expectedMessages));
    }

    private File[] getFilesForDirectory(String directoryName) throws IOException {
        String directoryAbsolutePath = getPath(directoryName);
        File directory = new File(directoryAbsolutePath);
        File[] files = listFilesForDirectory(directory, new ArrayList<File>());
        return files;
    }
}
