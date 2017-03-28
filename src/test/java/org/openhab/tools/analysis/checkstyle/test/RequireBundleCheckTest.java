/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.RequireBundleCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link RequireBundleCheck}
 *
 * @author Petar Valchev
 *
 */
public class RequireBundleCheckTest extends AbstractStaticCheckTest {
    private static final String REQUIRE_BUNDLE_USED_MSG = "The MANIFEST.MF file must not contain any Require-Bundle entries. "
            + "Instead, Import-Package must be used.";
    
    private static DefaultConfiguration config;

    @BeforeClass
    public static void setUpClass() {
        config = createCheckConfig(RequireBundleCheck.class);
    }

    @Test
    public void testExportedInternalPackage() throws Exception {
        verifyManifest("require_bundle_manifest_directory", "REQUIRE_BUNDLE_MANIFEST.MF", 9, REQUIRE_BUNDLE_USED_MSG);
    }

    @Test
    public void testValidManifest() throws Exception {
        verifyManifest("valid_manifest_directory", "VALID_MANIFEST.MF", 0, null);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration("root");
        defaultConfiguration.addChild(config);
        return defaultConfiguration;
    }

    private void verifyManifest(String testDirectoryName, String testFileName, int expectedLine, String expectedMessage)
            throws Exception {
        String requireBundleCheckTestDirectory = "requireBundleCheckTest/";
        String manifestRelativePath = requireBundleCheckTestDirectory + File.separator + testDirectoryName
                + File.separator + testFileName;
        String manifestAbsolutePath = getPath(manifestRelativePath);

        String[] expectedMessages = null;
        if (expectedMessage != null) {
            expectedMessages = generateExpectedMessages(expectedLine, REQUIRE_BUNDLE_USED_MSG);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verify(config, manifestAbsolutePath, expectedMessages);
    }
}
