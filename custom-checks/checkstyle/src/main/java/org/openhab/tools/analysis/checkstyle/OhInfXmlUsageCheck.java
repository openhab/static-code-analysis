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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.openhab.tools.analysis.checkstyle.api.AbstractOhInfXmlCheck;
import org.openhab.tools.analysis.checkstyle.api.CheckConstants;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Check for missing bridge-type or supported bridge-type-refs in the same file.<br>
 * Check for missing config file if there is a uri reference to configuration.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class OhInfXmlUsageCheck extends AbstractOhInfXmlCheck {
    private static final String CONFIG_DESCRIPTION_EXPRESSION = "//config-description[@uri]/@uri";
    private static final String CONFIG_DESCRIPTION_REF_EXPRESSION = "//config-description-ref[@uri]/@uri";
    private static final String BRIDGE_TYPE_EXPRESSION = "//bridge-type[@id]/@id";
    private static final String SUPPORTED_BRIDGE_TYPE_REF_EXPRESSION = "//supported-bridge-type-refs/bridge-type-ref[@id]/@id";
    private static final String CONFIGURABLE_SERVICE_REF_EXPRESSION = "//component/property[@name='service.config.description.uri']/@value";

    private static final String MESSAGE_MISSING_URI_CONFIGURATION = "Missing configuration for the configuration reference with uri - {0}";
    private static final String MESSAGE_MISSING_SUPPORTED_BRIDGE = "Missing the supported bridge with id {0}";
    private static final String MESSAGE_UNUSED_URI_CONFIGURATION = "Unused configuration reference with uri - {0}";
    private static final String MESSAGE_UNUSED_BRIDGE = "Unused bridge reference with id - {0}";

    private final Map<String, File> allConfigDescriptionRefs = new HashMap<>();
    private final Map<String, File> allConfigDescriptions = new HashMap<>();

    private final Map<String, File> allSupportedBridges = new HashMap<>();
    private final Map<String, File> allBridgeTypes = new HashMap<>();

    @Override
    public void finishProcessing() {
        // Check for missing supported bridge-type-refs.
        final Map<String, File> missingSupportedBridges = removeAll(allSupportedBridges, allBridgeTypes);
        logMissingEntries(missingSupportedBridges, MESSAGE_MISSING_SUPPORTED_BRIDGE);

        // Check for missing referenced config descriptions
        final Map<String, File> missingConfigDescriptions = removeAll(allConfigDescriptionRefs, allConfigDescriptions);
        logMissingEntries(missingConfigDescriptions, MESSAGE_MISSING_URI_CONFIGURATION);

        // Check for unused bridge-type-refs.
        final Map<String, File> unusedBridges = removeAll(allBridgeTypes, allSupportedBridges);
        logMissingEntries(unusedBridges, MESSAGE_UNUSED_BRIDGE);

        // Check for unused referenced config descriptions
        Map<String, File> unusedConfigDescriptions = removeAll(allConfigDescriptions, allConfigDescriptionRefs);
        unusedConfigDescriptions.keySet().removeIf(key -> key.startsWith("profile:"));
        if (!unusedConfigDescriptions.isEmpty()) {
            // Check if the unused config descriptions are referenced by configurable service components
            Map<String, File> configurableServiceRefs = getConfigurableServiceRefs(
                    unusedConfigDescriptions.values().iterator().next().toPath());
            unusedConfigDescriptions = removeAll(unusedConfigDescriptions, configurableServiceRefs);
        }

        logMissingEntries(unusedConfigDescriptions, MESSAGE_UNUSED_URI_CONFIGURATION);
    }

    @Override
    protected void checkConfigFile(final FileText xmlFileText) throws CheckstyleException {
        // The allowed values are described in the config description XSD
        allConfigDescriptions.putAll(evaluateExpressionOnFile(xmlFileText, CONFIG_DESCRIPTION_EXPRESSION));
    }

    @Override
    protected void checkAddonFile(final FileText xmlFileText) throws CheckstyleException {
        // The allowed values are described in the addon XSD
        allConfigDescriptionRefs.putAll(evaluateExpressionOnFile(xmlFileText, CONFIG_DESCRIPTION_REF_EXPRESSION));
        allConfigDescriptions.putAll(evaluateExpressionOnFile(xmlFileText, CONFIG_DESCRIPTION_EXPRESSION));
    }

    @Override
    protected void checkThingTypeFile(final FileText xmlFileText) throws CheckstyleException {
        // Process the files for all nodes below,
        // the allowed values are described in the thing description XSD
        allSupportedBridges.putAll(evaluateExpressionOnFile(xmlFileText, SUPPORTED_BRIDGE_TYPE_REF_EXPRESSION));
        allBridgeTypes.putAll(evaluateExpressionOnFile(xmlFileText, BRIDGE_TYPE_EXPRESSION));
        allConfigDescriptionRefs.putAll(evaluateExpressionOnFile(xmlFileText, CONFIG_DESCRIPTION_REF_EXPRESSION));
        allConfigDescriptions.putAll(evaluateExpressionOnFile(xmlFileText, CONFIG_DESCRIPTION_EXPRESSION));
    }

    private Map<String, File> evaluateExpressionOnFile(final FileText xmlFileText, final String xPathExpression)
            throws CheckstyleException {
        final Map<String, File> collection = new HashMap<>();
        final NodeList nodes = getNodes(xmlFileText, xPathExpression);

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                collection.put(nodes.item(i).getNodeValue(), xmlFileText.getFile());
            }
        }
        return collection;
    }

    private Map<String, File> removeAll(final Map<String, File> firstMap, final Map<String, File> secondMap) {
        final Map<String, File> result = new HashMap<>(firstMap);
        result.keySet().removeAll(secondMap.keySet());
        return result;
    }

    private void logMissingEntries(final Map<String, File> collection, final String message) {
        for (final Entry<String, File> entry : collection.entrySet()) {
            final File xmlFile = entry.getValue();
            logMessage(xmlFile.getPath(), 0, xmlFile.getName(), MessageFormat.format(message, entry.getKey()));
        }
    }

    private Map<String, File> getConfigurableServiceRefs(Path basePath) {
        Map<String, File> uriFileMap = new HashMap<>();
        for (Path path = basePath; path != null; path = path.getParent()) {
            if (CheckConstants.OH_INF_DIRECTORY.equals(path.getFileName().toString())) {
                Path osgiInfPath = path.resolve("../../../../" + CheckConstants.OSGI_INF_PATH);
                if (!Files.exists(osgiInfPath)) {
                    return uriFileMap;
                }
                try (Stream<Path> pathStream = Files.list(osgiInfPath)) {
                    pathStream.forEach(xmlPath -> uriFileMap.putAll(getConfigurableServiceRefsFromXml(xmlPath)));
                } catch (IOException e) {
                }
                break;
            }
        }
        return uriFileMap;
    }

    private Map<String, File> getConfigurableServiceRefsFromXml(Path xmlPath) {
        try {
            FileText xmlFileText = new FileText(xmlPath.toFile(), StandardCharsets.UTF_8.name());
            return evaluateExpressionOnFile(xmlFileText, CONFIGURABLE_SERVICE_REF_EXPRESSION);
        } catch (CheckstyleException | IOException e) {
            return Map.of();
        }
    }
}
