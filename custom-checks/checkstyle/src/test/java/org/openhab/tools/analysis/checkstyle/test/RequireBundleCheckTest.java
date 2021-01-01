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

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.RequireBundleCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link RequireBundleCheck}
 *
 * @author Petar Valchev - Initial contribution
 */
public class RequireBundleCheckTest extends AbstractStaticCheckTest {
    private static final String REQUIRE_BUNDLE_USED_MSG = "The MANIFEST.MF file must not contain any Require-Bundle entries. "
            + "Instead, Import-Package must be used.";
    private static final String REQUIRE_BUNDLE_TEST_USED_MSG = "The MANIFEST.MF file of a test fragment must not contain "
            + "Require-Bundle entries other than org.junit, org.hamcrest, org.mockito.";

    private static DefaultConfiguration config;

    @BeforeClass
    public static void setUpClass() {
        config = createModuleConfig(RequireBundleCheck.class);

        String allowedRequireBundles = String.format("%s,%s,%s", "org.junit", "org.hamcrest", "org.mockito");
        config.addAttribute("allowedRequireBundles", allowedRequireBundles);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/requireBundleCheckTest";
    }

    @Test
    public void testRequireBundlePackage() throws Exception {
        verifyManifest("require_bundle_manifest_directory", "REQUIRE_BUNDLE_MANIFEST.MF", 9, REQUIRE_BUNDLE_USED_MSG);
    }

    @Test
    public void testValidManifest() throws Exception {
        verifyManifest("valid_manifest_directory", "VALID_MANIFEST.MF", 0, null);
    }

    @Test
    public void testValidTestManifest() throws Exception {
        verifyManifest("valid_test_manifest_directory", "VALID_TEST_MANIFEST.MF", 0, null);
    }

    @Test
    public void testValidTestManifestNoRequireBundle() throws Exception {
        verifyManifest("valid_test_manifest_directory", "VALID_TEST_MANIFEST_NO_REQUIRE_BUNDLE.MF", 0, null);
    }

    @Test
    public void testInvalidRequireBundleTestPackage() throws Exception {
        verifyManifest("invalid_test_manifest_directory", "INVALID_TEST_MANIFEST.MF", 13, REQUIRE_BUNDLE_TEST_USED_MSG);
    }

    private void verifyManifest(String testDirectoryName, String testFileName, int expectedLine, String expectedMessage)
            throws Exception {
        String manifestRelativePath = testDirectoryName + File.separator + testFileName;
        String manifestAbsolutePath = getPath(manifestRelativePath);

        String[] expectedMessages = null;
        if (expectedMessage != null) {
            expectedMessages = generateExpectedMessages(expectedLine, expectedMessage);
        } else {
            expectedMessages = CommonUtil.EMPTY_STRING_ARRAY;
        }

        verify(config, manifestAbsolutePath, expectedMessages);
    }
}
