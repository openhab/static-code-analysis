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

import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.AnnotationDependencyCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link AnnotationDependencyCheck}
 *
 * @author Kristina Simova - Initial contribution
 *
 */
public class AnnotationDependencyCheckTest extends AbstractStaticCheckTest {

    private static final String TEST_DIRECTORY_NAME = "annotationDependencyCheckTest";
    private static final String EXPECTED_WARNING_MESSAGE = "Every bundle should have optional Import-Package dependency to org.eclipse.jdt.annotation.";
    private static DefaultConfiguration checkConfiguration = createCheckConfig(AnnotationDependencyCheck.class);

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration configuration) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration("root");
        defaultConfiguration.addChild(configuration);
        return defaultConfiguration;
    }

    @Test
    public void testManifestFileWithoutOptionalAnnotationDependency() throws Exception {
        String fileName = "MANIFEST.MF";
        String[] expectedMessage = generateExpectedMessages(9, EXPECTED_WARNING_MESSAGE);
        checkFile(fileName, expectedMessage);
    }

    @Test
    public void testManifestFileWithOptionalAnnotationDependency() throws Exception {
        String fileName = "MANIFEST2.MF";
        checkFile(fileName, CommonUtils.EMPTY_STRING_ARRAY);
    }

    @Test
    public void testManifestFileWithImportPackageDependencyAndMissingOptionalResolution() throws Exception {
        String fileName = "MANIFEST3.MF";
        String[] expectedMessage = generateExpectedMessages(9, EXPECTED_WARNING_MESSAGE);
        checkFile(fileName, expectedMessage);
    }

    private void checkFile(String fileName, String... expectedMessages) throws Exception {
        String filePath = getPath(TEST_DIRECTORY_NAME + File.separator + fileName);
        verify(checkConfiguration, filePath, expectedMessages);
    }
}
