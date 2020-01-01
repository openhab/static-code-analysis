/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.POM_XML_FILE_NAME;

import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.tools.analysis.checkstyle.KarafFeatureCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Tests for {@link KarafFeatureCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class KarafFeatureCheckTest extends AbstractStaticCheckTest {

    @Mock
    private Handler handler;

    @Captor
    private ArgumentCaptor<LogRecord> captor;

    private static final String MSG_MISSING_BUNDLE_IN_FEATURE_XML = "Bundle with ID '{0}' must be added in one of {1}";
    private static final DefaultConfiguration CONFIGURATION = createModuleConfig(KarafFeatureCheck.class);

    @BeforeClass
    public static void setUp() {
        CONFIGURATION.addAttribute("featureXmlPath", "feature/feature.xml:feature/internal/feature.xml");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/karafFeatureCheck";
    }

    @Before
    public void setup() {
        Logger system = Logger.getLogger("");
        system.addHandler(handler);
    }

    @After
    public void teardown() {
        Logger system = Logger.getLogger("");
        system.removeHandler(handler);
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

        org.mockito.Mockito.verify(handler, times(1)).publish(captor.capture());

        assertThat(captor.getValue().getLevel(), is(Level.WARNING));
        assertThat(captor.getValue().getMessage(), startsWith(
                "KarafFeatureCheck will be skipped. Could not find Maven group ID (parent group ID) or artifact ID in"));
    }

    @Test
    public void testMissingBundle() throws Exception {
        String[] messages = generateExpectedMessages(0, MessageFormat.format(MSG_MISSING_BUNDLE_IN_FEATURE_XML,
                "mvn:org.openhab.binding/org.openhab.binding.missing/{project.version}", "feature/feature.xml:feature/internal/feature.xml"));
        verify("missingBundle", messages);
    }
    
    @Test
    public void testMultipleFeatureFiles() throws Exception {
        
    }

    private void verify(String fileDirectory, String[] expectedMessages) throws Exception {
        verify(CONFIGURATION,
                getPath(fileDirectory + File.separator + POM_XML_FILE_NAME),
                expectedMessages);
    }
}
