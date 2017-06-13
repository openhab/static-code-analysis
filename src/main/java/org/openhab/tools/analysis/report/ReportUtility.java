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
 *
 */
package org.openhab.tools.analysis.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * @author Markus Sprunck - Initial Implementation
 * @author Svilen Valkanov - Some minor changes and adaptations
 * @author Petar Valchev - Changed the logging to be parameterized
 */

@Mojo(name = "report")
public class ReportUtility extends AbstractMojo {

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
     * The directory where the summary report, containing links to the individual reports will be
     * generated
     */
    @Parameter(property = "report.summary.targetDir", defaultValue = "${session.executionRootDirectory}/target")
    private File summaryReportDirectory;

    private static final String REPORT_SUBDIR = "report";

    // XSLT files that are used to create the merged report, located in the resources folder
    private static final String CREATE_HTML_XSLT = REPORT_SUBDIR + "/create_html.xslt";
    private static final String MERGE_XSLT = REPORT_SUBDIR + "/merge.xslt";
    private static final String PREPARE_PMD_XSLT = REPORT_SUBDIR + "/prepare_pmd.xslt";
    private static final String PREPARE_CHECKSTYLE_XSLT = REPORT_SUBDIR + "/prepare_checkstyle.xslt";
    private static final String PREPARE_FINDBUGS_XSLT = REPORT_SUBDIR + "/prepare_findbugs.xslt";

    private static final String SUMMARY_TEMPLATE_FILE_NAME = "summary.html";

    // Input files that contain the reports of the different tools
    private static final String PMD_INPUT_FILE_NAME = "pmd.xml";
    private static final String CHECKSTYLE_INPUT_FILE_NAME = "checkstyle-result.xml";
    private static final String FINDBUGS_INPUT_FILE_NAME = "findbugsXml.xml";

    // Name of the file that contains the merged report
    public static final String RESULT_FILE_NAME = "report.html";
    public static final String SUMMARY_FILE_NAME = "summary_report.html";

    private static final String EMPTY = "";
    private static final String XML_TRANSFORM_PROPERTY_KEY = "javax.xml.transform.TransformerFactory";
    private static final String XML_TRANSFOMR_PROPERTY_VALUE = "net.sf.saxon.TransformerFactoryImpl";

    private final Logger logger = LoggerFactory.getLogger(ReportUtility.class);

    // Setters will be used in the test
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void setSummaryReport(File summaryReport) {
        this.summaryReportDirectory = summaryReport;
    }

    @Override
    public void execute() throws MojoFailureException {
        // Prepare userDirectory and tempDirectoryPrefix
        final String timeStamp = Integer.toHexString((int) System.nanoTime());

        System.setProperty(XML_TRANSFORM_PROPERTY_KEY, XML_TRANSFOMR_PROPERTY_VALUE);
        // 1. Create intermediate xml-file for Findbugs
        final File inputFileFindbugs = new File(targetDirectory, FINDBUGS_INPUT_FILE_NAME);
        final File findbugsTempFile = new File(targetDirectory, timeStamp + "_PostFB.xml");
        run(PREPARE_FINDBUGS_XSLT, inputFileFindbugs, findbugsTempFile, EMPTY, null);

        // 2. Create intermediate xml-file for Checkstyle
        final File inputFileCheckstyle = new File(targetDirectory, CHECKSTYLE_INPUT_FILE_NAME);
        final File checkstyleTempFile = new File(targetDirectory, timeStamp + "_PostCS.xml");
        run(PREPARE_CHECKSTYLE_XSLT, inputFileCheckstyle, checkstyleTempFile, EMPTY, null);

        // 3. Create intermediate xml-file for PMD
        final File inputFilePMD = new File(targetDirectory, PMD_INPUT_FILE_NAME);
        final File pmdTempFile = new File(targetDirectory, timeStamp + "_PostPM.xml");
        run(PREPARE_PMD_XSLT, inputFilePMD, pmdTempFile, EMPTY, null);

        // 4. Merge first two files and create firstMergeResult file
        final File firstMergeResult = new File(targetDirectory, timeStamp + "_FirstMerge.xml");
        run(MERGE_XSLT, checkstyleTempFile, firstMergeResult, "with", findbugsTempFile);

        // 5. Merge result file with third file and create secondMergeResult
        // file
        final File secondMergeResult = new File(targetDirectory, timeStamp + "_SecondMerge.xml");
        run(MERGE_XSLT, firstMergeResult, secondMergeResult, "with", pmdTempFile);

        // 6. Create html report out of secondMergeResult
        final File htmlOutputFileName = new File(targetDirectory, RESULT_FILE_NAME);
        run(CREATE_HTML_XSLT, secondMergeResult, htmlOutputFileName, EMPTY, null);

        // 7. Append the individual report to the summary, if it is not empty
        if (summaryReportDirectory != null) {
            appendToSummary(htmlOutputFileName, summaryReportDirectory, secondMergeResult);
        }

        // 8. Fail the build if the option is enabled and high priority warnings are found
        if (failOnError) {
            String errorLog = checkForErrors(secondMergeResult, htmlOutputFileName);
            if (errorLog != null) {
                throw new MojoFailureException(errorLog);
            }
        }

        // 9. Delete all temporary files
        deletefile(findbugsTempFile);
        deletefile(checkstyleTempFile);
        deletefile(pmdTempFile);
        deletefile(firstMergeResult);
        deletefile(secondMergeResult);

    }

