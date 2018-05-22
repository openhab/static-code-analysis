/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ForbiddenPackageUsageCheck;
import org.openhab.tools.analysis.checkstyle.InheritDocCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ForbiddenPackageUsageCheck}
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ForbiddenPackageUsageCheckTest extends AbstractStaticCheckTest {
    private final static String MESSAGE = "The package %s should not be used.";
    DefaultConfiguration config = createModuleConfig(ForbiddenPackageUsageCheck.class);
    
    @Before
    public void setUp() {
        config.addAttribute("forbiddenPackages", "com.google.common,com.something.something,org.something.asd");
        config.addAttribute("exceptions", "com.google.common.utils");
    }
    
    @Override
    protected String getPackageLocation() {
        return "checkstyle/ForbiddenPackageUsageCheckTest";
    }
    
    @Test
    public void shouldNotLogWhenThereIsNoForbiddenPackageUsage() throws Exception {
        verifyClass("validFile.java", CommonUtils.EMPTY_STRING_ARRAY);
    }
    
    @Test
    public void shouldLogWhenAForbiddenPackageIsInUse() throws Exception {
        verifyClass("file-with-forbidden-package.java", generateExpectedMessages(7,String.format(MESSAGE, "com.something.something")));
    }
    
    @Test
    public void shouldLogWhenThereAreMultipleForbiddenPackagesInUse() throws Exception {
        String secondMessage = String.format(MESSAGE, "com.something.something");
        String firstMessage = String.format(MESSAGE, "org.something.asd");
        verifyClass("file-with-multiple-forbidden-packages.java", generateExpectedMessages(7,firstMessage,8,secondMessage));
    }
    
    @Test
    public void shouldLogWhenAForbiddenSubpackageIsUsed() throws Exception {
        verifyClass("file-with-forbidden-subpackage.java", generateExpectedMessages(7,String.format(MESSAGE, "com.google.common.collect")));
    }
    
    @Test
    public void shouldNotLogWhenThePackageIsAddedToExceptions() throws Exception {
        verifyClass("file-with-exception.java", CommonUtils.EMPTY_STRING_ARRAY);
    }
    
    @Test
    public void shouldNotLogWhenTheSubpackageIsAddedToExceptions() throws Exception {
        verifyClass("file-with-subpackage-with-exception.java", CommonUtils.EMPTY_STRING_ARRAY);
    }
    
    @Test
    public void shouldLogOnlyThePackagesThatAreNotInExceptionsWhenMultiplePackagesAreUsed() throws Exception {
        String secondMessage = String.format(MESSAGE, "com.something.something");
        String firstMessage = String.format(MESSAGE, "org.something.asd");
        String thirdMessage = String.format(MESSAGE, "com.google.common.collect");
        verifyClass("file-with-multiple-packages.java", generateExpectedMessages(7,firstMessage,8,secondMessage,10,thirdMessage));
    }
    
    private void verifyClass(String testFileName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(testFileName);
        verify(config, absolutePathToTestFile, expectedMessages);
    }

}
