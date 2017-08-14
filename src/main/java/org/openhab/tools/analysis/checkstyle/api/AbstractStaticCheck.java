/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;

/**
 * Provides common functionality for different static code analysis checks
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Mihaela Memova - Simplify findLineNumber method
 *
 */
public abstract class AbstractStaticCheck extends AbstractFileSetCheck {

    /**
     * Finds the first occurrence of a text in a list of text lines representing the file content and
     * returns the line number, where the text was found
     *
     * @param fileContent - each element of the list represents a line from the file
     * @param searchedText - the text that we are looking for
     * @param startLineNumber - the line number from which the search starts exclusive, to start the
     *            search of the beginning of the text the startLineNumber should be 0
     * @return the number of the line starting from 1, where the searched text occurred for the first
     *         time, or -1 if
     *         no match was found
     */
    protected int findLineNumber(String[] fileContent, String searchedText, int startLineNumber) {
        for (int lineNumber = startLineNumber; lineNumber < fileContent.length; lineNumber++) {
            String line = fileContent[lineNumber];
            if (line.contains(searchedText)) {
                // The +1 is to compensate the 0-based list and the 1-based text file
                return lineNumber + 1;
            }
        }
        return -1;
    }

    /**
     * Parses the content of the given file as an XML document.
     *
     * @param file - the input file
     * @return DOM Document object
     * @throws CheckstyleException - if an error occurred while trying to parse the file
     */
    protected Document parseDomDocumentFromFile(File file) throws CheckstyleException {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(file);
            return document;

        } catch (ParserConfigurationException e) {
            throw new CheckstyleException("Serious configuration error occured while creating a DocumentBuilder.", e);
        } catch (SAXException e) {
            throw new CheckstyleException("Unable to read from file: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new CheckstyleException("Unable to open file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Parses the content of the given Manifest file
     *
     * @param file - the input file
     * @return Bundle info extracted from the bundle manifest
     * @throws CheckstyleException - if an error occurred while trying to parse the file
     */
    protected BundleInfo parseManifestFromFile(File file) throws CheckstyleException {
        try {
            BundleInfo info = ManifestParser.parseManifest(file);
            return info;
        } catch (IOException e) {
            throw new CheckstyleException("Unable to read from file: " + file.getAbsolutePath(), e);
        } catch (ParseException e) {
            throw new CheckstyleException("Unable to parse file:" + file.getAbsolutePath(), e);
        }
    }

    /**
     * Reads a properties list from a file
     *
     * @param file - the input file
     * @return Properties object containing all the read properties
     * @throws CheckstyleException - if an error occurred while trying to parse the file
     */
    protected Properties readPropertiesFromFile(File file) throws CheckstyleException {
        String filePath = file.getAbsolutePath();

        try (InputStream buildPropertiesInputStream = new FileInputStream(filePath)) {
            Properties properties = new Properties();
            properties.load(buildPropertiesInputStream);
            return properties;
        } catch (FileNotFoundException e) {
            throw new CheckstyleException("File: " + file.getAbsolutePath() + " does not exist.", e);
        } catch (IOException e) {
            throw new CheckstyleException("Unable to read properties from: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Parses the content of a given file as a HTML file
     *
     * @param file - the input file
     * @return HTML Document representation of the file
     * @throws CheckstyleException - if an error occurred while trying to parse the file
     */
    protected org.jsoup.nodes.Document parseHTMLDocumentFromFile(File file) throws CheckstyleException {
        try {
            byte[] fileByteArray = Files.readAllBytes(file.toPath());
            String fileContent = new String(fileByteArray);
            org.jsoup.nodes.Document fileDocument = Jsoup.parse(fileContent);
            return fileDocument;
        } catch (IOException e) {
            throw new CheckstyleException("Unable to read the content of the file" + file.getAbsolutePath(), e);
        }

    }

    /**
     * Compiles an XPathExpression
     *
     * @param expresion - the XPath expression
     * @return compiled XPath expression
     * @throws CheckstyleException if an error occurred during the compilation
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
     * @param file - the input file
     * @return IBuild representation of the file
     * @throws CheckstyleException - if an error occurred while trying to parse the file
     */
    protected IBuild parseBuildProperties(File file) throws CheckstyleException {
        IDocument document = new SynchronizableDocument();
        BuildModel buildModel = new BuildModel(document, false);
        try {
            buildModel.load(new FileInputStream(file), true);
            return buildModel.getBuild();
        } catch (FileNotFoundException e) {
            throw new CheckstyleException("File: " + file.getAbsolutePath() + " does not exist.", e);
        } catch (CoreException e) {
            throw new CheckstyleException("Unable to read build.properties file", e);
        }
    }

    /**
     * Checks whether a file is empty
     *
     * @param file - the file to check
     * @return true if the file is empty, otherwise false
     */
    protected boolean isEmpty(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            if (bufferedReader.readLine() == null) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Adds an entry in the report using the {@link MessageDispatcher}.
     * Can be used in the {@link #finishProcessing()} where the {@link #log(int, String, Object...)}
     * methods can't be used as the entries logged by them won't be included in the report.
     *
     * @param filePath the absolute path to the file. Although a relative path can be used,
     *            it is not recommended as it will make filtering harder
     * @param line the line that will be added in the report
     * @param fileName the name the file
     * @param message the message that will be logged
     */
    protected void logMessage(String filePath, int line, String fileName, String message) {
        MessageDispatcher dispatcher = getMessageDispatcher();
        dispatcher.fireFileStarted(filePath);
        log(line, message, fileName);
        fireErrors(filePath);
        dispatcher.fireFileFinished(filePath);
    }
}
