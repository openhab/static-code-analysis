/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

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
 * Checks if a pom file overrides a configuration inherited by the parent pom.
 *
 * @author Aleksandar Kovachev
 *
 */
public class OverridingParentPomConfigurationCheck extends AbstractStaticCheck {
    private static final String POM_CONFIGURATION_EXPRESSION = "/project//*[@combine.self='override']/@combine.self";

    private final Log logger = LogFactory.getLog(OverridingParentPomConfigurationCheck.class);

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
                    logger.error("An error has occured while parsing the pom.xml. Check if the file is valid.", e);
                }

                if (nodes != null) {
                    int lineNumber = 0;

                    for (int i = 0; i < nodes.getLength(); i++) {
                        lineNumber = findLineNumber(fileText, nodes.item(i).getNodeValue(), lineNumber);
                        if (lineNumber != -1) {
                            log(lineNumber, "Avoid overriding a configuration inherited by the parent pom.");
                        }
                    }
                }
            }
        }
    }
}
