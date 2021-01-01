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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.BundleVendorCheck;
import org.openhab.tools.analysis.checkstyle.ManifestJavaVersionCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link BundleVendorCheck}
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class ManifestJavaVersionCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CHECK_CONFIG = createModuleConfig(ManifestJavaVersionCheck.class);

    @BeforeClass
    public static void setUpClass() {
        CHECK_CONFIG.addAttribute("allowedValues", "JavaSE-1.8");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/manifestJavaVersionCheckTest";
    }

    @Test
    public void testEmptyFile() throws Exception {
        verify("emptyManifest.MF", generateExpectedMessages());

        // we ignore this, its the responsibility of another check
    }

    @Test
    public void testCompliantFile() throws Exception {
        verify("compliantSample.MF", generateExpectedMessages());
    }

    @Test
    public void testMissingBundleVersion() throws Exception {
        verify("missingJavaVersionSample.MF",
                generateExpectedMessages(0, "\"Bundle-RequiredExecutionEnvironment\" is missing"));
    }

    @Test
    public void testDoubleBundleVersion() throws Exception {
        verify("doubleJavaVersionSample.MF",
                generateExpectedMessages(5, "Only 1 \"Bundle-RequiredExecutionEnvironment\" was expected.", 8,
                        "Only 1 \"Bundle-RequiredExecutionEnvironment\" was expected."));
    }

    @Test
    public void testWrongLabelForBundleVersion() throws Exception {
        verify("wrongKeySample.MF", generateExpectedMessages(7,
                "Expect eg. \"Bundle-RequiredExecutionEnvironment: JavaSE-1.8\" got \"Bundle-RequiredExecutionEnvironmentJavaSE-1.7\""));
    }

    @Test
    public void testWrongValueForBundleVersion() throws Exception {
        verify("wrongValueSample.MF",
                generateExpectedMessages(7, "Unexpected \"JavaSE-1.7\", only allowed options: [JavaSE-1.8]"));
    }

    private void verify(String fileName, String[] expectedMessages) throws Exception {
        verify(CHECK_CONFIG, getPath(fileName), expectedMessages);
    }
}
