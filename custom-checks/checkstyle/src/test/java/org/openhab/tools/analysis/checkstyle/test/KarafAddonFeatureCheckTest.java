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

import static org.openhab.tools.analysis.checkstyle.KarafAddonFeatureCheck.*;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.POM_XML_FILE_NAME;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.KarafAddonFeatureCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link KarafAddonFeatureCheck}
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class KarafAddonFeatureCheckTest extends AbstractStaticCheckTest {

    private static final String ARTIFACT_ID = "org.openhab.binding.example";
    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(KarafAddonFeatureCheck.class);

    @Override
    protected String getPackageLocation() {
        return "checkstyle/karafAddonFeatureCheck";
    }

    @Test
    public void testValid() throws Exception {
        verify("valid", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testExcludeAddonPatterns() throws Exception {
        DefaultConfiguration config = createModuleConfig(KarafAddonFeatureCheck.class);
        config.addProperty("excludeAddonPatterns", "excludeAddon.*");

        verify(config, getPath("excludeAddonPatterns" + File.separator + POM_XML_FILE_NAME),
                ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testMissingFeatureFile() throws Exception {
        final File featureFile = new File(getPath("missingFeature"), FEATURE_XML_PATH.toString());

        verify(createChecker(CONFIGURATION), getPath("missingFeature" + File.separator + POM_XML_FILE_NAME),
                featureFile.getAbsolutePath(),
                generateExpectedMessages(0, MessageFormat.format(MSG_MISSING_FEATURE_XML, featureFile)));
    }

    @Test
    public void testInvalidBundleName() throws Exception {
        verify("invalidBundle", generateExpectedMessages(7,
                MessageFormat.format(MSG_BUNDLE_INVALID, MessageFormat.format(BUNDLE_VALUE, ARTIFACT_ID))));
    }

    @Test
    public void testInvalidFeaturesName() throws Exception {
        verify("invalidFeaturesName",
                generateExpectedMessages(2, MessageFormat.format(MSG_FEATURES_NAME_INVALID, ARTIFACT_ID)));
    }

    @Test
    public void testInvalidFeatureName() throws Exception {
        verify("invalidFeatureName", generateExpectedMessages(5,
                MessageFormat.format(MSG_FEATURE_NAME_INVALID, ARTIFACT_ID.replaceAll("\\.", "-").substring(4))));
    }

    @Test
    public void testPatternFeatureName() throws Exception {
        DefaultConfiguration config = createModuleConfig(KarafAddonFeatureCheck.class);
        config.addProperty("featureNameMappings", "openhab-binding-example:org.openhab.binding.someother");
        verify(config, getPath("invalidFeatureName" + File.separator + FEATURE_XML_PATH),
                ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private void verify(String fileDirectory, String[] expectedMessages) throws Exception {
        verify(CONFIGURATION, getPath(fileDirectory + File.separator + FEATURE_XML_PATH), expectedMessages);
    }
}
