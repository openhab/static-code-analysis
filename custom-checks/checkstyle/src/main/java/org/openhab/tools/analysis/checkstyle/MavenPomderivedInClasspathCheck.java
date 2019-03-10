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
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.CLASSPATH_EXTENSION;

import java.io.File;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the classpath file has a maven.pomderived attribute. This attribute should be used only if you have
 * problems downloading your maven dependencies.
 *
 * @author Aleksandar Kovachev
 *
 */
public class MavenPomderivedInClasspathCheck extends AbstractStaticCheck {
    private static final String POMDERIVED_EXPRESSION = "/classpath/classpathentry/attributes/attribute[@name='maven.pomderived' and @value='true']/@name";

    private final Log logger = LogFactory.getLog(MavenPomderivedInClasspathCheck.class);

    public MavenPomderivedInClasspathCheck() {
        setFileExtensions(CLASSPATH_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (isEmpty(fileText)) {
            log(0, "The .classpath file should not be empty.");
        } else {
            Document document = parseDomDocumentFromFile(fileText);

            XPathExpression xpathExpression = compileXPathExpression(POMDERIVED_EXPRESSION);

            NodeList nodes = null;
            try {
                nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                logger.error("An error has occured while parsing the .classpath file. Check if the file is valid.", e);
            }

            if (nodes != null) {
                int lineNumber = 0;
                for (int i = 0; i < nodes.getLength(); i++) {
                    String nodeValue = nodes.item(i).getNodeValue();
                    lineNumber = findLineNumberSafe(fileText, nodeValue, lineNumber, "XML node line number nof found.");

                    log(lineNumber, "The classpath file contains maven.pomderived attribute. "
                            + "This attribute should be used only if you have problems downloading your maven dependencies.");
                }
            }
        }
    }
}
