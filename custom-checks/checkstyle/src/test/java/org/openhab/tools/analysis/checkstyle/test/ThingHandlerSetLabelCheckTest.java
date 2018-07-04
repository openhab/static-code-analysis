/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.openhab.tools.analysis.checkstyle.ThingHandlerSetLabelCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.google.common.collect.Maps;
import com.puppycrawl.tools.checkstyle.BriefUtLogger;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Tests for {@link ThingHandlerSetLabelCheck}
 *
 * @author Tanya Georgieva - Initial Contribution
 */
public class ThingHandlerSetLabelCheckTest extends AbstractStaticCheckTest {

    private static final String TEST_DIRECTORY_NAME = "thingHandlerSetLabelCheckTest";
    private static final String WARNING_MESSAGE = "Do not use the setLabel of the thing in the ThingHandler";

    private static DefaultConfiguration config = createCheckConfig(ThingHandlerSetLabelCheck.class);

    private static final String BINDING_BASE_HANDLER = "org.openhab.binding.base.handler";
    private static final String BINDING_EXTENDING_HANDLER = "org.openhab.binding.extending.handler";
    private static final String BINDING_IMPORT_ONLY = "org.openhab.binding.import.only";
    private static final String BINDING_NOT_EXTENDING_HANDLER = "org.openhab.binding.notextending.handler";
    private static final String BINDING_THING_HANDLER = "org.openhab.binding.thing.handler";
    private static final String BINDING_THING_FIELD = "org.openhabl.binding.thing.field";

