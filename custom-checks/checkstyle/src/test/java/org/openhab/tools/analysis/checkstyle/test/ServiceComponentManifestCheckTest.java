/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.ServiceComponentManifestCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link ServiceComponentManifestCheck}
 *
 * @author Aleksandar Kovachev - Initial contribution
 * @author Petar Valchev - Changed the verifyServiceComponentHeader() method and
 *         some of the test methods
 * @author Dimitar Ivanov - Common wildcard test added
 * @author Svilen Valkanov - Added tests for missing services in build.properties
 *
 */
public class ServiceComponentManifestCheckTest extends AbstractStaticCheckTest {
    private static final String MANIFEST_RELATIVE_PATH = META_INF_DIRECTORY_NAME + File.separator + MANIFEST_FILE_NAME;

    private static final String BEST_APPROACH_MESSAGE = "A good approach is to use OSGI-INF/*.xml "
            + "instead of including the services metadata files separately or using common wildcard.";
    private static final String WRONG_DIRECTORY_MESSAGE = "Incorrect directory for services - %s. "
            + "The best practice is services metadata files to be placed directly in OSGI-INF directory.";
    private static final String WRONG_EXTENSION_MESSAGE = "The service %s is with invalid extension."
            + "Only XML metadata files for services description are expected in the OSGI-INF directory.";
    private static final String NOT_INCLUDED_SERVICE_MESSAGE = "The service %s is not included in the MANIFEST.MF file. "
            + "Are you sure that there is no need to be included?";
    private static final String NOT_EXISTING_SERVICE_MESSAGE = "The service %s does not exist in the OSGI-INF folder.";
    private static final String REPEATED_SERVICE_MESSAGE = "If you are using OSGI-INF/*.xml, do not include any of the services explicitly. "
            + "Otherwise they will be included more than once.";
    private static final String NOT_MATCHING_REGEX_MESSAGE = "The service component %s does not match any of the exisitng services.";
    private static final String MISSING_SERVICE_IN_BUILD_PROPERTIES = "The service component {0} isn`t included in the build.properties file."
            + " Good approach is to include all files by adding `OSGI-INF/` value to the bin.includes property.";

    private static DefaultConfiguration config;

    @BeforeAll
    public static void createConfiguration() {
        config = createModuleConfig(ServiceComponentManifestCheck.class);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/serviceComponentManifestCheckTest";
    }

