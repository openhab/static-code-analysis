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
import org.openhab.tools.analysis.checkstyle.AvoidScheduleAtFixedRateCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link AvoidScheduleAtFixedRateCheck}
 *
 * @author Lyubomir Papazov - Initial contribution
 */
public class AvoidScheduleAtFixedRateCheckTest extends AbstractStaticCheckTest {

    private static final String WARNING_MESSAGE = "For periodically executed jobs that do not require a fixed rate scheduleWithFixedDelay should be preferred over scheduleAtFixedRate.";

    private final Configuration config = createModuleConfig(AvoidScheduleAtFixedRateCheck.class);

    @Override
    protected String getPackageLocation() {
        return "checkstyle/avoidScheduleAtFixedRateCheckTest";
    }

    @Test
    public void verifyScheduleAtFixedRateUsed() throws Exception {
        final String[] expected = generateExpectedMessages(11, WARNING_MESSAGE);
        verify(config, getPath("ScheduleAtFixedRateUsed.java"), expected);
    }

    @Test
    public void verifyNoScheduledExecutorMethods() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verify(config, getPath("NoScheduledExecutorServiceMethods.java"), expected);
    }

    @Test
    public void verifyScheduleWithFixedDelay() throws Exception {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;
        verify(config, getPath("ValidScheduleWithFixedDelay.java"), expected);
    }
}
