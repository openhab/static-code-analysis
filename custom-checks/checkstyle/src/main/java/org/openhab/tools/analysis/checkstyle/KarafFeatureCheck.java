/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if a bundle is added in a Karaf feature.xml file
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class KarafFeatureCheck extends AbstractStaticCheck {

    private static final String MSG_MISSING_BUNDLE_IN_FEATURE_XML = "Bundle with ID '{0}' must be added in one of {1}";
    private static final String BINDING_ID_PATTERN = "mvn:{0}/{1}/{2}";
    private static final String BINDING_FEATURE_EXPRESSION = "//features/feature/bundle[text()=\"{0}\"]";
    private static final String POM_ARTIFACT_ID_XPATH_EXPRESSION = "//project/artifactId/text()";
    private static final String POM_GROUP_ID_XPATH_EXPRESSION = "//project/groupId/text()";
    private static final String POM_PARENT_GROUP_ID_XPATH_EXPRESSION = "//project/parent/groupId/text()";

    private final Logger logger = LoggerFactory.getLogger(KarafFeatureCheck.class);

    /**
     * Configuration property - relative path to the feature.xml file
     */
    private String featureXmlPath;

    public KarafFeatureCheck() {
        setFileExtensions(XML_EXTENSION);
    }

    public void setFeatureXmlPath(String featureXmlPath) {
        this.featureXmlPath = featureXmlPath;
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (POM_XML_FILE_NAME.equals(file.getName())) {
            String bundleId = getBundleId(fileText);
            if (bundleId == null) {
                logger.warn("{} will be skipped. Could not find Maven group ID (parent group ID) or artifact ID in {}",
                        getClass().getSimpleName(), file.getAbsolutePath());
                return;
            }

            String expression = MessageFormat.format(BINDING_FEATURE_EXPRESSION, bundleId);

            String[] individualPaths = featureXmlPath.split(":");

            boolean isFound = false;

            for (String singlePath : individualPaths) {
                Path featurePath = resolveRecursively(file.toPath(), Paths.get(singlePath));

                if (featurePath == null) {
                    logger.debug("Could not find file feature file {}", singlePath);
                    continue;
                }

                try {
                    FileText featureFileText = new FileText(featurePath.toFile(), StandardCharsets.UTF_8.name());
                    Document featureXML = parseDomDocumentFromFile(featureFileText);

                    Node result = getFirstNode(featureXML, expression);

                    if (result != null) {
                        isFound = true;
                        break;
                    }
                } catch (IOException e) {
                    logger.error("Could not read {}", featureXmlPath);
                }
            }

            if (!isFound) {
                log(0, MessageFormat.format(MSG_MISSING_BUNDLE_IN_FEATURE_XML, bundleId, featureXmlPath));
            }
        }
    }

    private String getBundleId(FileText fileText) throws CheckstyleException {
        Document featureXML = parseDomDocumentFromFile(fileText);

        Node artifactId = getFirstNode(featureXML, POM_ARTIFACT_ID_XPATH_EXPRESSION);
        Node groupId = getFirstNode(featureXML, POM_GROUP_ID_XPATH_EXPRESSION);
        Node parentGroupId = getFirstNode(featureXML, POM_PARENT_GROUP_ID_XPATH_EXPRESSION);

        // Maven allows us to skip adding the group ID if the parent element has group ID
        if (artifactId != null && groupId != null) {
            return MessageFormat.format(BINDING_ID_PATTERN, groupId.getNodeValue(), artifactId.getNodeValue(),
                    "${project.version}");
        } else if (artifactId != null && parentGroupId != null) {
            return MessageFormat.format(BINDING_ID_PATTERN, parentGroupId.getNodeValue(), artifactId.getNodeValue(),
                    "${project.version}");
        } else {
            return null;
        }
    }

    private Node getFirstNode(Document document, String xpathExpression) {
        try {
            XPathExpression artifactIdExpression = compileXPathExpression(xpathExpression);
            return ((NodeList) artifactIdExpression.evaluate(document, XPathConstants.NODESET)).item(0);
        } catch (CheckstyleException | XPathExpressionException e) {
            logger.error("Could not evaluate XPath expression {}", xpathExpression, e);
            return null;
        }
    }

    private Path resolveRecursively(Path absolute, Path relativePath) {
        while (absolute.getNameCount() > 0) {
            absolute = absolute.getParent();
            Path resolved = absolute.resolve(relativePath);
            if (Files.exists(resolved)) {
                return resolved;
            }
        }
        return null;
    }
}
