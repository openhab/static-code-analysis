/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import java.io.File;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.PrivateReferencesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link PrivateReferencesCheck}
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class PrivateReferencesCheckTest extends AbstractStaticCheckTest {
    private final static String TEST_DIRECTORY_NAME = "privateReferencesCheckTest";
    private final static String LOG_MESSAGE = "Internal type usage detected in public API: Exported type %s contains internal type %s in the method definition of %s";
    private static DefaultConfiguration configuration;

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = createCheckConfig(TreeWalker.class);
        configParent.addChild(config);
        return configParent;
    }

    @BeforeClass
    public static void createConfiguration() {
        configuration = createCheckConfig(PrivateReferencesCheck.class);
    }

    private void verifyReferences(String testBundleName, String[] expectedMessages) throws Exception {
        String absolutePathToTestFile = getPath(TEST_DIRECTORY_NAME + File.separator + testBundleName);
        File[] files = this.listFilesForDirectory(new File(absolutePathToTestFile), new ArrayList<File>());
        verify(createChecker(configuration), files, "", expectedMessages);
    }

    @Test
    public void shouldNotLogWhenBundleHasNoPrivateReferencesInPublicApi() throws Exception {
        verifyReferences("bundleWithoutPrivateReferences", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldLogWhenThereArePrivateReferencesInProtectedMethod() throws Exception {
        String type = "org.openhab.binding.test.handler.TestHandler";
        String bundleName = "bundleWithPrivateReferencesInProtectedMethod";
        String internalType = "TestConfiguration";
        String methodName = "updateChannel";
        verifyReferences(bundleName,
                generateExpectedMessages(0, String.format(LOG_MESSAGE, type, internalType, methodName)));
    }

    @Test
    public void shouldLogWhenBundleHasPrivateReferencesInPublicApi() throws Exception {
        String type = "org.openhab.binding.test.handler.TestHandler";
        String bundleName = "bundleWithPrivateReferences";
        String internalType = "TestConfiguration";
        String methodName = "updateChannel";
        verifyReferences(bundleName,
                generateExpectedMessages(0, String.format(LOG_MESSAGE, type, internalType, methodName)));
    }

    @Test
    public void shouldNotLogWhenBundleHasNoInternalPackages() throws Exception {
        verifyReferences("bundleWithNoInternalPackages", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldNotLogWhenBundleHasNoExportedPackages() throws Exception {
        verifyReferences("bundleWithNoExportedPackages", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldNotLogWhenBundleHasADifferentImportWithTheSameName() throws Exception {
        verifyReferences("bundleWithDifferentImportWithTheSameName", CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void shouldLogWhenBundleHasMultiplePrivateReferences() throws Exception {
        String type = "org.openhab.binding.test.handler.TestHandler";
        String type2 = "org.openhab.binding.test.handler.TestHandler2";
        String internalType = "TestConfiguration";
        String internalType2 = "TestHandlerFactory";
        String methodName = "updateChannel";
        String methodName2 = "something";
        
        String firstMessage = String.format(LOG_MESSAGE, type, internalType, methodName);
        String secondMessage = String.format(LOG_MESSAGE, type2, internalType, methodName);
        String thirdMessage = String.format(LOG_MESSAGE, type2, internalType2, methodName);
        String fourthMessage = String.format(LOG_MESSAGE, type2, internalType2, methodName2);

        verifyReferences("bundleWithMultiplePrivateReferences",
                generateExpectedMessages(0, firstMessage, 0, secondMessage, 0, thirdMessage, 0, fourthMessage));
    }
}