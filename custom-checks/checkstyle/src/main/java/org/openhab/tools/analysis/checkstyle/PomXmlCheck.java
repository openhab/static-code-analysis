/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.text.MessageFormat;
import java.util.Optional;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.osgi.core.BundleInfo;
import org.apache.ivy.osgi.util.Version;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the version and the artifactId in the pom.xml file correspond to
 * the ones in the MANIFEST.MF
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Replaced headers, applied minor improvements, added
 *         check for parent pom ID
 * @author Velin Yordanov - The check can now verify that the pom version is the
 *         same as the parent pom version if checkPomVersion property is set to
 *         true and also removed the version regex property.
 */
public class PomXmlCheck extends AbstractStaticCheck {
    private static final String MISSING_VERSION_MSG = "Missing /project/version in the pom.xml file.";
    private static final String MISSING_ARTIFACT_ID_MSG = "Missing /project/artifactId in the pom.xml file.";
    private static final String WRONG_VERSION_MSG = "Wrong /project/parent/version in the pom.xml file. "
            + "The version should match the one in the MANIFEST.MF file.";
    private static final String WRONG_ARTIFACT_ID_MSG = "Wrong /project/artifactId in the pom.xml file. "
            + "The artifactId should match the bundle symbolic name in the MANIFEST.MF file.";
    private static final String MISSING_PARENT_ARTIFACT_ID_MSG = "Missing /project/parent/artifactId of the parent pom";
    private static final String WRONG_PARENT_ARTIFACT_ID_MSG = "Wrong /project/parent/artifactId. Expected {0} but was {1}";

    private static final String POM_ARTIFACT_ID_XPATH_EXPRESSION = "/project/artifactId/text()";
    private static final String POM_PARENT_ARTIFACT_ID_XPATH_EXPRESSION = "/project/parent/artifactId/text()";
    private static final String POM_PARENT_VERSION_XPATH_EXPRESSION = "/project/parent/version/text()";
    private static final String POM_VERSION_XPATH_EXPRESSION = "/project/version/text()";
    private static final String DIFFERENT_POM_VERSION = "The pom version is different from the parent pom version";

    private final Log logger = LogFactory.getLog(this.getClass());

    private String pomDirectoryPath;

    private String pomVersion;
    private int pomVersionLine;
    private String manifestVersion;

    private String pomArtifactId;
    private int pomArtifactIdLine;
    private String manifestBundleSymbolicName;
    private Document parentPomXmlDocument;
    private String parentPomPath;
    private String pomPath;
    private boolean checkPomVersion;

    public void setCheckPomVersion(boolean value) {
        checkPomVersion = value;
    }

