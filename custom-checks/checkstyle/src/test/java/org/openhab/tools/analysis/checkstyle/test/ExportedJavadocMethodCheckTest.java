/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ExportedJavadocMethodCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ExportedJavadocMethodCheck}
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ExportedJavadocMethodCheckTest extends AbstractStaticCheckTest {
    private static final String LOG_MESSAGE_TEMPLATE = "Missing javadoc for method %s";
    private final Configuration config = createModuleConfig(ExportedJavadocMethodCheck.class);

    @Test
    public void shouldLogWhenPackageIsExported() throws Exception {
        String[] expectedMessages = generateExpectedMessages(14, String.format(LOG_MESSAGE_TEMPLATE, "handleCommand"),
                18, String.format(LOG_MESSAGE_TEMPLATE, "method"), 22, String.format(LOG_MESSAGE_TEMPLATE, "method2"));
        verifyJavadoc("exportedPackageFile.java", expectedMessages);
    }

    @Test
    public void shouldNotLogWhenPackageIsInternal() throws Exception {
        verifyJavadoc("internalPackageFile.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldNotLogWhenPackageIsExportedButMethodHasJavadoc() throws Exception {
        verifyJavadoc("exportedFileWithJavadoc.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    private void verifyJavadoc(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/exportedJavadocMethodCheckTest";
    }
}
