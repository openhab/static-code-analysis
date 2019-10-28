/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.tools.analysis.checkstyle.api;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.text.MessageFormat;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Abstract class for checks that will validate .xml files located in the OH-INF directory.
 *
 * @author Aleksandar Kovachev - Initial contribution
 * @author Svilen Valkanov - Some code refactoring and cleanup, added check for the build.properties file
 */
public abstract class AbstractOhInfXmlCheck extends AbstractStaticCheck {
    public static final String THING_DIRECTORY = "thing";
    public static final String BINDING_DIRECTORY = "binding";
    public static final String CONFIGURATION_DIRECTORY = "config";

    private static final String MESSAGE_EMPTY_FILE = "The file {0} should not be empty.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractOhInfXmlCheck() {
        setFileExtensions(XML_EXTENSION);
    }

    @Override
    public void beginProcessing(final String charset) {
        logger.debug("Executing the {}", getClass().getSimpleName());
    }

    @Override
    protected void processFiltered(final File file, final FileText fileText) throws CheckstyleException {
        final String fileName = file.getName();

        if (FilenameUtils.getExtension(fileName).equals(XML_EXTENSION)) {
            processXmlFile(fileText);
        }
    }

    private void processXmlFile(final FileText xmlFileText) throws CheckstyleException {
        final File xmlFile = xmlFileText.getFile();
        if (isEmpty(xmlFileText)) {
            log(0, MessageFormat.format(MESSAGE_EMPTY_FILE, xmlFile.getName()), xmlFile.getPath());
        } else {
            final File fileParentDirectory = xmlFile.getParentFile();
            final boolean isOHParentDirectory = OH_INF_DIRECTORY.equals(fileParentDirectory.getParentFile().getName());

            if (isOHParentDirectory) {
                switch (fileParentDirectory.getName()) {
                    case THING_DIRECTORY: {
                        checkThingTypeFile(xmlFileText);
                        break;
                    }
                    case BINDING_DIRECTORY: {
                        checkBindingFile(xmlFileText);
                        break;
                    }
                    case CONFIGURATION_DIRECTORY: {
                        checkConfigFile(xmlFileText);
                        break;
                    }
                    default:
                        // Other directories like l18n are allowed, but they are not object of this check, so they will
                        // be skipped
                        break;
                }
            }
        }
    }

    /**
     * Validate a .xml file located in the OH-INF/config directory
     *
     * @param xmlFileText Represents the text contents of the xml file
     * @throws CheckstyleException when exception occurred during XML processing
     */
    protected abstract void checkConfigFile(FileText xmlFileText) throws CheckstyleException;

    /**
     * Validate a .xml file located in the OH-INF/binding directory
     *
     * @param xmlFileText Represents the text contents of the xml file
     * @throws CheckstyleException when exception occurred during XML processing
     */
    protected abstract void checkBindingFile(FileText xmlFileText) throws CheckstyleException;

    /**
     * Validate a .xml file located in the OH-INF/thing directory
     *
     * @param xmlFileText Represents the text contents of the xml file
     * @throws CheckstyleException when exception occurred during XML processing
     */
    protected abstract void checkThingTypeFile(FileText xmlFileText) throws CheckstyleException;

    protected NodeList getNodes(final FileText xmlFileText, final String expression) throws CheckstyleException {
        final Document document = parseDomDocumentFromFile(xmlFileText);

        final XPathExpression xpathExpression = compileXPathExpression(expression);

        NodeList nodes = null;
        try {
            nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            final String message = MessageFormat.format(
                    "Problem occurred while evaluating the expression {0} on the {1} file.", expression,
                    xmlFileText.getFile().getName());
            logger.error(message, e);
        }
        return nodes;
    }
}
