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
import org.openhab.tools.analysis.checkstyle.ManifestLineLengthCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link ManifestLineLengthCheck}
 *
 * @author Velin Yordanov - Initial contribution
 */
public class ManifestLineLengthCheckTest extends AbstractStaticCheckTest {
    private static final byte MAX_LINE_SIZE = 72;
    private static final String LOG_MESSAGE = "No line may be longer than " + MAX_LINE_SIZE
            + " bytes (not characters), in its UTF8-encoded form. If a value would make the initial line longer than this, it should be continued on extra lines (each starting with a single SPACE).";

    private static DefaultConfiguration configuration;

    @BeforeClass
    public static void setUp() {
        configuration = createModuleConfig(ManifestLineLengthCheck.class);
    }

    @Test
    public void shouldNotLogWhenALineDoesNotExceedMaximumBytes() throws Exception {
        verifyManifest("CorrectManifest.MF", CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldLogWhenALineExceedsMaximumBytes() throws Exception {
        verifyManifest("ExceedingManifest.MF", generateExpectedMessages(0, LOG_MESSAGE));
    }

    private void verifyManifest(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(configuration, absolutePathToTestFile, expectedMessages);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/manifestLineLengthCheck";
    }
}
