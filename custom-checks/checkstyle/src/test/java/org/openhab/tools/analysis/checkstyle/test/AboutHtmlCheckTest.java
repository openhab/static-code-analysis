/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.net.URL;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.AboutHtmlCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.utils.CachingHttpClient;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link AboutHtmlCheck}
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Added test for about.html file missing in build.properties
 */
public class AboutHtmlCheckTest extends AbstractStaticCheckTest {
    private static final String VALID_ABOUT_HTML_FILE_URL = "https://raw.githubusercontent.com/openhab/openhab2-addons/master/src/etc/about.html";

    private static final String VALID_ABOUT_HTML_FILE_LINK_MSG = "Here is an example of a valid about.html file: "
            + VALID_ABOUT_HTML_FILE_URL;
    private static final String MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG = "about.html file must be added to the bin.includes property";
    private static final String INVALID_LICENSE_HEADER_MSG = "Invalid or missing license header in the about.html file. "
            + VALID_ABOUT_HTML_FILE_LINK_MSG;
    private static final String INVALID_LICENSE_PARAGRAPH_MSG = "Invalid or missing license paragraph in the about.html file. "
            + VALID_ABOUT_HTML_FILE_LINK_MSG;
    private static final String EMPTY_FILE_MSG = "Empty about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG;

    private DefaultConfiguration config;

    private boolean availableResourceURL = false;

    @Override
    protected String getPackageLocation() {
        return "checkstyle/aboutHtmlCheckTest";
    }

    @Before
    public void setUp() {
        try {
            URL url = new URL(VALID_ABOUT_HTML_FILE_URL);
            CachingHttpClient<String> cachingClient = new CachingHttpClient<>(c -> new String(c));
            availableResourceURL = cachingClient.get(url) != null;
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
        Assume.assumeTrue(availableResourceURL);

        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG);
        verifyAboutHtmlFile("not_valid_license_header_directory", expectedMessages);
    }

    @Test
    public void testMissingLicenseHeader() throws Exception {
        Assume.assumeTrue(availableResourceURL);

        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG);
        verifyAboutHtmlFile("missing_license_header_directory", expectedMessages);
    }

    @Test
    public void testNotValidLicenseParagraph() throws Exception {
        Assume.assumeTrue(availableResourceURL);

        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_PARAGRAPH_MSG);
        verifyAboutHtmlFile("not_valid_license_paragraph_directory", expectedMessages);
    }

    @Test
    public void testMissingLicenseParagraph() throws Exception {
        Assume.assumeTrue(availableResourceURL);

        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_PARAGRAPH_MSG);
        verifyAboutHtmlFile("missing_license_paragraph_directory", expectedMessages);
    }

    @Test
    public void testEmptyAboutHtmlFile() throws Exception {
        Assume.assumeTrue(availableResourceURL);
        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, EMPTY_FILE_MSG);
        verifyAboutHtmlFile("empty_about_html_directory", expectedMessages);
    }

    @Test
    public void testWrongLicenseHeaderAndParagraph() throws Exception {
        Assume.assumeTrue(availableResourceURL);
        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG, 0,
                INVALID_LICENSE_PARAGRAPH_MSG);
        verifyAboutHtmlFile("not_valid_license_header_and_paragraph_directory", expectedMessages);
    }

    @Test
    public void testMissingLicenseHeaderAndParagraph() throws Exception {
        Assume.assumeTrue(availableResourceURL);
        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, INVALID_LICENSE_HEADER_MSG, 0,
                INVALID_LICENSE_PARAGRAPH_MSG);
        verifyAboutHtmlFile("missing_license_header_and_paragraph_directory", expectedMessages);
    }

    @Test
    public void testMissingValidAboutHtmlFile() throws Exception {
        config = createModuleConfig(AboutHtmlCheck.class);
        config.addAttribute("validAboutHtmlFileURL", "non.existent.url");

        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;

        verifyAboutHtmlFile("valid_about_html_directory", expectedMessages);
    }

    @Test
    public void testAboutHtmlMissingInBuildProperties() throws Exception {
        createValidConfig();

        String[] expectedMessages = generateExpectedMessages(0, MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG);
        String testDirectoryName = "about_html_missing_in_build_properties";
        File testDirectory = new File(getPath(testDirectoryName));

        // The message is logged for the build.properties file
        String messageFilePath = getPath(testDirectoryName) + File.separator + "build.properties";

        verify(createChecker(config), testDirectory.listFiles(), messageFilePath, expectedMessages);
    }

    private void createValidConfig() throws IOException {
        config = createModuleConfig(AboutHtmlCheck.class);
        config.addAttribute("validAboutHtmlFileURL", VALID_ABOUT_HTML_FILE_URL);
    }

    private void verifyAboutHtmlFile(String testDirectoryName, String[] expectedMessages) throws Exception {
        File testDirectory = new File(getPath(testDirectoryName));
        String messageFilePath = getPath(testDirectoryName) + File.separator + ABOUT_HTML_FILE_NAME;

        verify(createChecker(config), testDirectory.listFiles(), messageFilePath, expectedMessages);
    }
}
