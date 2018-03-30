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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openhab.tools.analysis.checkstyle.CompactProfileCheck;
import org.openhab.tools.analysis.checkstyle.CompactProfileCheck.Profile;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.google.common.io.Files;
import com.ibm.icu.text.MessageFormat;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Tests for {@link CompactProfileCheck}
 * 
 * @author Svilen Valkanov - Initial contribution
 *
 */
public class CompactProfileCheckTest extends AbstractStaticCheckTest {

    private static final String TEST_BASE_DIR = "compactProfileCheckTest/";
    private static final DefaultConfiguration checkConfig = createCheckConfig(
            CompactProfileCheck.class);
    private static final String MESSAGE_PATTERN = "Package {0} depends on {1}, which is a superset of {2}";

    private Log logger = LogFactory.getLog(CompactProfileCheckTest.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder(new File(
            this.getClass().getClassLoader().getResource(".").getPath()));

    @Test
    public void testCompactProfileTwoCompatibleJar() throws Exception {
        verify("compactProfileTwoCompatible",
                "CompactProfileTwoCompatible.java", generateExpectedMessages());
    }

    @Test
    public void testCompactProfileThreeCompatibleJar() throws Exception {
        String expectedMessage = MessageFormat.format(MESSAGE_PATTERN,
                "checkstyle.compactProfileCheckTest",
                "javax.xml.crypto (compact3)", Profile.COMPACT_PROFILE_2);

        verify("compactProfileThreeCompatible",
                "CompactProfileThreeCompatible.java",
                generateExpectedMessages(0, expectedMessage));
    }

    @Test
    public void testFullStandardEditionJar() throws Exception {
        String expectedMessage = MessageFormat.format(MESSAGE_PATTERN,
                "checkstyle.compactProfileCheckTest", "javax.jws (Full JRE)",
                Profile.COMPACT_PROFILE_2);

        verify("fullStandardEdition", "FullStandardEdition.java",
                generateExpectedMessages(0, expectedMessage));
    }

    private void verify(String testCaseDir, String testSourceFile,
            String[] expectedMessages) throws Exception {
        String testSourceDirectory = getPath(TEST_BASE_DIR + "/" + testCaseDir);
        Path testSourcePath = Paths.get(testSourceDirectory, "src");

        File targetFolder = tempFolder.newFolder(TARGET_DIRECTORY);
        Path targetDirPath = targetFolder.toPath();

        // compile the classes into tempfolder/target
        String compileCommand = String.format("javac -d %s %s%s%s",
                targetDirPath.toString(), testSourcePath.toString(),
                File.separator, testSourceFile);

        Process compilation = Runtime.getRuntime().exec(compileCommand);
        compilation.waitFor();
        debug(compilation, compileCommand);

        // package the compiled classes into jar
        String packageCommand = String.format("jar cf %s %s",
                targetDirPath.toString() + File.separator
                        + "org.openhab.tools.sat-plugin.test-0.9.0.jar",
                targetDirPath.toString());

        Process packaging = Runtime.getRuntime().exec(packageCommand);
        packaging.waitFor();
        debug(packaging, packageCommand);

        // Copy the pom.xml. It will be read by the check to find out the jar
        // name
        File copiedPomFile = tempFolder.newFile(POM_XML_FILE_NAME);
        Files.copy(new File(testSourceDirectory, POM_XML_FILE_NAME),
                copiedPomFile);

        // execute the check in the temp folder
        verify(createChecker(checkConfig), copiedPomFile.getAbsolutePath(),
                expectedMessages);

    }

    private void debug(Process process, String commandLine) throws IOException {
        String s = null;
        BufferedReader stdInput = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        logger.debug("stdInput: " + commandLine);
        while ((s = stdInput.readLine()) != null) {
            logger.debug(s);
        }
        logger.debug("stdError: " + commandLine);
        while ((s = stdError.readLine()) != null) {
            logger.error(s);
        }
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration(
                "root");
        defaultConfiguration.addChild(config);
        return defaultConfiguration;
    }
}