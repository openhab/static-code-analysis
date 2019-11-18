/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLockInterruptionException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import com.google.common.io.Files;

import net.sf.saxon.TransformerFactoryImpl;

/**
 * Generates HTML report summaries based on the the content in the merge XML file using XSLT.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(role = SummaryReportHtmlGenerator.class)
public class SummaryReportHtmlGenerator {

    @Requirement
    private Logger logger;

    private TransformerFactory transformerFactory;

    private ClassLoader contextClassLoader;

    void initialize() {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        transformerFactory = TransformerFactory.newInstance(TransformerFactoryImpl.class.getName(), contextClassLoader);
    }

    File generateHtmlSummaryByRules(final String summaryReportDirectory) {
        File latestMergeResult = new File(summaryReportDirectory, MERGE_XML_FILE_NAME);
        File latestMergeResultCopy = new File(summaryReportDirectory, SUMMARY_XML_FILE_NAME);

        FileChannel mergeLockFileChannel = null;
        FileChannel summaryLockFileChannel = null;

        try {
            // Acquire the merge and summary locks
            mergeLockFileChannel = acquireFileLock(summaryReportDirectory, MERGE_LOCK_FILE_NAME);
            if (!latestMergeResult.exists()) {
                return null;
            }
            summaryLockFileChannel = acquireFileLock(summaryReportDirectory, SUMMARY_LOCK_FILE_NAME);

            // Copy merge.xml to summary.xml which is used for generating the report
            Files.copy(latestMergeResult, latestMergeResultCopy);

            // Release the merge lock so plugin reporting goals executed in parallel can keep merging
            closeFileChannel(mergeLockFileChannel);

            File latestSummaryReport = new File(summaryReportDirectory, SUMMARY_REPORT_FILE_NAME);
            run(CREATE_HTML_XSLT, latestMergeResultCopy, latestSummaryReport);

            if (!latestMergeResultCopy.delete()) {
                logger.error("Unable to delete file: " + latestMergeResultCopy.getAbsolutePath());
            }

            return latestSummaryReport;
        } catch (InterruptedException | FileLockInterruptionException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new IllegalStateException("Exception while acquiring lock file", e);
        } finally {
            closeFileChannel(mergeLockFileChannel);
            closeFileChannel(summaryLockFileChannel);
        }
        return null;
    }

    private void closeFileChannel(FileChannel fileChannel) {
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                logger.error("Exception while closing file channel: " + fileChannel, e);
            }
        }
    }

    private FileChannel acquireFileLock(final String summaryReportDirectory, final String fileName)
            throws InterruptedException, FileLockInterruptionException {
        File mergeLockFile = new File(summaryReportDirectory, fileName);
        try {
            return ReportUtil.acquireFileLock(mergeLockFile);
        } catch (IOException e) {
            throw new IllegalStateException("Exception while acquiring lock file: " + mergeLockFile, e);
        }
    }

    private void run(final String xslt, final File input, final File output) {
        try (FileOutputStream outputStream = new FileOutputStream(output)) {
            if (logger.isDebugEnabled()) {
                logger.debug(MessageFormat.format("{0}  > {1} >  {2}", input, xslt, output));
            }

            // Process the Source into a Transformer Object
            final InputStream inputStream = contextClassLoader.getResourceAsStream(xslt);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final StreamSource source = new StreamSource(reader);

            final Transformer transformer = transformerFactory.newTransformer(source);

            final StreamResult outputTarget = new StreamResult(outputStream);
            final StreamSource xmlSource = new StreamSource(input);

            // Transform the XML Source to a Result
            Instant start = Instant.now();
            transformer.transform(xmlSource, outputTarget);
            Instant end = Instant.now();

            if (logger.isDebugEnabled()) {
                logger.debug(MessageFormat.format("Transformation ''{0}'' took {1}ms", xslt,
                        Duration.between(start, end).toMillis()));
            }
        } catch (IOException e) {
            logger.error("IOException occurred", e);
        } catch (TransformerException e) {
            logger.error("TransformerException occurred", e);
        }
    }

}
