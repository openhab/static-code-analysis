/**
 * Copyright (C) 2012-2013, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openhab.tools.analysis.report;

import static org.openhab.tools.analysis.report.ReportUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dom4j.dom.DOMNodeHelper.EmptyNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.saxon.TransformerFactoryImpl;

/**
 * Transforms the results from FindBugs, Checkstyle and PMD into a single HTML Report with XSLT
 *
 * @see <a href=
 *      "http://www.sw-engineering-candies.com/blog-1/howtotransformtheresultsfromfindbugscheckstyleandpmdintoasinglehtmlreportwithxslt20andjava">
 *      http://www.sw-engineering-candies.com/</a>
 * @see <a href="https://github.com/MarkusSprunck/static-code-analysis-report">https://github.com/
 *      MarkusSprunck/static-
 *      code-analysis-report</a>
 *
 * @author Markus Sprunck - Initial contribution
 * @author Svilen Valkanov - Some minor changes and adaptations
 * @author Petar Valchev - Changed the logging to be parameterized
 * @author Martin van Wingerden - added maven console logging of all messages
 * @author Wouter Born - Synchronize summary updates to make Mojo thread-safe
 */
@Mojo(name = "report", threadSafe = true)
public class ReportMojo extends AbstractMojo {

    /**
     * The directory where the individual report will be generated
     */
    @Parameter(property = "report.targetDir", defaultValue = "${project.build.directory}/code-analysis")
    private File targetDirectory;

    /**
     * Describes of the build should fail if high priority error is found
     */
    @Parameter(property = "report.fail.on.error", defaultValue = "true")
    private boolean failOnError;

    /**
     * Describes of the build should fail if medium priority error is found
     */
    @Parameter(property = "report.fail.on.warning", defaultValue = "false")
    private boolean failOnWarning;

    /**
     * Describes of the build should fail if low priority error is found
     */
    @Parameter(property = "report.fail.on.info", defaultValue = "false")
    private boolean failOnInfo;

    /**
     * The directory where the summary report, containing links to the individual reports will be
     * generated
     */
    @Parameter(property = "report.summary.targetDir", defaultValue = "${session.executionRootDirectory}/target")
    private File summaryReportDirectory;

    @Parameter(property = "report.in.maven", defaultValue = "true")
    private boolean reportInMaven;

    @Parameter(property = "report.summary.html.generation", defaultValue = "PERIODIC")
    private SummaryHtmlGeneration summaryHtmlGeneration;

    @Parameter(property = "report.summary.html.generation.period", defaultValue = "60")
    private int summaryHtmlGenerationPeriod;

    private TransformerFactory transformerFactory;