    private void run(final String xslt, final File input, final File output, final String param, final File value) {

        FileOutputStream outputStream = null;
        try {

            logger.debug("{}  > {} {} {} >  {}", input, xslt, param, value, output);

            // Process the Source into a Transformer Object
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslt);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final StreamSource source = new StreamSource(reader);
            final Transformer transformer = transformerFactory.newTransformer(source);

            // Add a parameter for the transformation
            if (!param.isEmpty()) {
                transformer.setParameter(param, value.toURI().toURL());
            }

            outputStream = new FileOutputStream(output);
            final StreamResult outputTarget = new StreamResult(outputStream);
            final StreamSource xmlSource = new StreamSource(input);

            // Transform the XML Source to a Result
            transformer.transform(xmlSource, outputTarget);

        } catch (IOException e) {
            logger.error("IOException occcured " + e.getMessage());
        } catch (TransformerException e) {
            logger.error("TransformerException occcured " + e.getMessage());
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    void deletefile(File file) {
        if (!file.delete()) {
            logger.error("Unable to delete file {}", file.getAbsolutePath());
        }
    }

    private String checkForErrors(File secondMergeResult, File reportLocation) {
        NodeList nodes = selectNodes(secondMergeResult, "/sca/file/message[@priority=1]");
        int errorNumber = nodes.getLength();

        if (errorNumber == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        result.append("Code Analysis Tool has found ").append(errorNumber).append(" error(s)! \n");
        result.append("Please fix the errors and rerun the build. \n");
        result.append("Errors list: \n");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentNode = nodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element messageNode = (Element) currentNode;
                String message = messageNode.getAttribute("message");
                String tool = messageNode.getAttribute("tool");
                String line = messageNode.getAttribute("line");

                Element fileNode = ((Element) messageNode.getParentNode());
                String fileName = fileNode.getAttribute("name");

                String logTemplate = "ERROR found by %s: %s:%s %s \n";
                String log = String.format(logTemplate, tool, fileName, line, message);
                result.append(log);
            }
        }
        result.append("Detailed report can be found at: file:///").append(reportLocation).append("\n");
        return result.toString();

    }

    private void appendToSummary(File htmlOutputFileName, File summaryReportDirectory, File secondMergeResult) {
        NodeList nodes = selectNodes(secondMergeResult, "/sca/file/message");
        int messagesNumber = nodes.getLength();
        if (messagesNumber == 0) {
            logger.info("Empty report will not be appended to the summary report.");
            return;
        }

        try {
            File summaryReport = new File(summaryReportDirectory + File.separator + SUMMARY_FILE_NAME);
            if (!summaryReport.exists()) {

                InputStream inputStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(REPORT_SUBDIR + "/" + SUMMARY_TEMPLATE_FILE_NAME);

                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, Charset.defaultCharset());
                String htmlString = writer.toString();

                String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                htmlString = htmlString.replace("$time", now);

                FileUtils.writeStringToFile(summaryReport, htmlString);
            }
            String reportContent = FileUtils.readFileToString(summaryReport);

            final String singleItem = "<tr class=alternate><td><a href=\"%s\">%s</a></td></tr><tr></tr>";
            Path absoluteIndividualReportPath = htmlOutputFileName.toPath();
            Path summaryReportDirectoryPath = summaryReportDirectory.toPath();
            Path relativePath = summaryReportDirectoryPath.relativize(absoluteIndividualReportPath);

            String bundleName = absoluteIndividualReportPath.getName(absoluteIndividualReportPath.getNameCount() - 4)
                    .toString();

            String row = String.format(singleItem, relativePath, bundleName);

            reportContent = reportContent.replace("<tr></tr>", row);
            FileUtils.writeStringToFile(summaryReport, reportContent);
            logger.info("Individual report appended to summary report.");
        } catch (IOException e) {
            logger.warn("Can't read or write to summary report. The summary report might be incomplete!", e);
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
            logger.warn("Can't select {} nodes from {}. Empty NodeList will be returned.", xPathExpression,
                    file.getAbsolutePath(), e);
            return new EmptyNodeList();
        }

    }

}
