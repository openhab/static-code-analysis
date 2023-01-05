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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.POM_XML_FILE_NAME;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openhab.tools.analysis.checkstyle.KarafFeatureCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link KarafFeatureCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class KarafFeatureCheckTest extends AbstractStaticCheckTest {

    private static final String MSG_MISSING_BUNDLE_IN_FEATURE_XML = "Bundle with ID '{0}' must be added in one of {1}";
    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(KarafFeatureCheck.class);

    public final @RegisterExtension LoggedMessagesExtension extension = new LoggedMessagesExtension(
            KarafFeatureCheck.class);

    @BeforeAll
    public static void setUp() {
        CONFIGURATION.addProperty("featureXmlPath", "feature/feature.xml:feature/internal/feature.xml");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/karafFeatureCheck";
    }

    @Test
    public void testIncludedBundle() throws Exception {
        verify("includedBundle", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testIncludedBundleWithParentGroupIdOnly() throws Exception {
        verify("includedBundleWithParentGroupIdOnly", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testIncludedBundleWithMultipleFeatureFiles() throws Exception {
        verify("includedBundleWithParentGroupIdOnly", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testInvalidBundle() throws Exception {
        verify("invalidBundle", ArrayUtils.EMPTY_STRING_ARRAY);

        List<String> messages = extension.getFormattedMessages();

        assertThat(messages.size(), is(1));
        assertThat(messages.get(0), startsWith(
                "KarafFeatureCheck will be skipped. Could not find Maven group ID (parent group ID) or artifact ID in"));
    }

    @Test
    public void testMissingBundle() throws Exception {
        String[] messages = generateExpectedMessages(0,
                MessageFormat.format(MSG_MISSING_BUNDLE_IN_FEATURE_XML,
                        "mvn:org.openhab.binding/org.openhab.binding.missing/{project.version}",
                        "feature/feature.xml:feature/internal/feature.xml"));
        verify("missingBundle", messages);
    }

    @Test
    public void testMultipleFeatureFiles() throws Exception {
    }

    private void verify(String fileDirectory, String[] expectedMessages) throws Exception {
        verify(CONFIGURATION, getPath(fileDirectory + File.separator + POM_XML_FILE_NAME), expectedMessages);
    }
}