    // Setters will be used in the test
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void setFailOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
    }

    public void setFailOnInfo(boolean failOnInfo) {
        this.failOnInfo = failOnInfo;
    }

    public void setSummaryReport(File summaryReport) {
        this.summaryReportDirectory = summaryReport;
    }

    public void setReportInMaven(boolean reportInMaven) {
        this.reportInMaven = reportInMaven;
    }

    @Override
    public void execute() throws MojoFailureException {
        transformerFactory = TransformerFactory.newInstance(TransformerFactoryImpl.class.getName(),
                Thread.currentThread().getContextClassLoader());

        // Prepare userDirectory and tempDirectoryPrefix
        final String timeStamp = Integer.toHexString((int) System.nanoTime());
        Queue<File> transformedReports = new LinkedList<>();

        // 1. Create intermediate xml-file for FindBugs
        final File inputFileFindbugs = new File(targetDirectory, FINDBUGS_INPUT_FILE_NAME);
        if (inputFileFindbugs.exists()) {
            final File findbugsTempFile = new File(targetDirectory, timeStamp + "_PostFB.xml");
            run(PREPARE_FINDBUGS_XSLT, inputFileFindbugs, findbugsTempFile, EMPTY, null);
            transformedReports.add(findbugsTempFile);
        }

        // 2. Create intermediate xml-file for Checkstyle
        final File inputFileCheckstyle = new File(targetDirectory, CHECKSTYLE_INPUT_FILE_NAME);
        if (inputFileCheckstyle.exists()) {
            final File checkstyleTempFile = new File(targetDirectory, timeStamp + "_PostCS.xml");
            run(PREPARE_CHECKSTYLE_XSLT, inputFileCheckstyle, checkstyleTempFile, EMPTY, null);
            transformedReports.add(checkstyleTempFile);
        }

        // 3. Create intermediate xml-file for PMD
        final File inputFilePMD = new File(targetDirectory, PMD_INPUT_FILE_NAME);
        if (inputFilePMD.exists()) {
            final File pmdTempFile = new File(targetDirectory, timeStamp + "_PostPM.xml");
            run(PREPARE_PMD_XSLT, inputFilePMD, pmdTempFile, EMPTY, null);
            transformedReports.add(pmdTempFile);
        }

        if (!transformedReports.isEmpty()) {
            while (transformedReports.size() != 1) {
                File firstReport = transformedReports.poll();
                File secondReport = transformedReports.poll();

                // 4. Merge first two files and create merge result file
                final File mergeResult = new File(targetDirectory,
                        timeStamp + "_" + transformedReports.size() + "_Merge.xml");
                run(MERGE_XSLT, firstReport, mergeResult, "with", secondReport);

                // 5. Add the result for further merging
                transformedReports.add(mergeResult);

                deleteFile(firstReport);
                deleteFile(secondReport);
            }
            // 6. Create html report out of the last merged result
            final File htmlOutputFileName = new File(targetDirectory, RESULT_FILE_NAME);
            final File mergedReport = transformedReports.poll();
            run(CREATE_HTML_XSLT, mergedReport, htmlOutputFileName, EMPTY, null);

            // 7. Append the individual report to the summary, if it is not empty
            if (summaryReportDirectory != null) {
                ensureSummaryReportDirectoryExists();

                ReportUtil.acquireMergeLock();
                try {
                    generateSummaryByBundle(htmlOutputFileName, mergedReport);
                    generateSummaryByRules(htmlOutputFileName, mergedReport);
                } finally {
                    ReportUtil.releaseMergeLock();
                }
            }

            // 8. Report errors and warnings in Maven
            if (reportInMaven) {
                reportWarningsAndErrors(mergedReport, htmlOutputFileName);
            }

            // 9. Fail the build if any level error is enabled and configured error levels are found
            if (failOnError || failOnWarning || failOnInfo) {
                failOnErrors(mergedReport);
            }

            // 10. Delete the temporary files
            deleteFile(mergedReport);
        } else {
            getLog().info("No reports found !");
        }
    }

    private void run(final String xslt, final File input, final File output, final String param, final File value) {
        try (FileOutputStream outputStream = new FileOutputStream(output);
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslt);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            if (getLog().isDebugEnabled()) {
                getLog().debug(MessageFormat.format("{0}  > {1} {2} {3} >  {4}", input, xslt, param, value, output));
            }

            // Process the Source into a Transformer Object
            final StreamSource source = new StreamSource(reader);
            final Transformer transformer = transformerFactory.newTransformer(source);

            // Add a parameter for the transformation
            if (!param.isEmpty()) {
                transformer.setParameter(param, value.toURI().toURL());
            }

            final StreamResult outputTarget = new StreamResult(outputStream);
            final StreamSource xmlSource = new StreamSource(input);

            // Transform the XML Source to a Result
            Instant start = Instant.now();
            transformer.transform(xmlSource, outputTarget);
            Instant end = Instant.now();

            if (getLog().isDebugEnabled()) {
                getLog().debug(MessageFormat.format("Transformation ''{0}'' took {1}ms", xslt,
                        Duration.between(start, end).toMillis()));
            }
        } catch (IOException e) {
            getLog().error("IOException occurred", e);
        } catch (TransformerException e) {
            getLog().error("TransformerException occurred", e);
        }
    }

    private void copyFile(File source, File target) throws IOException {
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteFile(File file) {
        if (!file.delete()) {
            getLog().error("Unable to delete file " + file.getAbsolutePath());
        }
    }

    private void reportWarningsAndErrors(File mergedReport, File reportLocation) {
        NodeList messages = selectNodes(mergedReport, "/sca/file/message");
        int messageCount = messages.getLength();

        int errorCount = countPriority(messages, "1");
        int warnCount = countPriority(messages, "2");
        int infoCount = countPriority(messages, "3");

        if (messageCount == 0) {
            return;
        }

        String format = String.format("Code Analysis Tool has found: %n %d error(s)! %n %d warning(s) %n %d info(s)",
                errorCount, warnCount, infoCount);
        report(maxLevel(errorCount, warnCount, infoCount), format);

        for (int i = 0; i < messages.getLength(); i++) {
            Node currentNode = messages.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element messageNode = (Element) currentNode;
                String priority = messageNode.getAttribute("priority");

                Element fileNode = ((Element) messageNode.getParentNode());
                String fileName = fileNode.getAttribute("name");
                String line = messageNode.getAttribute("line");
                String message = messageNode.getAttribute("message").trim();

                String logTemplate = "%s:[%s]%n%s";
                String log = String.format(logTemplate, fileName, line, message);
                report(priority, log);
            }
        }
        getLog().info("Detailed report can be found at: " + reportLocation.toURI());
    }

    private String maxLevel(int errorCount, int warnCount, int infoCount) {
        if (errorCount > 0) {
            return "1";
        } else if (warnCount > 0) {
            return "2";
        } else {
            return "3";
        }
    }

    private int countPriority(NodeList messages, String priority) {
        int count = 0;
        for (int i = 0; i < messages.getLength(); i++) {
            Node currentNode = messages.item(i);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element messageNode = (Element) currentNode;

            if (priority.equals(messageNode.getAttribute("priority"))) {
                count++;
            }
        }
        return count;
    }

    private void failOnErrors(File mergedReport) throws MojoFailureException {
        List<String> errorMessages = new ArrayList<>();
        if (failOnError) {
            detectFailures(errorMessages, mergedReport, 1);
        }
        if (failOnWarning) {
            detectFailures(errorMessages, mergedReport, 2);
        }
        if (failOnInfo) {
            detectFailures(errorMessages, mergedReport, 3);
        }
        if (!errorMessages.isEmpty()) {
            throw new MojoFailureException(String.join("\n", errorMessages));
        }
    }

    private void detectFailures(List<String> errorMessages, File mergedReport, int priority) {
        NodeList messages = selectNodes(mergedReport, "/sca/file/message");
        int count = countPriority(messages, String.valueOf(priority));
        if (count > 0) {
            errorMessages.add(failureMessage(priority(priority), count));
        }
    }

    private String priority(int priority) {
        switch (priority) {
            case 1:
                return "error";
            case 2:
                return "warning";
            case 3:
            default:
                return "info";
        }
    }

    private String failureMessage(String severity, int count) {
        return String.format("%nCode Analysis Tool has found %d %s(s)! %nPlease fix the %s(s) and rerun the build.",
                count, severity, severity);
    }

    private void report(String priority, String log) {
        switch (priority) {
            case "1":
                getLog().error(log);
                break;
            case "2":
                getLog().warn(log);
                break;
            case "3":
            default:
                getLog().debug(log);
        }
    }

    private void ensureSummaryReportDirectoryExists() {
        if (!summaryReportDirectory.exists()) {
            summaryReportDirectory.mkdirs();
        }
    }

    private void generateSummaryByBundle(File htmlOutputFile, File mergedReport) {
        NodeList nodes = selectNodes(mergedReport, "/sca/file/message");
        int messagesNumber = nodes.getLength();
        if (messagesNumber == 0) {
            getLog().info("Empty report will not be appended to the summary report.");
            return;
        }

        try {
            File summaryReport = new File(summaryReportDirectory, SUMMARY_BUNDLES_FILE_NAME);
            if (!summaryReport.exists()) {
                InputStream inputStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(REPORT_SUBDIR + "/" + SUMMARY_TEMPLATE_FILE_NAME);

                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, Charset.defaultCharset());
                String htmlString = writer.toString();

                DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss ")
                        .appendOffset("+HH:MM", "Z").toFormatter();
                htmlString = htmlString.replace("$time", formatter.format(ZonedDateTime.now()));

                FileUtils.writeStringToFile(summaryReport, htmlString, StandardCharsets.UTF_8);
            }
            String reportContent = FileUtils.readFileToString(summaryReport, StandardCharsets.UTF_8);

            final String singleItem = "<tr class=alternate><td><a href=\"%s\">%s</a></td></tr><tr></tr>";
            Path absoluteIndividualReportPath = htmlOutputFile.toPath();
            Path summaryReportDirectoryPath = summaryReportDirectory.toPath();
            Path relativePath = summaryReportDirectoryPath.relativize(absoluteIndividualReportPath);

            String bundleName = absoluteIndividualReportPath.getName(absoluteIndividualReportPath.getNameCount() - 4)
                    .toString();

            String row = String.format(singleItem, relativePath, bundleName);

            reportContent = reportContent.replace("<tr></tr>", row);
            FileUtils.writeStringToFile(summaryReport, reportContent, StandardCharsets.UTF_8);
            getLog().info("Individual report appended to summary report.");
        } catch (IOException e) {
            getLog().warn("Can't read or write to summary report. The summary report might be incomplete!", e);
        }
    }

    private void generateSummaryByRules(final File htmlOutputFileName, final File mergedReport) {
        File latestMergeResult = new File(summaryReportDirectory, MERGE_XML_FILE_NAME);
        File latestSummaryReport = new File(summaryReportDirectory, SUMMARY_REPORT_FILE_NAME);

        try {
            if (!latestMergeResult.exists() && !latestSummaryReport.exists()) {
                latestMergeResult.createNewFile();
                latestSummaryReport.createNewFile();
                copyFile(mergedReport, latestMergeResult);
                copyFile(htmlOutputFileName, latestSummaryReport);
            } else {
                final File tempMergedReport = new File(summaryReportDirectory, MERGE_XML_TMP_FILE_NAME);
                copyFile(latestMergeResult, tempMergedReport);
                run(MERGE_XSLT, tempMergedReport, latestMergeResult, "with", mergedReport);
                deleteFile(tempMergedReport);
            }
        } catch (IOException e) {
            getLog().error("Unable to create or write to file " + e.getMessage(), e);
        }
    }

    private NodeList selectNodes(File file, String xPathExpression) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression expression = xPath.compile(xPathExpression);
            return (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        } catch (Exception e) {
            String message = MessageFormat.format("Can't select {0} nodes from {1}. Empty NodeList will be returned.",
                    xPathExpression, file.getAbsolutePath());
            getLog().warn(message, e);
            return new EmptyNodeList();
        }
    }
}
