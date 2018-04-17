/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BIN_INCLUDES_PROPERTY_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BUILD_PROPERTIES_FILE_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.OUTPUT_PROPERTY_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.SOURCE_PROPERTY_NAME;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.BuildPropertiesCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link BuildPropertiesCheck}
 *
 * @author Petar Valchev - Intial implementation
 * @author Svilen Valkanov - Some minor fixes and improvements
 */
public class BuildPropertiesCheckTest extends AbstractStaticCheckTest {
    private static final String MISSING_PROPERTY_MSG = "Missing %s property in the %s file.";
    private static final String MISSING_VALUE_MSG = "Property  %s in the %s file is missing value: ";
    private static final String EMPTY_FILE_MSG = String.format("Empty %s file", BUILD_PROPERTIES_FILE_NAME);

    private static final String MISSING_BIN_INCLUDES_PROPERTY_MSG = String.format(MISSING_PROPERTY_MSG,
            BIN_INCLUDES_PROPERTY_NAME, BUILD_PROPERTIES_FILE_NAME);

    private static final String MISSING_BIN_INCLUDES_VALUE_MSG = String.format(MISSING_VALUE_MSG,
            BIN_INCLUDES_PROPERTY_NAME, BUILD_PROPERTIES_FILE_NAME);
    private static final String MISSING_OUTPUT_VALUE_MSG = String.format(MISSING_VALUE_MSG, OUTPUT_PROPERTY_NAME,
            BUILD_PROPERTIES_FILE_NAME);
    private static final String MISSING_SRC_VALUE_MSG = String.format(MISSING_VALUE_MSG, SOURCE_PROPERTY_NAME,
            BUILD_PROPERTIES_FILE_NAME);

    private static DefaultConfiguration config = createModuleConfig(BuildPropertiesCheck.class);

    @BeforeClass
    public static void setUpTest() {
        config.addAttribute("expectedBinIncludesValues", "META-INF/");
        config.addAttribute("possibleOutputValues", "target/classes,target/test-classes");
        config.addAttribute("possibleSourceValues", "src/main/java,src/main/resources,src/test/java,src/test/groovy");
    }

    @Override
    protected String getPackageLocation() {
        return "checkstyle/buildPropertiesCheckTest";
    }

    @Test
    public void testValidBuildPropertiesFile() throws Exception {
        verifyBuildPropertiesFile("valid_build_properties_directory", 0, null);
    }

    @Test
    public void testValidBuildPropertiesTestFile() throws Exception {
        verifyBuildPropertiesFile("valid_build_properties_test_directory", 0, null);
    }

    @Test
    public void testMissingBinIncludesValue() throws Exception {
        String missingValue = "META-INF/";
        verifyBuildPropertiesFile("missing_bin_includes_value_directory", 2,
                MISSING_BIN_INCLUDES_VALUE_MSG + missingValue);
    }

    @Test
    public void testMissingOutputValue() throws Exception {
        String missingValue = "Any of [target/classes, target/test-classes]";
        verifyBuildPropertiesFile("missing_output_value_directory", 1, MISSING_OUTPUT_VALUE_MSG + missingValue);
    }

    @Test
    public void testMissingSourceValue() throws Exception {
        String missingValue = "src/main/java";
        verifyBuildPropertiesFile("missing_source_value_directory", 7, MISSING_SRC_VALUE_MSG + missingValue);
    }

    @Test
    public void testMissingBinIncludesProperty() throws Exception {
        verifyBuildPropertiesFile("missing_bin_includes_property_directory", 0, MISSING_BIN_INCLUDES_PROPERTY_MSG);
    }

    @Test
    public void testEmptyBuildPropertiesFile() throws Exception {
        verifyBuildPropertiesFile("empty_build_properties_file_directory", 0, EMPTY_FILE_MSG);
    }

    private void verifyBuildPropertiesFile(String testDirectoryName, int expectedLine, String expectedMessage)
            throws Exception {
        String testDirectoryAbsolutePath = getPath(testDirectoryName);
        File testDirectory = new File(testDirectoryAbsolutePath);
        File[] filesToCheck = FileUtils.listFiles(testDirectory, null, true).toArray(new File[] {});

        String filePath = testDirectoryAbsolutePath + File.separator + BUILD_PROPERTIES_FILE_NAME;

        String[] expectedMessages;
        if (expectedMessage != null) {
            expectedMessages = generateExpectedMessages(expectedLine, expectedMessage);
        } else {
            expectedMessages = CommonUtils.EMPTY_STRING_ARRAY;
        }

        verify(createChecker(config), filesToCheck, filePath, expectedMessages);
    }
}
