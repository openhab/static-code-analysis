/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.osgi.core.BundleInfo;
import org.apache.ivy.osgi.util.Version;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the version and the artifactId in the pom.xml file correspond to
 * the ones in the MANIFEST.MF
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Replaced headers, applied minor improvements, added check for parent pom ID
 * @author Velin Yordanov - Enhanced the check. It can now check for a specific version.
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

    private static final String DEFAULT_VERSION_REGULAR_EXPRESSION = "^\\d+\\.\\d+\\.\\d+";
    private static final String POM_ARTIFACT_ID_XPATH_EXPRESSION = "/project/artifactId/text()";
    private static final String POM_PARENT_ARTIFACT_ID_XPATH_EXPRESSION = "/project/parent/artifactId/text()";
    private static String POM_PARENT_VERSION_XPATH_EXPRESSION = "/project/parent/version/text()";
    private static String POM_VERSION_XPATH_EXPRESSION = "/project/version/text()";
    private static String INCORRECT_VERSION = "The manifest version did not match the requirements %s";

    private final Log logger = LogFactory.getLog(this.getClass());

    private String pomDirectoryPath;

    private String pomVersion;
    private int pomVersionLine;
    private String manifestVersion;

    private String pomArtifactId;
    private int pomArtifactIdLine;
    private String manifestBundleSymbolicName;

    private String pomVersionRegularExpression;
    private String manifestVersionRegularExpression;

    private Pattern manifestVersionPattern;
    private Pattern pomVersionPattern;

    public PomXmlCheck() {
        setFileExtensions(XML_EXTENSION, MANIFEST_EXTENSION);
    }

    /**
     * Sets a configuration property for a regular expression,
     * that must match the version in the pom.xml file
     *
     * @param pomVersionRegularExpression regex that matches the pom.xml version
     */
    public void setPomVersionRegularExpression(String pomVersionRegularExpression) {
        this.pomVersionRegularExpression = pomVersionRegularExpression;
    }

    /**
     * Sets a configuration property for a regular expression,
     * that must match the bundle version in the MANIFEST.MF file
     *
     * @param manifestVersionRegularExpression regex that matches the MANIFEST.MF version
     */
    public void setManifestVersionRegularExpression(String manifestVersionRegularExpression) {
        this.manifestVersionRegularExpression = manifestVersionRegularExpression;
    }

    @Override
    public void beginProcessing(String charset) {
        pomVersionPattern = compilePattern(pomVersionRegularExpression);
        manifestVersionPattern = compilePattern(manifestVersionRegularExpression);
        super.beginProcessing(charset);
    }

    private Pattern compilePattern(String regExp) {
        if (regExp != null) {
            try {
                return Pattern.compile(regExp);
            } catch (PatternSyntaxException e) {
                String message = MessageFormat.format("Pattern {0} syntax is invalid.", regExp);
                logger.error(message, e);
            }
        }
        logger.debug("Default pattern will be used for version matching: " + DEFAULT_VERSION_REGULAR_EXPRESSION);
        return Pattern.compile(DEFAULT_VERSION_REGULAR_EXPRESSION);
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
        manifestVersion = getVersion(version.toString(), manifestVersionPattern);

        if (manifestVersion == null) {
            log(0, String.format(INCORRECT_VERSION, manifestVersionRegularExpression));
        }

        manifestBundleSymbolicName = bundleInfo.getSymbolicName();
    }

    private void processPomXmlFile(FileText fileText) throws CheckstyleException {
        File file = fileText.getFile();

        File pomDirectory = file.getParentFile();

        // the pom directory path will be used in the finalization
        pomDirectoryPath = pomDirectory.getPath();
        File parentPom = new File(pomDirectory.getParentFile(), POM_XML_FILE_NAME);

        // The pom.xml must reference the correct parent pom (which is usually in the parent folder)
        if (parentPom.exists()) {
            FileText parentPomFileText = null;
            try {
                parentPomFileText = new FileText(parentPom, "UTF-8");
            } catch (IOException e) {
                logger.error("Error in reading the pom file", e);
                return;
            }

            String parentArtifactIdValue = getNodeValue(fileText, POM_PARENT_ARTIFACT_ID_XPATH_EXPRESSION);
            String parentPomArtifactIdValue = getNodeValue(parentPomFileText, POM_ARTIFACT_ID_XPATH_EXPRESSION);
            if (parentArtifactIdValue != null) {
                if (!parentArtifactIdValue.equals(parentPomArtifactIdValue)) {
                    int parentArtifactTagLine = findLineNumberSafe(fileText, "parent", 0,
                            "Parent line number not found.");
                    int parentArtifactIdLine = findLineNumberSafe(fileText, "artifactId", parentArtifactTagLine,
                            "Parent artifact ID line number not found.");

                    String formattedMessage = MessageFormat.format(WRONG_PARENT_ARTIFACT_ID_MSG,
                            parentPomArtifactIdValue, parentArtifactIdValue);
                    log(parentArtifactIdLine, formattedMessage, file.getPath());
                }
            } else {
                log(0, MISSING_PARENT_ARTIFACT_ID_MSG, file.getPath());
            }
        }

        // get the version from the pom.xml
        String versionNodeValue = getNodeValue(fileText, POM_VERSION_XPATH_EXPRESSION);
        if (versionNodeValue == null) {
            versionNodeValue = getNodeValue(fileText, POM_PARENT_VERSION_XPATH_EXPRESSION);
        }
        pomVersion = getVersion(versionNodeValue, pomVersionPattern);

        if (pomVersion != null) {
            // the version line will be preserved for finalization of the processing
            String versionTagName = "version";
            String versionLine = String.format("<%s>%s</%s>", versionTagName, versionNodeValue, versionTagName);
            pomVersionLine = findLineNumberSafe(fileText, versionLine, 0, "Pom version line number not found");
        }

        // get the artifactId from the pom.xml
        String artifactIdNodeValue = getNodeValue(fileText, POM_ARTIFACT_ID_XPATH_EXPRESSION);
        pomArtifactId = artifactIdNodeValue;

        if (pomArtifactId != null) {
            // the artifact ID line will be used in the finalization as well
            String artifactIdTagName = "artifactId";
            String artifactIdLine = String.format("<%s>%s</%s>", artifactIdTagName, artifactIdNodeValue,
                    artifactIdTagName);
            pomArtifactIdLine = findLineNumberSafe(fileText, artifactIdLine, 0,
                    "Pom artifact ID line number not found");
        }
    }

    @Override
    public void finishProcessing() {
        compareProperties(pomVersion, manifestVersion, pomVersionLine, WRONG_VERSION_MSG, MISSING_VERSION_MSG);
        compareProperties(pomArtifactId, manifestBundleSymbolicName, pomArtifactIdLine, WRONG_ARTIFACT_ID_MSG,
                MISSING_ARTIFACT_ID_MSG);
        super.finishProcessing();
    }

    private void compareProperties(String pomProperty, String manifestProperty, int wrongPropertyLine,
            String wrongPropertyMessage, String missingPropertyMessage) {
        if (pomProperty != null) {
            if (manifestProperty != null && !pomProperty.equals(manifestProperty)) {
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

    private String getNodeValue(FileText fileText, String versionExpression) throws CheckstyleException {
        Document xmlDocument = parseDomDocumentFromFile(fileText);

        XPathExpression xPathExpression = compileXPathExpression(versionExpression);
        try {
            Object result = xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            Node node = nodes.item(0);
            return node != null ? node.getNodeValue() : null;
        } catch (XPathExpressionException e) {
            logger.error("An exception was thrown, while trying to parse the file: " + fileText.getFile().getPath(), e);
            return null;
        }
    }

    private String getVersion(String versionValue, Pattern pattern) {
        if (versionValue != null) {
            Matcher matcher = pattern.matcher(versionValue);
            String version = null;
            if (matcher.find()) {
                version = matcher.group();
            }
            return version;
        } else {
            return null;
        }
    }
}
