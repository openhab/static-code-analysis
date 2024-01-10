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
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if an add-on has a valid feature.xml file.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class KarafAddonFeatureCheck extends AbstractStaticCheck {

    public static final String FEATURE_XML = "feature.xml";
    public static final Path FEATURE_XML_PATH = Path.of("src", "main", "feature", FEATURE_XML);
    public static final String MSG_MISSING_FEATURE_XML = "Missing feature file {0}";
    public static final String MSG_FEATURES_NAME_INVALID = "Invalid features name, expected name starting with: {0}";
    public static final String MSG_FEATURE_NAME_INVALID = "Invalid feature name, expected name: {0}";
    public static final String BUNDLE_VALUE = "mvn:org.openhab.addons.bundles/{0}/$'{'project.version'}'";
    public static final String MSG_BUNDLE_INVALID = "Invalid or missing bundle entry. Expected <bundle start-level=\"80\">{0}</bundle>";

    private static final String FEATURES_NAME_EXPRESSION = "//features[@name]/@name";
    private static final String FEATURES_SEARCH = "<features";
    private static final String FEATURE_NAME_EXPRESSION = "//features/feature[@name]/@name";
    private static final String FEATURE_SEARCH = "<feature ";
    private static final String BUNDLE_EXPRESSION = "//features/feature/bundle/text()";
    private static final String BUNDLE_SEARCH = "mvn:org.openhab.addons.bundles";

    private static final String POM_ARTIFACT_ID_XPATH_EXPRESSION = "//project/artifactId/text()";
    private static final String POM_PARENT_ARTIFACT_ID = "org.openhab.addons.reactor.bundles";
    private static final String POM_PARENT_ARTIFACT_ID_XPATH_EXPRESSION = "//project/parent/artifactId/text()";

    private final Logger logger = LoggerFactory.getLogger(KarafAddonFeatureCheck.class);

    private final Map<String, String> featureNamePatternsMap = new LinkedHashMap<>();
    private final List<Pattern> excludeAddonsList = new ArrayList<>();

    public KarafAddonFeatureCheck() {
        setFileExtensions(XML_EXTENSION);
    }

    /**
     * Comma separated list of key value pairs (separated by colon) transform the calculated expected feature name in
     * the actual feature name.
     * For example openhab-transform-map:openhab-transformation-map.
     * the openhab-transform-map is as expected as derived from the artifactId, but all bundles use transformation.
     * So with the map expected feature name can be constructed.
     *
     * @param featureNameMappings list of key/value pairs of patterns
     */
    public void setFeatureNameMappings(String featureNameMappings) {
        if (featureNameMappings.trim().isBlank()) {
            return;
        }
        for (String pattern : featureNameMappings.split(",")) {
            final String[] keypair = pattern.split(":");

            if (keypair.length == 2) {
                featureNamePatternsMap.put(keypair[0], keypair[1]);
            } else {
                logger.warn("{} check pattern for option featureNamePatterns is invalid. Value set: {}",
                        getClass().getName(), featureNameMappings);
            }
        }
    }

    /**
     * Comma separated list of add-on name patterns that will be excluded from checking.
     *
     * @param excludeAddonPatterns command separated list
     */
    public void setExcludeAddonPatterns(String excludeAddonPatterns) {
        if (excludeAddonPatterns.trim().isBlank()) {
            return;
        }
        for (String pattern : excludeAddonPatterns.split(",")) {
            excludeAddonsList.add(Pattern.compile(pattern));
        }
    }

    @Override
    protected void processFiltered(@Nullable File file, @Nullable FileText fileText) throws CheckstyleException {
        if (file == null || fileText == null) {
            return;
        }
        switch (file.getName()) {
            case POM_XML_FILE_NAME:
                checkMissingFeatureFile(file, fileText);
                break;
            case FEATURE_XML:
                checkFeatureFile(file, fileText);
                break;
        }
    }

    private void checkMissingFeatureFile(File file, FileText fileText) throws CheckstyleException {
        final String artifactId = getArtifactId(fileText);

        if (artifactId == null) {
            logger.debug("{} will be skipped. Could not find Maven group ID (parent group ID) or artifact ID in {}",
                    getClass().getSimpleName(), file.getAbsolutePath());
            return;
        }
        String parent = file.getParent();
        final File featureFile = new File(parent, FEATURE_XML_PATH.toString());

        if (!featureFile.exists()) {
            if (isExcludedAddon(parent)) {
                logger.debug("Ignore check on none existing feature name {}", featureFile);
            } else {
                logMessage(featureFile.toString(), 0, FEATURE_XML,
                        MessageFormat.format(MSG_MISSING_FEATURE_XML, featureFile));
            }
        }
    }

    private void checkFeatureFile(File featureFile, FileText fileText) throws CheckstyleException {
        try {
            final String featureFileString = featureFile.getAbsoluteFile().toString();
            String addonPath = featureFileString.replace(FEATURE_XML_PATH.toString(), "");

            if (isExcludedAddon(new File(addonPath).getName())) {
                logger.debug("Ignore check on excluded addon with feature name {}", featureFile);
                return;
            }
            final FileText pomFile = new FileText(new File(addonPath, POM_XML_FILE_NAME),
                    StandardCharsets.UTF_8.name());
            final String artifactId = getArtifactId(pomFile);

            if (artifactId == null) {
                logger.debug("Ignore check on feature.xml with no bundle specific pom.xml: {}", featureFileString);
            } else {
                final Document featureXML = parseDomDocumentFromFile(fileText);

                checkFeatures(featureFile, artifactId, fileText, featureXML);
                checkFeature(featureFile, artifactId, fileText, featureXML);
                checkBundle(featureFile, artifactId, fileText, featureXML);
            }
        } catch (IOException e) {
            logger.error("Could not read {}", FEATURE_XML_PATH);
        }
    }

    private boolean isExcludedAddon(@Nullable String parent) {
        return excludeAddonsList.stream().anyMatch(p -> p.matcher(parent).find());
    }

    private void checkFeatures(File featureFile, String artifactId, FileText fileText, Document featureXML) {
        checkName(featureFile, artifactId, fileText, featureXML, FEATURES_NAME_EXPRESSION,
                name -> !artifactId.equals(name.substring(0, name.indexOf('-'))), MSG_FEATURES_NAME_INVALID,
                FEATURES_SEARCH);
    }

    private void checkFeature(File featureFile, String artifactId, FileText fileText, Document featureXML) {
        final String featureName = adaptedFeatureName(artifactId);

        checkName(featureFile, featureName, fileText, featureXML, FEATURE_NAME_EXPRESSION,
                name -> !featureName.equals(name), MSG_FEATURE_NAME_INVALID, FEATURE_SEARCH);
    }

    private String adaptedFeatureName(String artifactId) {
        // skip 'org.' part of artifactId by taking substring(4)
        String featureName = artifactId.substring(4).replaceAll("\\.", "-");

        for (Entry<String, String> entry : featureNamePatternsMap.entrySet()) {
            featureName = featureName.replace(entry.getKey(), entry.getValue());
        }
        return featureName;
    }

    private void checkName(File featureFile, String expectedName, FileText fileText, Document featureXML,
            String expression, Function<String, Boolean> checkFunction, String message, String lineSearchString) {
        final Node featuresName = getFirstNode(featureXML, expression);
        final String errorMessage = MessageFormat.format(message, expectedName);

        if (featuresName == null) {
            logMessage(featureFile.getAbsolutePath(), 0, featureFile.getName(), errorMessage);
        } else {
            String name = featuresName.getNodeValue();

            if (name == null || checkFunction.apply(name)) {
                logMessage(featureFile.getAbsolutePath(),
                        findLineNumberSafe(fileText, lineSearchString, 0, errorMessage), featureFile.getName(),
                        errorMessage);
            }
        }
    }

    private void checkBundle(File featureFile, String artifactId, FileText fileText, Document document) {
        final String errorMessage = MessageFormat.format(MSG_BUNDLE_INVALID, BUNDLE_VALUE.replace("{0}", artifactId));

        try {
            final XPathExpression artifactIdExpression = compileXPathExpression(BUNDLE_EXPRESSION);
            final NodeList bundles = ((NodeList) artifactIdExpression.evaluate(document, XPathConstants.NODESET));
            final String expectedBundleValue = MessageFormat.format(BUNDLE_VALUE, artifactId);
            boolean found = false;

            for (int i = 0; i < bundles.getLength(); i++) {
                final Node item = bundles.item(i);
                if (expectedBundleValue.equals(item.getNodeValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                logMessage(featureFile.getAbsolutePath(), findLineNumberSafe(fileText, BUNDLE_SEARCH, 0, errorMessage),
                        featureFile.getName(), errorMessage);
            }
        } catch (CheckstyleException | XPathExpressionException e) {
            logMessage(featureFile.getAbsolutePath(), 0, featureFile.getName(), errorMessage);
        }
    }

    private @Nullable String getArtifactId(FileText fileText) throws CheckstyleException {
        final Document documentXML = parseDomDocumentFromFile(fileText);
        final Node artifactId = getFirstNode(documentXML, POM_ARTIFACT_ID_XPATH_EXPRESSION);
        final Node parentArtifactId = getFirstNode(documentXML, POM_PARENT_ARTIFACT_ID_XPATH_EXPRESSION);

        return artifactId == null || parentArtifactId == null
                || !POM_PARENT_ARTIFACT_ID.equals(parentArtifactId.getNodeValue()) ? null : artifactId.getNodeValue();
    }

    private @Nullable Node getFirstNode(Document document, String xpathExpression) {
        try {
            final XPathExpression artifactIdExpression = compileXPathExpression(xpathExpression);

            return ((NodeList) artifactIdExpression.evaluate(document, XPathConstants.NODESET)).item(0);
        } catch (CheckstyleException | XPathExpressionException e) {
            logger.error("Could not evaluate XPath expression {}", xpathExpression, e);
            return null;
        }
    }
}
