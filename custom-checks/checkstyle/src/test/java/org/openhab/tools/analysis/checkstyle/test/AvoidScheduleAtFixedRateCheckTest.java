/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.AvoidScheduleAtFixedRateCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link AvoidScheduleAtFixedRateCheck}
 *
 * @author Lyubomir Papazov
 *
 */
public class AvoidScheduleAtFixedRateCheckTest extends AbstractStaticCheckTest {

    private static final String WARNING_MESSAGE = "For periodically executed jobs that do not require a fixed rate scheduleWithFixedDelay should be preferred over scheduleAtFixedRate.";
    private static final String TEST_DIRECTORY_NAME = "avoidScheduleAtFixedRateCheckTest";

    Configuration config = createCheckConfig(AvoidScheduleAtFixedRateCheck.class);

    @Test
    public void verifyScheduleAtFixedRateUsed() throws Exception {
        verifyJavaFile("ScheduleAtFixedRateUsed.java", generateExpectedMessages(11, WARNING_MESSAGE));
    }

    @Test
    public void verifyNoScheduledExecutorMethods() throws Exception {
        verifyJavaFileNoErrors("NoScheduledExecutorServiceMethods.java");
    }

    @Test
    public void verifyScheduleWithFixedDelay() throws Exception {
        verifyJavaFileNoErrors("ValidScheduleWithFixedDelay.java");
    }

    private void verifyJavaFile(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(TEST_DIRECTORY_NAME + "/" + testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }

    private void verifyJavaFileNoErrors(String testFileName) throws Exception {
        verifyJavaFile(testFileName, CommonUtils.EMPTY_STRING_ARRAY);
    }
}
