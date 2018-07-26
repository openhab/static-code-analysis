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
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ImportedPackagesInManifestCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;
import org.openhab.tools.analysis.checkstyle.api.CheckConstants;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ImportedPackagesInManifestCheck}
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ImportedPackagesInManifestCheckTest extends AbstractStaticCheckTest {
    private static final String WARNING_MESSAGE_TEMPLATE = "The package %s needs to be added to the imported packages in the MANIFEST.MF file";
    private static final String NOT_REQUIRED_PACKAGE_MESSAGE = "The package %s should not be imported in the MANIFEST.MF";
    private static DefaultConfiguration configuration;

    @BeforeClass
    public static void setUp() {
        configuration = createModuleConfig(ImportedPackagesInManifestCheck.class);
        configuration.addAttribute("ignoredPackages", "java,org.w3c,org.xml,javax");
        configuration.addAttribute("notRequiredPackages", "org.osgi.service.component");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/ImportedPackagesInManifestCheckTest";
    }

    @Test
    public void shouldNotLogWhenThereAreNoPackagesMissingFromManifest() throws Exception {
        verifyBundle("valid", CommonUtils.EMPTY_STRING_ARRAY);
    }
    
    @Test
    public void shouldLogWhenThereArePackagesMissingFromManifest() throws Exception {
        String message = String.format(WARNING_MESSAGE_TEMPLATE, "org.something");
        verifyBundle("not-valid", generateExpectedMessages(0,message));
    }
    
    @Test
    public void shouldLogWhenANotRequiredPackageIsUsed() throws Exception {
        String message = String.format(NOT_REQUIRED_PACKAGE_MESSAGE, "org.osgi.service.component");
        verifyBundle("not-required-package", generateExpectedMessages(0,message));
    }
    
    private File[] getFiles(String testRootFolder) throws IOException {
        String javaFile = testRootFolder + File.separator + "Test.java";
        String manifestFile = testRootFolder + File.separator + CheckConstants.META_INF_DIRECTORY_NAME + File.separator + CheckConstants.MANIFEST_FILE_NAME;
        return new File[] {new File(getPath(javaFile)), new File(getPath(manifestFile))};
    }
    
    private void verifyBundle(String testRootFolderName, String[] expectedMessages) throws Exception {
        String manifestFile = getPath(testRootFolderName + File.separator + CheckConstants.META_INF_DIRECTORY_NAME+ File.separator + CheckConstants.MANIFEST_FILE_NAME);
        File[] files = getFiles(testRootFolderName);
        verify(createChecker(configuration), files,manifestFile,expectedMessages);
    }
}
