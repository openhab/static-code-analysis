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
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

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
 * Checks if a pom file overrides a configuration inherited by the parent pom.
 *
 * @author Aleksandar Kovachev - Initial contribution
 */
public class OverridingParentPomConfigurationCheck extends AbstractStaticCheck {
    private static final String POM_CONFIGURATION_EXPRESSION = "/project//*[@combine.self='override']/@combine.self";

    private final Logger logger = LoggerFactory.getLogger(OverridingParentPomConfigurationCheck.class);

    public OverridingParentPomConfigurationCheck() {
        setFileExtensions(XML_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (file.getName().equals(POM_XML_FILE_NAME)) {
            if (isEmpty(fileText)) {
                log(0, "The pom.xml file should not be empty.");
            } else {
                Document document = parseDomDocumentFromFile(fileText);

                XPathExpression xpathExpression = compileXPathExpression(POM_CONFIGURATION_EXPRESSION);

                NodeList nodes = null;
                try {
                    nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    logger.error("An error has occurred while parsing the pom.xml. Check if the file is valid.", e);
                }

                if (nodes != null) {
                    int lineNumber = 0;

                    for (int i = 0; i < nodes.getLength(); i++) {
                        String nodeValue = nodes.item(i).getNodeValue();
                        lineNumber = findLineNumberSafe(fileText, nodeValue, lineNumber,
                                "XML node line number not found.");
                        log(lineNumber, "Avoid overriding a configuration inherited by the parent pom.");
                    }
                }
            }
        }
    }
}
