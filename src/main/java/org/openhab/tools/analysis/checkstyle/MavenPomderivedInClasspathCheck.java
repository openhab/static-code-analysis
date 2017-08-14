/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.CLASSPATH_EXTENSION;

import java.io.File;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(MavenPomderivedInClasspathCheck.class);

    public MavenPomderivedInClasspathCheck() {
        setFileExtensions(CLASSPATH_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {

        if (isEmpty(file)) {
            log(0, "The .classpath file should not be empty.");
        } else {
            Document document = parseDomDocumentFromFile(file);

            XPathExpression xpathExpression = compileXPathExpression(POMDERIVED_EXPRESSION);

            NodeList nodes = null;
            try {
                nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                logger.error("An error has occured while parsing the .classpath file. Check if the file is valid.", e);
            }

            if (nodes != null) {
                int lineNumber = 0;
                String[] lines = fileText.toLinesArray();

                for (int i = 0; i < nodes.getLength(); i++) {
                    lineNumber = findLineNumber(lines, nodes.item(i).getNodeValue(), lineNumber);
                    if (lineNumber != -1) {
                        log(lineNumber, "The classpath file contains maven.pomderived attribute. "
                                + "This attribute should be used only if you have problems downloading your maven dependencies.");
                    }
                }
            }
        }
    }
}
