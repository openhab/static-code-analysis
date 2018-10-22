/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.osgi.core.BundleInfo;
import org.apache.ivy.osgi.core.ManifestParser;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.internal.core.text.build.BuildModel;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;

/**
 * Provides common functionality for different static code analysis checks
 *
 * @author Svilen Valkanov - Initial contribution, add Exception to
 *         findLineNumber method
 * @author Mihaela Memova - Simplify findLineNumber method
 * @author Velin Yordanov - Used FileText instead of File to avoid unnecessary
 *         IO
 */
public abstract class AbstractStaticCheck extends AbstractFileSetCheck {
    private Log logger = LogFactory.getLog(AbstractStaticCheck.class);
    private long startTime;
    private long methodTime;

    public void beginProcessing(String charset) {
        startTime = System.nanoTime();
    }

    public void finishProcessing() {
        methodTime = (System.nanoTime() - startTime) / 1000000;
        try (FileWriter writer = new FileWriter("measurements.txt",true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.append(this.getClass().getSimpleName() + " : " + methodTime);
            bufferedWriter.newLine();
        } catch (IOException e) {
            logger.error("Error in writing to or creating measurements.txt", e);
        }
    }
    
    /**
     * Finds the first occurrence of a text in a list of text lines representing the
     * file content and returns the line number, where the text was found
     *
     *
     * @param fileContent
     *            - represents the text content
     * @param searchedText
     *            - the text that we are looking for
     * @param startLineNumber
     *            - the line number from which the search starts exclusive, to start
     *            the search of the beginning of the text the startLineNumber should
     *            be 0
     * @return the number of the line starting from 1, where the searched text
     *         occurred for the first time
     * @throws NoResultException
     *             when no match was found
     */
    protected int findLineNumber(FileText fileContent, String searchedText, int startLineNumber)
            throws NoResultException {
        for (int lineNumber = startLineNumber; lineNumber < fileContent.size(); lineNumber++) {
            String line = fileContent.get(lineNumber);
            if (line.contains(searchedText)) {
                // The +1 is to compensate the 0-based list and the 1-based text file
                return lineNumber + 1;
            }
        }
        String message = MessageFormat.format(
                "`{0}` was not found in the file {1} starting from line `{2}`."
                        + " Check if it is split between multiple lines or it is missing",
                searchedText, fileContent.getFile().getAbsolutePath(), startLineNumber);
        throw new NoResultException(message);
    }

    /**
     * Finds the first occurrence of a text in a list of text lines representing the
     * file content and returns the line number, where the text was found
     *
     * @param fileText
     *            - represents the content of a file
     * @param searchedText
     *            - the text that we are looking for
     * @param startLineNumber
     *            - the line number from which the search starts exclusive, to start
     *            the search of the beginning of the text the startLineNumber should
     *            be 0
     * @param warningMessage
     *            - message to be logged as warning in case no match is found
     * @return the number of the line starting from 1, where the searched text
     *         occurred for the first time, or 0 if no matches are found
     */
    protected int findLineNumberSafe(FileText fileText, String searchedText, int startLineNumber,
            String warningMessage) {
        try {
            return findLineNumber(fileText, searchedText, startLineNumber);
        } catch (NoResultException e) {
            logger.warn(warningMessage + " Fall back to 0.", e);
            return 0;
        }
    }

    /**
     * Parses the content of the given file as an XML document.
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @return DOM Document object
     * @throws CheckstyleException
     *             - if an error occurred while trying to parse the file
     */
    protected Document parseDomDocumentFromFile(FileText fileText) throws CheckstyleException {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(getInputStream(fileText));
            return document;
        } catch (ParserConfigurationException e) {
            throw new CheckstyleException("Serious configuration error occured while creating a DocumentBuilder.", e);
        } catch (SAXException e) {
            throw new CheckstyleException("Unable to read from file: " + fileText.getFile().getAbsolutePath(), e);
        } catch (IOException e) {
            throw new CheckstyleException("Unable to open file: " + fileText.getFile().getAbsolutePath(), e);
        }
    }

