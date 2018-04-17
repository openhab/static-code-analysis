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
import org.openhab.tools.analysis.checkstyle.InheritDocCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link InheritDocCheck}
 *
 * @author Velin Yordanov
 *
 */
public class InheritDocCheckTest extends AbstractStaticCheckTest {
    private final static String LOG_MESSAGE = "Remove unnecessary inherit doc";

    Configuration config = createModuleConfig(InheritDocCheck.class);

    @Override
    protected String getPackageLocation() {
        return "checkstyle/inheritDocCheckTest";
    }

    @Test
    public void shouldLogWhenThereIsAnEmptyInheritDoc() throws Exception {
        verifyJavadoc("fileWithProblem.java", generateExpectedMessages(3, LOG_MESSAGE));
    }

    @Test
    public void shouldNotLogWhenThereIsAnInheritDocWithAdditionalDocumentation() throws Exception {
        verifyJavadoc("fileWithValidJavadoc.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldNotLogWhenTheFileHasNoJavadoc() throws Exception {
        verifyJavadoc("fileWithoutJavadoc.java", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldLogWhenThereIsAnEmptyInheritDocWithTabsAndSpaces() throws Exception {
        verifyJavadoc("notValidFileWithTabsAndSpaces.java", generateExpectedMessages(3, LOG_MESSAGE));
    }

    @Test
    public void shouldLogWhenThereIsAnEmptyInheritDocWithALotOfNewLines() throws Exception {
        verifyJavadoc("fileWithALotOfNewLines.java", generateExpectedMessages(3, LOG_MESSAGE));
    }

    private void verifyJavadoc(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }
}
