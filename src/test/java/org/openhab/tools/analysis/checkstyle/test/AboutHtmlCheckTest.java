/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.ABOUT_HTML_FILE_NAME;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.AboutHtmlCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link AboutHtmlCheck}
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Added test for about.html file missing in build.properties
 */
public class AboutHtmlCheckTest extends AbstractStaticCheckTest {
    private static final String ABOUT_HTML_CHECK_TEST_DIRECTORY_NAME = "aboutHtmlCheckTest";

    private static final String VALID_ABOUT_HTML_FILE_URL = "https://raw.githubusercontent.com/openhab/openhab2-addons/master/src/etc/about.html";

    private static final String VALID_ABOUT_HTML_FILE_LINK_MSG = "Here is an example of a valid about.html file: "
            + VALID_ABOUT_HTML_FILE_URL;
    private static final String MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG = "About.html file must be added to the bin.includes property";
    private static final String INVALID_LICENSE_HEADER_MSG = "Invalid or missing license header in the about.html file. "
            + VALID_ABOUT_HTML_FILE_LINK_MSG;
    private static final String INVALID_LICENSE_PARAGRAPH_MSG = "Invalid or missing license paragraph in the about.html file. "
            + VALID_ABOUT_HTML_FILE_LINK_MSG;
    private static final String EMPTY_FILE_MSG = "Empty about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG;

    private DefaultConfiguration config;

    private boolean availableResourceURL = false;

    @Before
    public void setUp() {
        try {
            URL url = new URL(VALID_ABOUT_HTML_FILE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                availableResourceURL = true;
            }
        } catch (IOException e) {
            availableResourceURL = false;
        }
    }

    @Test
    public void testValidAboutHtmlFile() throws Exception {
        createValidConfig();

        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;

        verifyAboutHtmlFile("valid_about_html_directory", expectedMessages);
    }

    @Test
    public void testNotValidLicenseHeader() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("not_valid_license_header_directory", expectedMessages);
    }

    @Test
    public void testMissingLicenseHeader() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("missing_license_header_directory", expectedMessages);
    }

    @Test
    public void testNotValidLicenseParagraph() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_PARAGRAPH_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("not_valid_license_paragraph_directory", expectedMessages);
    }

    @Test
    public void testMissingLicenseParagraph() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_PARAGRAPH_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("missing_license_paragraph_directory", expectedMessages);
    }

    @Test
    public void testEmptyAboutHtmlFile() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, EMPTY_FILE_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("empty_about_html_directory", expectedMessages);
    }

    @Test
    public void testWrongLicenseHeaderAndParagraph() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG, 0,
                    INVALID_LICENSE_PARAGRAPH_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("not_valid_license_header_and_paragraph_directory", expectedMessages);
    }

    @Test
    public void testMissingLicenseHeaderAndParagraph() throws Exception {
        createValidConfig();

        String[] expectedMessages;
        if (availableResourceURL) {
            expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG, 0,
                    INVALID_LICENSE_PARAGRAPH_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verifyAboutHtmlFile("missing_license_header_and_paragraph_directory", expectedMessages);
    }

    @Test
    public void testMissingValidAboutHtmlFile() throws Exception {
        config = createCheckConfig(AboutHtmlCheck.class);
        config.addAttribute("validAboutHtmlFileURL", "non.existent.url");

        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;

        verifyAboutHtmlFile("valid_about_html_directory", expectedMessages);
    }

    @Test
    public void testAboutHtmlMissingInBuildProperties() throws Exception {
        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG);
        String testDirectoryName = "about_html_missing_in_build_properties";

        // The message is logged for the build.properties file
        String testDirectoryRelativePath = ABOUT_HTML_CHECK_TEST_DIRECTORY_NAME + File.separator + testDirectoryName;
        String testDirectoryAbsolutePath = getPath(testDirectoryRelativePath);
        File testDirectory = new File(testDirectoryAbsolutePath);

        String messageFilePath = testDirectoryAbsolutePath + File.separator + "build.properties";

        verify(createChecker(config), testDirectory.listFiles(), messageFilePath, expectedMessages);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration("root");
        defaultConfiguration.addChild(config);
        return defaultConfiguration;
    }

    private void createValidConfig() throws IOException {
        config = createCheckConfig(AboutHtmlCheck.class);
        config.addAttribute("validAboutHtmlFileURL", VALID_ABOUT_HTML_FILE_URL);
    }

    private void verifyAboutHtmlFile(String testDirectoryName, String[] expectedMessages) throws Exception {
        String testDirectoryRelativePath = ABOUT_HTML_CHECK_TEST_DIRECTORY_NAME + File.separator + testDirectoryName;
        String testDirectoryAbsolutePath = getPath(testDirectoryRelativePath);
        File testDirectory = new File(testDirectoryAbsolutePath);

        String messageFilePath = testDirectoryAbsolutePath + File.separator + ABOUT_HTML_FILE_NAME;

        verify(createChecker(config), testDirectory.listFiles(), messageFilePath, expectedMessages);
    }
}
