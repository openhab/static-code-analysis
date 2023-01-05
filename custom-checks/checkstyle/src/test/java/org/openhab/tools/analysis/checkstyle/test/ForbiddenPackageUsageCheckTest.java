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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.ForbiddenPackageUsageCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Tests for {@link ForbiddenPackageUsageCheck}
 *
 * @author Velin Yordanov - Initial contribution
 */
public class ForbiddenPackageUsageCheckTest extends AbstractStaticCheckTest {
    private static final String MESSAGE = "The package %s should not be used.";
    DefaultConfiguration config = createModuleConfig(ForbiddenPackageUsageCheck.class);

    @BeforeEach
    public void setUp() {
        config.addProperty("forbiddenPackages", "com.google.common,com.something.something,org.something.asd");
        config.addProperty("exceptions", "com.google.common.utils");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/ForbiddenPackageUsageCheckTest";
    }

    @Test
    public void shouldNotLogWhenThereIsNoForbiddenPackageUsage() throws Exception {
        verifyClass("validFile.java", CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldLogWhenAForbiddenPackageIsInUse() throws Exception {
        verifyClass("file-with-forbidden-package.java",
                generateExpectedMessages(7, String.format(MESSAGE, "com.something.something")));
    }

    @Test
    public void shouldLogWhenThereAreMultipleForbiddenPackagesInUse() throws Exception {
        String secondMessage = String.format(MESSAGE, "com.something.something");
        String firstMessage = String.format(MESSAGE, "org.something.asd");
        verifyClass("file-with-multiple-forbidden-packages.java",
                generateExpectedMessages(7, firstMessage, 8, secondMessage));
    }

    @Test
    public void shouldLogWhenAForbiddenSubpackageIsUsed() throws Exception {
        verifyClass("file-with-forbidden-subpackage.java",
                generateExpectedMessages(7, String.format(MESSAGE, "com.google.common.collect")));
    }

    @Test
    public void shouldNotLogWhenThePackageIsAddedToExceptions() throws Exception {
        verifyClass("file-with-exception.java", CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldNotLogWhenTheSubpackageIsAddedToExceptions() throws Exception {
        verifyClass("file-with-subpackage-with-exception.java", CommonUtil.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldLogOnlyThePackagesThatAreNotInExceptionsWhenMultiplePackagesAreUsed() throws Exception {
        String secondMessage = String.format(MESSAGE, "com.something.something");
        String firstMessage = String.format(MESSAGE, "org.something.asd");
        String thirdMessage = String.format(MESSAGE, "com.google.common.collect");
        verifyClass("file-with-multiple-packages.java",
                generateExpectedMessages(7, firstMessage, 8, secondMessage, 10, thirdMessage));
    }

    private void verifyClass(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }
}
