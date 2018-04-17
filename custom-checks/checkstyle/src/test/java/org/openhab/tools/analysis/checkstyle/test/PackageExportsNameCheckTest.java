/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.PackageExportsNameCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Test for {@link PackageExportsNameCheck }
 *
 * @author Аleksandar Кovachev - Initial contribution
 * @author Petar Valchev - Added a test for non existent packages
 *
 */
public class PackageExportsNameCheckTest extends AbstractStaticCheckTest {
    private static final String CORRECT_NAMING_OF_NOT_EXPORTED_PACKAGES_MESSAGE = "The package %s"
            + " should be marked as \"internal\" if it is not exported.";

    private static final String MANIFEST_REALTIVE_PATH = META_INF_DIRECTORY_NAME + File.separator + MANIFEST_FILE_NAME;

    private static DefaultConfiguration configuration;

    @BeforeClass
    public static void createConfiguration() {
        configuration = createModuleConfig(PackageExportsNameCheck.class);
        configuration.addAttribute("sourceDirectories", "src/main/java");
        configuration.addAttribute("excludedPackages", ".*.internal.*");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/packageExportsNameCheckTest";
    }

    @Test
    public void testNotExportedPackages() throws Exception {
        int lineNumber = 0;
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(CORRECT_NAMING_OF_NOT_EXPORTED_PACKAGES_MESSAGE, "org.openhab.not.exported.package"));

        verifyWarningMessages("not_exported_packages", expectedMessages);
    }

    @Test
    public void testValidExports() throws Exception {
        verifyNoWarningMessages("valid_exports");
    }

    @Test
    public void testNotIncludedSourceDirectories() throws Exception {
        // we dont expect a message for or.openhab.core.export2, because the tested directory
        // is not included as a configuration property in the directories to be checked
        verifyNoWarningMessages("not_included_source_directories");
    }

    @Test
    public void testExcludedPackages() throws Exception {
        int lineNumber = 0;
        // we get a message only for the package that is not excluded,
        // but not for the other not exported packages
        String[] expectedMessages = generateExpectedMessages(lineNumber,
                String.format(CORRECT_NAMING_OF_NOT_EXPORTED_PACKAGES_MESSAGE, "org.openhab.export"));

        verifyWarningMessages("excluded_packages", expectedMessages);
    }

    private void verifyNoWarningMessages(String directory) throws Exception {
        verifyWarningMessages(directory, CommonUtils.EMPTY_STRING_ARRAY);
    }

    private void verifyWarningMessages(String directory, String[] messages) throws Exception {
        String manifestFilePath = getPath(directory + File.separator + MANIFEST_REALTIVE_PATH);

        verify(configuration, manifestFilePath, messages);
    }
}
