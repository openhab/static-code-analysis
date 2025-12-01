/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.tools.analysis.report;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.sf.saxon.TransformerFactoryImpl;

/**
 * Tests for the prepare_checkstyle.xslt transformation
 *
 * @author Wouter Born - Initial contribution
 */
public class PrepareCheckstyleXsltTest {

    private static final String XSLT_PATH = "report/prepare_checkstyle.xslt";
    private TransformerFactory transformerFactory;

    @BeforeEach
    public void setUp() {
        transformerFactory = TransformerFactory.newInstance(TransformerFactoryImpl.class.getName(),
                Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void testResourceFilePathExtraction() throws Exception {
        // Test that files in src/main/resources are correctly transformed
        String checkstyleInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<checkstyle version=\"10.0\">"
                + "<file name=\"/opt/openhab-addons/bundles/org.openhab.binding.test/src/main/resources/OH-INF/config/config.xml\">"
                + "<error line=\"5\" severity=\"error\" message=\"Test error message\" source=\"org.openhab.tools.analysis.checkstyle.OnlyTabIndentationCheck\"/>"
                + "</file>" + "</checkstyle>";

        String result = transform(checkstyleInput);

        // Verify the file element is created with the correct name
        assertTrue(result.contains("<file name=\"OH-INF.config.config.xml\">"),
                "File name should be extracted from resources path. Actual result: " + result);
        assertTrue(result.contains("<message"), "Message element should be present. Actual result: " + result);
        assertTrue(result.contains("tool=\"checkstyle\""),
                "Tool attribute should be checkstyle. Actual result: " + result);
        assertTrue(result.contains("rule=\"OnlyTabIndentationCheck\""),
                "Rule should be OnlyTabIndentationCheck. Actual result: " + result);
    }

    @Test
    public void testJavaFilePathExtraction() throws Exception {
        // Test that Java files in src/main/java are correctly transformed
        String checkstyleInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<checkstyle version=\"10.0\">"
                + "<file name=\"/opt/openhab/bundles/org.openhab.core/src/main/java/org/openhab/core/Test.java\">"
                + "<error line=\"10\" severity=\"warning\" message=\"Test warning\" source=\"com.puppycrawl.tools.checkstyle.checks.TestCheck\"/>"
                + "</file>" + "</checkstyle>";

        String result = transform(checkstyleInput);

        // Verify the file element is created with the correct name (path after .java.)
        assertTrue(result.contains("<file name=\"org.openhab.core.Test.java\">"),
                "File name should be extracted from java path. Actual result: " + result);
    }

    @Test
    public void testWindowsPathWithBackslashes() throws Exception {
        // Test that Windows paths with backslashes are correctly handled
        String checkstyleInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<checkstyle version=\"10.0\">"
                + "<file name=\"C:\\prj\\openhab\\bundles\\org.openhab.binding.test\\src\\main\\resources\\OH-INF\\thing\\thing-types.xml\">"
                + "<error line=\"3\" severity=\"error\" message=\"Whitespace issue\" source=\"org.openhab.tools.analysis.checkstyle.OnlyTabIndentationCheck\"/>"
                + "</file>" + "</checkstyle>";

        String result = transform(checkstyleInput);

        // Verify the file element is created with the correct name (backslashes converted to dots)
        assertTrue(result.contains("<file name=\"OH-INF.thing.thing-types.xml\">"),
                "Windows path should be correctly transformed. Actual result: " + result);
    }

    @Test
    public void testMultipleFilesWithErrors() throws Exception {
        // Test that multiple files with errors are all included
        String checkstyleInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<checkstyle version=\"10.0\">"
                + "<file name=\"/opt/openhab/bundles/org.openhab.automation.jsscripting/src/main/resources/OH-INF/config/config.xml\">"
                + "<error line=\"5\" severity=\"error\" message=\"Whitespace error 1\" source=\"org.openhab.tools.analysis.checkstyle.OnlyTabIndentationCheck\"/>"
                + "</file>"
                + "<file name=\"/opt/openhab/bundles/org.openhab.binding.huesync/src/main/resources/OH-INF/thing/channel-types.xml\">"
                + "<error line=\"10\" severity=\"error\" message=\"Whitespace error 2\" source=\"org.openhab.tools.analysis.checkstyle.OnlyTabIndentationCheck\"/>"
                + "</file>" + "</checkstyle>";

        String result = transform(checkstyleInput);

        // Verify both files are present
        assertTrue(result.contains("<file name=\"OH-INF.config.config.xml\">"),
                "First file should be present. Actual result: " + result);
        assertTrue(result.contains("<file name=\"OH-INF.thing.channel-types.xml\">"),
                "Second file should be present. Actual result: " + result);
    }

    @Test
    public void testFileWithoutErrorsIsExcluded() throws Exception {
        // Test that files without errors are NOT included in the output
        String checkstyleInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<checkstyle version=\"10.0\">"
                + "<file name=\"/opt/openhab/bundles/org.openhab.core/src/main/java/org/openhab/core/Clean.java\">"
                + "</file>" + "</checkstyle>";

        String result = transform(checkstyleInput);

        // Verify the file without errors is not included
        assertFalse(result.contains("<file name=\"org.openhab.core.Clean.java\">"),
                "File without errors should NOT be included. Actual result: " + result);
    }

    private String transform(String inputXml) throws Exception {
        try (InputStream xsltStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(XSLT_PATH)) {
            assertNotNull(xsltStream, "XSLT file should be found on classpath");

            StreamSource xsltSource = new StreamSource(xsltStream);
            Transformer transformer = transformerFactory.newTransformer(xsltSource);

            StringWriter writer = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(inputXml)), new StreamResult(writer));

            return writer.toString();
        }
    }
}
