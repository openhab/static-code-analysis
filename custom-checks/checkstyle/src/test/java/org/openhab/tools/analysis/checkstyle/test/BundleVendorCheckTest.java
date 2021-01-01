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
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link BundleVendorCheck}
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class BundleVendorCheckTest extends AbstractStaticCheckTest {
    private static final DefaultConfiguration CHECK_CONFIG = createModuleConfig(BundleVendorCheck.class);

    @BeforeClass
    public static void setUpClass() {
        CHECK_CONFIG.addAttribute("allowedValues", "openHAB");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/correctOpenHABSpellingInManifestTest";
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
        verify("missingBundleVendorSample.MF", generateExpectedMessages(0, "\"Bundle-Vendor\" is missing"));
    }

    @Test
    public void testDoubleBundleVersion() throws Exception {
        verify("doubleBundleVendorSample.MF", generateExpectedMessages(4, "Only 1 \"Bundle-Vendor\" was expected.", 8,
                "Only 1 \"Bundle-Vendor\" was expected."));
    }

    @Test
    public void testWrongLabelForBundleVersion() throws Exception {
        verify("wrongKeySample.MF",
                generateExpectedMessages(5, "Expect eg. \"Bundle-Vendor: openHAB\" got \"Bundle-vendor:openHAB\""));
    }

    @Test
    public void testWrongValueForBundleVersion() throws Exception {
        verify("wrongValueSample.MF",
                generateExpectedMessages(5, "Unexpected \"Openhab\", only allowed options: [openHAB]"));
    }

    private void verify(String fileName, String[] expectedMessages) throws Exception {
        verify(CHECK_CONFIG, getPath(fileName), expectedMessages);
    }
}