    public PomXmlCheck() {
        setFileExtensions(XML_EXTENSION, MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        String fileName = file.getName();
        if (fileName.equals(POM_XML_FILE_NAME)) {
            processPomXmlFile(fileText);
        } else if (fileName.equals(MANIFEST_FILE_NAME)) {
            processManifestFile(fileText);
        }
    }

    private void processManifestFile(FileText fileText) throws CheckstyleException {
        BundleInfo bundleInfo = parseManifestFromFile(fileText);

        Version version = bundleInfo.getVersion();
        // We need this in order to filter the "qualifier" for the Snapshot versions
        manifestVersion = version.withoutQualifier().toString();

        if (manifestVersion == null) {
            log(0, "Manifest version is missing");
        }

        manifestBundleSymbolicName = bundleInfo.getSymbolicName();
    }

    private void processPomXmlFile(FileText fileText) throws CheckstyleException {
        File file = fileText.getFile();
        pomPath = file.getPath();
        File pomDirectory = file.getParentFile();
        Document pomXmlDocument = parseDomDocumentFromFile(fileText);
        String pomXmlPath = fileText.getFile().getPath();

        // the pom directory path will be used in the finalization
        pomDirectoryPath = pomDirectory.getPath();
        File parentPom = new File(pomDirectory.getParentFile(), POM_XML_FILE_NAME);

        // get the version from the pom.xml
        getPomVersion(pomXmlDocument, pomXmlPath).ifPresent(value -> {
            pomVersion = value;
            // the version line will be preserved for finalization of the processing
            String versionTagName = "version";
            String versionLine = String.format("<%s>%s</%s>", versionTagName, value, versionTagName);
            pomVersionLine = findLineNumberSafe(fileText, versionLine, 0, "Pom version line number not found");
        });

        // get the artifactId from the pom.xml
        getNodeValue(pomXmlDocument, POM_ARTIFACT_ID_XPATH_EXPRESSION, pomXmlPath).ifPresent(value -> {
            pomArtifactId = value;
            // the artifact ID line will be used in the finalization as well
            String artifactIdTagName = "artifactId";
            String artifactIdLine = String.format("<%s>%s</%s>", artifactIdTagName, value, artifactIdTagName);
            pomArtifactIdLine = findLineNumberSafe(fileText, artifactIdLine, 0,
                    "Pom artifact ID line number not found");
        });

        // The pom.xml must reference the correct parent pom (which is usually in the
        // parent folder)
        if (parentPom.exists()) {
            Optional<Document> maybeDocument = getParsedPom(parentPom);
            if (!maybeDocument.isPresent()) {
                return;
            }

            parentPomXmlDocument = maybeDocument.get();
            parentPomPath = parentPom.getPath();
            Optional<String> maybeParentArtifactIdValue = getNodeValue(pomXmlDocument,
                    POM_PARENT_ARTIFACT_ID_XPATH_EXPRESSION, pomXmlPath);
            Optional<String> maybeParentPomArtifactIdValue = getNodeValue(parentPomXmlDocument,
                    POM_ARTIFACT_ID_XPATH_EXPRESSION, parentPomPath);

            if (maybeParentArtifactIdValue.isPresent() && maybeParentPomArtifactIdValue.isPresent()) {
                String parentPomArtifactIdValue = maybeParentArtifactIdValue.get();
                String parentArtifactIdValue = maybeParentPomArtifactIdValue.get();
                if (!parentPomArtifactIdValue.equals(parentArtifactIdValue)) {
                    int parentArtifactTagLine = findLineNumberSafe(fileText, "parent", 0,
                            "Parent line number not found.");
                    int parentArtifactIdLine = findLineNumberSafe(fileText, "artifactId", parentArtifactTagLine,
                            "Parent artifact ID line number not found.");

                    String formattedMessage = MessageFormat.format(WRONG_PARENT_ARTIFACT_ID_MSG, parentArtifactIdValue,
                            parentPomArtifactIdValue);
                    log(parentArtifactIdLine, formattedMessage, file.getPath());
                }
            } else {
                log(0, MISSING_PARENT_ARTIFACT_ID_MSG, file.getPath());
            }
        }
    }

    @Override
    public void finishProcessing() {
        compareProperties(pomVersion, manifestVersion, pomVersionLine, WRONG_VERSION_MSG, MISSING_VERSION_MSG);
        compareProperties(pomArtifactId, manifestBundleSymbolicName, pomArtifactIdLine, WRONG_ARTIFACT_ID_MSG,
                MISSING_ARTIFACT_ID_MSG);
        if (checkPomVersion) {
            try {
                checkVersions();
            } catch (CheckstyleException e) {
                logger.error("An error occurred while processing poms", e);
            }
        }
    }

    private Optional<String> getPomVersion(Document pomXmlDocument, String filePath) throws CheckstyleException {
        Optional<String> maybeVersionNodeValue = getNodeValue(pomXmlDocument, POM_VERSION_XPATH_EXPRESSION, filePath);
        if (!maybeVersionNodeValue.isPresent()) {
            return getNodeValue(pomXmlDocument, POM_PARENT_VERSION_XPATH_EXPRESSION, filePath);
        }

        return maybeVersionNodeValue;
    }

    private void checkVersions() throws CheckstyleException {
        if (parentPomXmlDocument != null) {
            getPomVersion(parentPomXmlDocument, parentPomPath).ifPresent(value -> {
                if (!value.equals(pomVersion)) {
                    logMessage(pomPath, 0, POM_XML_FILE_NAME, DIFFERENT_POM_VERSION);
                }
            });
        }
    }

    private void compareProperties(String pomProperty, String manifestProperty, int wrongPropertyLine,
            String wrongPropertyMessage, String missingPropertyMessage) {
        if (pomProperty != null) {
            if (manifestProperty != null && !pomProperty.contains(manifestProperty)) {
                logMessage(pomDirectoryPath + File.separator + POM_XML_FILE_NAME, wrongPropertyLine, POM_XML_FILE_NAME,
                        wrongPropertyMessage);
            }
            // no need to log if something is wrong with the manifestProperty,
            // there are other special checks for that
        } else {
            logMessage(pomDirectoryPath + File.separator + POM_XML_FILE_NAME, 0, POM_XML_FILE_NAME,
                    missingPropertyMessage);
        }
    }

    private Optional<String> getNodeValue(Document xmlDocument, String versionExpression, String filePath)
            throws CheckstyleException {
        XPathExpression xPathExpression = compileXPathExpression(versionExpression);
        try {
            Object result = xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            return nodes.getLength() > 0 ? Optional.of(nodes.item(0).getTextContent()) : Optional.empty();
        } catch (XPathExpressionException e) {
            logger.error("An exception was thrown, while trying to parse the file: " + filePath, e);
            return Optional.empty();
        }
    }

    private Optional<Document> getParsedPom(File pom) throws CheckstyleException {
        FileText parentPomFileText = null;
        try {
            parentPomFileText = new FileText(pom, "UTF-8");
        } catch (IOException e) {
            logger.error("Error in reading the file", e);
        }

        return Optional.ofNullable(parseDomDocumentFromFile(parentPomFileText));
    }
}
