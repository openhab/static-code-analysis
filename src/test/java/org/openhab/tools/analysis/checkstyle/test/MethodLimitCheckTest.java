/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.MethodLimitCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link MethodLimitCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Petar Valchev - Added the general verifyMethodLimit() method
 *
 */
public class MethodLimitCheckTest extends AbstractStaticCheckTest {
    private static final String TEST_DIRECTORY_NAME = "methodLimitCheckTest";

    @Test
    public void testClassThatExceedsMethodLimit() throws Exception {
        int lineNumber = 3;
        String[] expectedMessages = generateExpectedMessages(lineNumber, "Too many methods.");
        verifyMethodLimit("1", "MethodLimitCheckTestFile.java", expectedMessages);
    }

    @Test
    public void testClassThatDoesNotExceedMethodLimit() throws Exception {
        String[] expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        verifyMethodLimit("10", "MethodLimitCheckTestFile.java", expectedMessages);
    }
    
    private void verifyMethodLimit(String maxMethods, String fileName, String[] expectedMessages) throws Exception{
        DefaultConfiguration config = createCheckConfig(MethodLimitCheck.class);
        config.addAttribute("max", maxMethods);
        
        String filePath = getPath(TEST_DIRECTORY_NAME + File.separator + fileName);
        verify(config, filePath, expectedMessages);
    }
}
