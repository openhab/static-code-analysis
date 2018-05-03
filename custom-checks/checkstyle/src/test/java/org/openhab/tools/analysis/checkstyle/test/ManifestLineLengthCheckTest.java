/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.openhab.tools.analysis.checkstyle.ManifestLineLengthCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ManifestLineLengthCheck}
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ManifestLineLengthCheckTest extends AbstractStaticCheckTest {
    private static final byte MAX_LINE_SIZE = 72;
    private static final String LOG_MESSAGE = "No line may be longer than " + MAX_LINE_SIZE + " bytes (not characters), in its UTF8-encoded form. If a value would make the initial line longer than this, it should be continued on extra lines (each starting with a single SPACE).";

    private static DefaultConfiguration configuration;

    @BeforeClass
    public static void setUp() {
        configuration = createModuleConfig(ManifestLineLengthCheck.class);
    }

    @Test
    public void shouldNotLogWhenALineDoesNotExceedMaximumBytes() throws Exception {
        verifyManifest("CorrectManifest.MF", CommonUtils.EMPTY_STRING_ARRAY);
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
