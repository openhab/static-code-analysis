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

import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.InheritDocCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link InheritDocCheck}
 *
 * @author Velin Yordanov - Initial contribution
 */
public class InheritDocCheckTest extends AbstractStaticCheckTest {
    private static final String LOG_MESSAGE = "Remove unnecessary inherit doc";

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
        verifyJavadoc("fileWithValidJavadoc.java", CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldNotLogWhenTheFileHasNoJavadoc() throws Exception {
        verifyJavadoc("fileWithoutJavadoc.java", CommonUtil.EMPTY_STRING_ARRAY);
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