    /**
     * Parses the content of the given Manifest file
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @return Bundle info extracted from the bundle manifest
     * @throws CheckstyleException
     *             - if an error occurred while trying to parse the file
     */
    protected BundleInfo parseManifestFromFile(FileText fileText) throws CheckstyleException {
        try {
            BundleInfo info = ManifestParser.parseManifest(getInputStream(fileText));
            return info;
        } catch (IOException e) {
            throw new CheckstyleException("Unable to read from file: " + fileText.getFile().getAbsolutePath(), e);
        } catch (ParseException e) {
            throw new CheckstyleException("Unable to parse file:" + fileText.getFile().getAbsolutePath(), e);
        }
    }

    /**
     * Reads a properties list from a file
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @return Properties object containing all the read properties
     * @throws CheckstyleException
     *             - if an error occurred while trying to parse the file
     */
    protected Properties readPropertiesFromFile(FileText fileText) throws CheckstyleException {

        try {
            Properties properties = new Properties();
            properties.load(getInputStream(fileText));
            return properties;
        } catch (FileNotFoundException e) {
            throw new CheckstyleException("File: " + fileText.getFile().getAbsolutePath() + " does not exist.", e);
        } catch (IOException e) {
            throw new CheckstyleException("Unable to read properties from: " + fileText.getFile().getAbsolutePath(), e);
        }
    }

    /**
     * Parses the content of a given file as a HTML file
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @return HTML Document representation of the file
     */
    protected org.jsoup.nodes.Document parseHTMLDocumentFromFile(FileText fileText) {
        String fileContent = fileText.getFullText().toString();
        return Jsoup.parse(fileContent);
    }

    /**
     * Compiles an XPathExpression
     *
     * @param expresion
     *            - the XPath expression
     * @return compiled XPath expression
     * @throws CheckstyleException
     *             if an error occurred during the compilation
     */
    protected XPathExpression compileXPathExpression(String expresion) throws CheckstyleException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        try {
            return xpath.compile(expresion);
        } catch (XPathExpressionException e) {
            throw new CheckstyleException("Unable to compile the expression" + expresion, e);
        }
    }

    /**
     * Parses the content of a given file as a build.properties file
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @return IBuild representation of the file
     * @throws CheckstyleException
     *             - if an error occurred while trying to parse the file
     */
    protected IBuild parseBuildProperties(FileText fileText) throws CheckstyleException {
        IDocument document = new SynchronizableDocument();
        BuildModel buildModel = new BuildModel(document, false);
        try {
            buildModel.load(getInputStream(fileText), true);
            return buildModel.getBuild();
        } catch (CoreException e) {
            throw new CheckstyleException("Unable to read build.properties file", e);
        }
    }

    /**
     * Checks whether a file is empty
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @return true if the file is empty, otherwise false
     */
    protected boolean isEmpty(FileText fileText) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getInputStream(fileText)))) {
            if (bufferedReader.readLine() == null) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Adds an entry in the report using the {@link MessageDispatcher}. Can be used
     * in the {@link #finishProcessing()} where the
     * {@link #log(int, String, Object...)} methods can't be used as the entries
     * logged by them won't be included in the report.
     *
     * @param filePath
     *            the absolute path to the file. Although a relative path can be
     *            used, it is not recommended as it will make filtering harder
     * @param line
     *            the line that will be added in the report
     * @param fileName
     *            the name the file
     * @param message
     *            the message that will be logged
     */
    protected void logMessage(String filePath, int line, String fileName, String message) {
        MessageDispatcher dispatcher = getMessageDispatcher();
        dispatcher.fireFileStarted(filePath);
        log(line, message, fileName);
        fireErrors(filePath);
        dispatcher.fireFileFinished(filePath);
    }

    /**
     * Parsed the content of a markdown file.
     *
     * @param fileText
     *            - Represents the text contents of a file
     * @param parsingOptions
     *            - parsing options
     * @return The markdown node
     */
    protected Node parseMarkdown(FileText fileText, MutableDataSet parsingOptions) {
        Parser parser = Parser.builder(parsingOptions).build();
        return parser.parse(fileText.getFullText().toString());
    }

    private InputStream getInputStream(FileText fileText) {
        return new ByteArrayInputStream(fileText.getFullText().toString().getBytes(fileText.getCharset()));
    }
}