    @Test
    public void testWrongServicesDirectoryInManifest() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(WRONG_DIRECTORY_MESSAGE, "wrong_directory"));

        verifyServiceComponentHeader("wrong_service_directory_in_manifest", expectedMessages);
    }

    @Test
    public void testNonExistentServiceDirectory() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(WRONG_DIRECTORY_MESSAGE, "non_existent_directory"));

        verifyServiceComponentHeader("non_existent_service_directory_in_manifest", expectedMessages);
    }

    @Test
    public void testWrongServiceExtensionsInManifest() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(WRONG_EXTENSION_MESSAGE, "htmlService.html"), lineNumber,
                String.format(WRONG_EXTENSION_MESSAGE, "txtService.txt"));

        verifyServiceComponentHeader("wrong_service_extension", expectedMessages);
    }

    @Test
    public void testMissingServiceComponentHeaderInManifest() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceFromSubFolder.xml"), lineNumber,
                String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceOne.xml"), lineNumber,
                String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceTwo.xml"));

        verifyServiceComponentHeader("missing_service_component_in_manifest", expectedMessages);
    }

    @Test
    public void testMissingServicesInManifest() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceFromSubFolder.xml"), lineNumber,
                String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceThree.xml"), lineNumber, BEST_APPROACH_MESSAGE);

        verifyServiceComponentHeader("not_included_services_in_manifest", expectedMessages);
    }

    @Test
    public void testManifestExplicitlyIncludeServices() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber, BEST_APPROACH_MESSAGE);

        verifyServiceComponentHeader("explicitly_included_services_in_manifest", expectedMessages);
    }

    @Test
    public void testManifestRegexIncludedServices() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber, BEST_APPROACH_MESSAGE);

        verifyServiceComponentHeader("regex_included_service_in_manifest", expectedMessages);
    }

    @Test
    public void testNotMatchingRegex() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(NOT_MATCHING_REGEX_MESSAGE, "nonExistentService*.xml"), lineNumber, BEST_APPROACH_MESSAGE,
                lineNumber, String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceOne.xml"), lineNumber,
                String.format(NOT_INCLUDED_SERVICE_MESSAGE, "testServiceTwo.xml"));

        verifyServiceComponentHeader("not_matching_regex_in_manifest", expectedMessages);
    }

    @Test
    public void testServicesInSubdirectory() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(WRONG_DIRECTORY_MESSAGE, "subdirectory"));

        verifyServiceComponentHeader("subdirectory_services", expectedMessages);
    }

    @Test
    public void testExplicitlyIncludedSubfolderServices() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(WRONG_DIRECTORY_MESSAGE, "subdirectory"), lineNumber,
                String.format(WRONG_DIRECTORY_MESSAGE, "subdirectory"), lineNumber, BEST_APPROACH_MESSAGE);

        verifyServiceComponentHeader("included_subfolder_services", expectedMessages);
    }

    @Test
    public void testNonExistentService() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber, BEST_APPROACH_MESSAGE, lineNumber,
                String.format(NOT_EXISTING_SERVICE_MESSAGE, "testServiceFour.xml"));

        verifyServiceComponentHeader("non_existent_service_in_manifest", expectedMessages);
    }

    @Test
    public void testCorrectlyIncludedServicesInManifest() throws Exception {
        String[] expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        verifyServiceComponentHeader("correctly_included_services_in_manifest", expectedMessages);
    }

    @Test
    public void testRepeatedService() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber, REPEATED_SERVICE_MESSAGE);

        verifyServiceComponentHeader("repeated_service_in_manifest", expectedMessages);
    }

    @Test
    public void testCommonWildcard() throws Exception {
        int lineNumber = 11;
        String[] expectedMessages = generateExpectedMessages(lineNumber, BEST_APPROACH_MESSAGE, lineNumber);
        verifyServiceComponentHeader("wildcard_only_in_manifest", expectedMessages);
    }

    @Test
    public void testMissingServiceInBuildProperties() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber, MessageFormat
                .format(MISSING_SERVICE_IN_BUILD_PROPERTIES, "OSGI-INF" + File.separator + "serviceTestFileTwo.xml"));
        verifyBuildProperties("missing_service_in_build_properties", expectedMessages);
    }

    @Test
    public void testMissingAllServicesInBuildProperties() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                MessageFormat.format(MISSING_SERVICE_IN_BUILD_PROPERTIES,
                        "OSGI-INF" + File.separator + "serviceTestFileOne.xml"),
                lineNumber, MessageFormat.format(MISSING_SERVICE_IN_BUILD_PROPERTIES,
                        "OSGI-INF" + File.separator + "serviceTestFileTwo.xml"));
        verifyBuildProperties("missing_all_services_in_build_properties", expectedMessages);
    }

    private void verifyServiceComponentHeader(String testDirectoryName, String[] expectedMessages) throws Exception {
        verify(MANIFEST_RELATIVE_PATH, testDirectoryName, expectedMessages);
    }

    private void verifyBuildProperties(String testDirectoryName, String[] expectedMessages) throws Exception {
        verify(BUILD_PROPERTIES_FILE_NAME, testDirectoryName, expectedMessages);
    }

    private void verify(String filePath, String testDirectoryName, String[] expectedMessages) throws Exception {
        File testDirectory = new File(getPath(testDirectoryName));
        String testFilePath = testDirectory.getPath() + File.separator + filePath;
        // All files are listed recursively
        File[] testFiles = FileUtils.listFiles(testDirectory, null, true).toArray(new File[] {});

        verify(createChecker(config), testFiles, testFilePath, expectedMessages);
    }
}
