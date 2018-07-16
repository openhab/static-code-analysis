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
import org.openhab.tools.analysis.checkstyle.ExportedJavadocVariableCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ExportedJavadocVariableCheck}
 * @author Velin Yordanov - initial contribution
 *
 */
public class ExportedJavadocVariableCheckTest extends AbstractStaticCheckTest  {
    private static final String LOG_MESSAGE = "javadoc.missing";
    private final Configuration config = createModuleConfig(ExportedJavadocVariableCheck.class);
    
    @Test
    public void shouldLogWhenPackageIsExported() throws Exception {
        verifyJavadoc("exportedPackageFile.java", generateExpectedMessages("29:5",LOG_MESSAGE, "30:5",LOG_MESSAGE,"31:5",LOG_MESSAGE));
    }
    
    @Test
    public void shouldNotLogWhenPackageIsInternal() throws Exception {
        verifyJavadoc("internalPackageFile.java", CommonUtils.EMPTY_STRING_ARRAY);
    }
    
    @Test
    public void shouldNotLogWhenPackageIsExportedButVariableHasJavadoc() throws Exception {
        verifyJavadoc("exportedPackageWithJavadoc.java", CommonUtils.EMPTY_STRING_ARRAY);
    }
    
    private void verifyJavadoc(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/exportedJavadocVariableCheckTest";
    }
}
