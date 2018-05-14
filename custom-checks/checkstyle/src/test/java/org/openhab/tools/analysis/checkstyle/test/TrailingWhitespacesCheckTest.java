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
import org.openhab.tools.analysis.checkstyle.TrailingWhitespacesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link TrailingWhitespacesCheck}
 * 
 * @author Lyubomir Papazov - Initial contribution
 *
 */
public class TrailingWhitespacesCheckTest extends AbstractStaticCheckTest {

    @Override
    protected String getPackageLocation() {
        return "checkstyle/trailingWhitespacesCheckTest";
    }

    @Test
    public void testOneTrailingWhitespace() throws Exception {
        int lineNumber = 3;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                TrailingWhitespacesCheck.TRAILING_WHITESPACE_MESSAGE);
        verify(createModuleConfig(TrailingWhitespacesCheck.class), getPath("OneTrailingWhitespace.java"),
                expectedMessages);
    }

    @Test
    public void testMultipleTrailingWhitespaces() throws Exception {
        String[] expectedMessages = generateExpectedMessages(3, TrailingWhitespacesCheck.TRAILING_WHITESPACE_MESSAGE, 4,
                TrailingWhitespacesCheck.TRAILING_WHITESPACE_MESSAGE);
        verify(createModuleConfig(TrailingWhitespacesCheck.class), getPath("MultipleTrailingWhitespaces.java"),
                expectedMessages);
    }

    @Test
    public void testNoTrailingWhitespaces() throws Exception {
        String[] expectedMessages =  CommonUtils.EMPTY_STRING_ARRAY;
        verify(createModuleConfig(TrailingWhitespacesCheck.class), getPath("NoTrailingWhitespaces.java"),
                expectedMessages);
    }
}