    private TreeMap<File, String[]> filesWithWarnings;

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @Override
    public BriefUtLogger getBriefUtLogger() {
        return new BriefUtLogger(stream);
    }

    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        DefaultConfiguration configParent = createCheckConfig(TreeWalker.class);
        configParent.addChild(config);
        return configParent;
    }

    @Before
    public void initializeMap() {
        filesWithWarnings = new TreeMap<>();
    }

    @Test
    public void verifyBindingExtendingHandlerPackage() throws Exception {
        filesWithWarnings = generateMap(BINDING_EXTENDING_HANDLER, "ThingHandlerWithSetLabel.java",
                CommonUtils.EMPTY_STRING_ARRAY, "ThingHandlerExtendingIndirectlyIncorrectBTH.java",
                CommonUtils.EMPTY_STRING_ARRAY, "ThingHandlerExtendingIncorrectClass.java",
                CommonUtils.EMPTY_STRING_ARRAY);

        verifyMapFiles(createChecker(config), filesWithWarnings);
    }

    @Test
    public void verifyBindingImportOnlyPackage() throws Exception {
        filesWithWarnings = generateMap(BINDING_IMPORT_ONLY, "ThingHandlerWithImportBaseThingHandlerOnly.java",
                CommonUtils.EMPTY_STRING_ARRAY);

        verifyMapFiles(createChecker(config), filesWithWarnings);
    }

    @Test
    public void verifyBindingNotExtendingHandlerPackage() throws Exception {
        filesWithWarnings = generateMap(BINDING_NOT_EXTENDING_HANDLER, "ThingHandlerExtendingIncorrectChildOfBTH.java",
                CommonUtils.EMPTY_STRING_ARRAY, "ThingHandlerWithIncorrectBaseThingHandlerPackage.java",
                CommonUtils.EMPTY_STRING_ARRAY, "ClassWithoutHandlerAndSetLabel.java", CommonUtils.EMPTY_STRING_ARRAY);

        verifyMapFiles(createChecker(config), filesWithWarnings);
    }

    @Test
    public void verifyBindingThingFieldPackage() throws Exception {
        filesWithWarnings = generateMap(BINDING_THING_FIELD, "ThingHandlerWithoutThingFieldWithSetLabel.java",
                CommonUtils.EMPTY_STRING_ARRAY);

        verifyMapFiles(createChecker(config), filesWithWarnings);
    }

    @Test
    public void verifyBindingBaseHandlerPackage() throws Exception {
        filesWithWarnings = generateMap(BINDING_BASE_HANDLER, "ThingHandlerExtendingIndirectChildOfBTH.java",
                generateExpectedMessages(14, WARNING_MESSAGE), "ThingHandlerWithSetLabel.java",
                generateExpectedMessages(15, WARNING_MESSAGE), "ThingHandlerExtendingIndirectlyBTH.java",
                generateExpectedMessages(14, WARNING_MESSAGE));

        verifyMapFiles(createChecker(config), filesWithWarnings);
    }

    @Test
    public void verifyBindingThingHandlerPackage() throws Exception {
        filesWithWarnings = generateMap(BINDING_THING_HANDLER, "ThingHandlerWithThingFieldWithoutSetLabel.java",
                CommonUtils.EMPTY_STRING_ARRAY, "ThingHandlerWithSetLabelWithLiteralThis.java",
                generateExpectedMessages(15, WARNING_MESSAGE), "ThingHandlerWithoutSetLabel.java",
                CommonUtils.EMPTY_STRING_ARRAY, "ThingHandlerWithMultipleSetLabel.java",
                generateExpectedMessages(17, WARNING_MESSAGE, 21, WARNING_MESSAGE));

        verifyMapFiles(createChecker(config), filesWithWarnings);
    }

    protected TreeMap<File, String[]> generateMap(String directoryName, Object... arguments) throws Exception {
        String filePath = getFilePath(directoryName);
        int filesNumber = arguments.length / 2;
        TreeMap<File, String[]> generatedMap = new TreeMap<>();

        for (int i = 0; i < filesNumber; i++) {
            File file = new File(filePath + arguments[2 * i]);
            String[] messages = (String[]) arguments[2 * i + 1];
            generatedMap.put(file, messages);
        }
        return generatedMap;
    }

    private String getFilePath(String fileDirectory) throws Exception {
        String testDirectoryRelativePath = TEST_DIRECTORY_NAME + File.separator + fileDirectory;
        String testDirectoryAbsolutePath = getPath(testDirectoryRelativePath);
        File testDirectory = new File(testDirectoryAbsolutePath);
        String filePath = testDirectory + File.separator;
        return filePath;
    }

    /**
     * Custom verify to perform verification of files represented as keys in the given TreeMap<File, String[]>.
     * Values are arrays of the expected messages with their line numbers.
     * Using TreeMap structure because of its natural ordering of the keys
     * (in the method we order the actual messages as well.Check below: final List<String> actuals).
     *
     * @param checker {@link Checker} instance.
     * @param collectedFiles files to verify
     * @throws Exception if exception occurs during verification process.
     */
    private void verifyMapFiles(Checker checker, TreeMap<File, String[]> collectedFiles) throws Exception {
        stream.flush();
        // filter the files which string array is not empty
        final Map<File, String[]> expectedFiles = Maps.filterValues(collectedFiles,
                expectedMessages -> expectedMessages.length > 0);

        File[] processedFiles = expectedFiles.keySet().toArray(new File[] {});
        final ArrayList<File> theFiles = new ArrayList<>();
        Collections.addAll(theFiles, processedFiles);
        final int errs = checker.process(theFiles);

        // process each of the lines
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

        try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            final int expectedMessagesCount = expectedFiles.values().stream().distinct()
                    .mapToInt(messages -> messages.length).sum();

            final List<String> actuals = lnr.lines().limit(expectedMessagesCount).sorted().collect(Collectors.toList());
            int msgNo = 0;

            for (Map.Entry<File, String[]> entry : expectedFiles.entrySet()) {
                String file = entry.getKey().toString();
                String[] messages = entry.getValue();
                for (String message : messages) {
                    final String expectedResult = file + ":" + message;
                    assertEquals("error message " + msgNo, expectedResult, actuals.get(msgNo));
                    msgNo++;
                }
            }
            assertEquals("unexpected output: " + lnr.readLine(), expectedMessagesCount, errs);
        }
        checker.destroy();
    }
}
