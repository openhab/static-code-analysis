/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.report.ReportUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.TransformerFactoryImpl;

/**
 * Generates HTML report summaries based on the the content in the merge XML file using XSLT.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(role = SummaryReportHtmlGenerator.class)
public class SummaryReportHtmlGenerator {

    private Logger logger = LoggerFactory.getLogger(SummaryReportHtmlGenerator.class);

    private TransformerFactory transformerFactory;

    private ClassLoader contextClassLoader;

    void initialize() {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        transformerFactory = TransformerFactory.newInstance(TransformerFactoryImpl.class.getName(), contextClassLoader);
    }

    File generateHtmlSummaryByRules(final String summaryReportDirectory) {
        File latestMergeResult = new File(summaryReportDirectory, MERGE_XML_FILE_NAME);
        File latestMergeResultCopy = new File(summaryReportDirectory, SUMMARY_XML_FILE_NAME);

        try {
            // Acquire the merge and summary locks
            ReportUtil.acquireMergeLock();
            if (!latestMergeResult.exists()) {
                return null;
            }
            ReportUtil.acquireSummaryLock();

            // Copy merge.xml to summary.xml which is used for generating the report
            Files.copy(latestMergeResult.toPath(), latestMergeResultCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Release the merge lock so plugin reporting goals executed in parallel can keep merging
            ReportUtil.releaseMergeLock();

            File latestSummaryReport = new File(summaryReportDirectory, SUMMARY_REPORT_FILE_NAME);
            run(CREATE_HTML_XSLT, latestMergeResultCopy, latestSummaryReport);

            if (!latestMergeResultCopy.delete()) {
                logger.error("Unable to delete file: {}", latestMergeResultCopy.getAbsolutePath());
            }

            return latestSummaryReport;
        } catch (IOException e) {
            throw new IllegalStateException("Exception while copying latest merge result", e);
        } finally {
            ReportUtil.releaseMergeLock();
            ReportUtil.releaseSummaryLock();
        }
    }

    private void run(final String xslt, final File input, final File output) {
        try (FileOutputStream outputStream = new FileOutputStream(output);
                InputStream inputStream = contextClassLoader.getResourceAsStream(xslt);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            logger.debug("{}  > {} >  {}", input, xslt, output);

            // Process the Source into a Transformer Object
            final StreamSource source = new StreamSource(reader);
            final Transformer transformer = transformerFactory.newTransformer(source);

            final StreamResult outputTarget = new StreamResult(outputStream);
            final StreamSource xmlSource = new StreamSource(input);

            // Transform the XML Source to a Result
            Instant start = Instant.now();
            transformer.transform(xmlSource, outputTarget);
            Instant end = Instant.now();

            logger.debug("Transformation '{}' took {}ms", xslt, Duration.between(start, end).toMillis());
        } catch (IOException e) {
            logger.error("IOException occurred", e);
        } catch (TransformerException e) {
            logger.error("TransformerException occurred", e);
        }
    }
}
