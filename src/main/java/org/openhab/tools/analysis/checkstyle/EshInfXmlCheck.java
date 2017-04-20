/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;

/**
 * Validate the thing-types, binding and config xml-s against their xsd schemas.<br>
 * Check for missing bridge-type or supported bridge-type-refs in the same file.<br>
 * Check for missing config file if there is a uri reference to configuration.
 * Check if all files from ESH-INF are included in the build.properties file.
 *
 * @author Aleksandar Kovachev - Initial implementation
 * @author Svlien Valkanov - Some code refactoring and cleanup, added check for the build.properties file
 *
 */
public class EshInfXmlCheck extends AbstractStaticCheck {

    private static final String THING_TYPE_EXTENSION = "xml";
    private static final String PROPERTIES_EXTENSTION = "properties";

    public static final String BUILD_PROPERTIES_FILE_NAME = "build." + PROPERTIES_EXTENSTION;
    public static final String THING_DIRECTORY = "thing";
    public static final String BINDING_DIRECTORY = "binding";
    public static final String ESH_INF_DIRECTORY = "ESH-INF";
    public static final String CONFIGURATION_DIRECTORY = "config";

    private static final String CONFIG_DESCRIPTION_EXPRESSION = "//config-description[@uri]/@uri";
    private static final String CONFIG_DESCRIPTION_REF_EXPRESSION = "//config-description-ref[@uri]/@uri";
    private static final String BRIDGE_TYPE_EXPRESSION = "//bridge-type[@id]/@id";
    private static final String SUPPORTED_BRIDGE_TYPE_REF_EXPRESSION = "//supported-bridge-type-refs/bridge-type-ref[@id]/@id";

    public static final String MESSAGE_MISSING_URI_CONFIGURATION = "Missing configuration for the configuration reference with uri - {0}";
    public static final String MESSAGE_MISSING_SUPPORTED_BRIDGE = "Missing the supported bridge with id {0}";
    public static final String MESSAGE_UNUSED_URI_CONFIGURATION = "Unused configuration reference with uri - {0}";
    public static final String MESSAGE_UNUSED_BRIDGE = "Unused bridge reference with id - {0}";
    public static final String MESSAGE_EMPTY_FILE = "The file {0} should not be empty.";
    private static final String MESSAGE_NOT_INCLUDED_XML_FILE = "The file {0} isn't included in the build.properties file. Good approach is to include all files by adding `ESH-INF/` value to the bin.includes property.";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, File> allConfigDescriptionRefs = new HashMap<>();
    private Map<String, File> allConfigDescriptions = new HashMap<>();

    private Map<String, File> allSupportedBridges = new HashMap<>();
    private Map<String, File> allBridgeTypes = new HashMap<>();

    private Map<Path, File> eshInfFiles = new HashMap<>();
    private IBuild buildPropertiesFile;

    private String thingSchema;
    private String bindingSchema;
    private String configSchema;

    /**
     * Sets the configuration property for the thing schema file.
     */
    public void setThingSchema(String thingSchema) {
        this.thingSchema = thingSchema;
    }

    /**
     * Sets the configuration property for the binding schema file.
     */
    public void setBindingSchema(String bindingSchema) {
        this.bindingSchema = bindingSchema;
    }

    /**
     * Sets the configuration property for the config schema file.
     */
    public void setConfigSchema(String configSchema) {
        this.configSchema = configSchema;
    }

    public EshInfXmlCheck() {
        setFileExtensions(THING_TYPE_EXTENSION, PROPERTIES_EXTENSTION);
    }

    @Override
    public void beginProcessing(String charset) {
        logger.debug("Executing the {}", this.getClass().getSimpleName());
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        String fileName = file.getName();
        logger.debug("Processing the {}", fileName);

        switch (FilenameUtils.getExtension(fileName)) {
            case THING_TYPE_EXTENSION:
                processXmlFile(file);
                break;
            case PROPERTIES_EXTENSTION:
                if (BUILD_PROPERTIES_FILE_NAME.equals(file.getName())) {
                    processBuildProperties(file);
                }
        }
    }

    @Override
    public void finishProcessing() {
        // Check for missing supported bridge-type-refs.
        Map<String, File> missingSupportedBridges = removeAll(allSupportedBridges, allBridgeTypes);
        logMissingEntries(missingSupportedBridges, MESSAGE_MISSING_SUPPORTED_BRIDGE);

        // Check for missing referenced config descriptions
        Map<String, File> missingConfigDescriptions = removeAll(allConfigDescriptionRefs, allConfigDescriptions);
        logMissingEntries(missingConfigDescriptions, MESSAGE_MISSING_URI_CONFIGURATION);

        // Check for unused bridge-type-refs.
        Map<String, File> unusedBridges = removeAll(allBridgeTypes, allSupportedBridges);
        logMissingEntries(unusedBridges, MESSAGE_UNUSED_BRIDGE);

        // Check for unused referenced config descriptions
        Map<String, File> unusedConfigDescriptions = removeAll(allConfigDescriptions, allConfigDescriptionRefs);
        logMissingEntries(unusedConfigDescriptions, MESSAGE_UNUSED_URI_CONFIGURATION);

        // Check for missing ESH-INF files in the build.properties
        checkBuildProperties();
    }

    private void checkBuildProperties() {
        if (buildPropertiesFile != null) {
            IBuildEntry binIncludes = buildPropertiesFile.getEntry(IBuildEntry.BIN_INCLUDES);
            if (binIncludes != null) {
                String[] includedTokens = binIncludes.getTokens();
                // Exclude processed files that are included
                for (String included : includedTokens) {
                    // Iterator is used, as the collection will be modified
                    for (Iterator<Entry<Path, File>> it = eshInfFiles.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<Path, File> entry = it.next();
                        if (entry.getKey().startsWith(included)) {
                            it.remove();
                        }
                    }
                }
            }
            logMissingEntries(eshInfFiles, MESSAGE_NOT_INCLUDED_XML_FILE);
        }
    }

    private void processXmlFile(File xmlFile) throws CheckstyleException {
        if (isEmpty(xmlFile)) {
            log(0, MessageFormat.format(MESSAGE_EMPTY_FILE, xmlFile.getName()), xmlFile.getPath());
        } else {

            File fileParentDirectory = xmlFile.getParentFile();
            boolean isESHParentDirectory = ESH_INF_DIRECTORY.equals(fileParentDirectory.getParentFile().getName());

            if (isESHParentDirectory) {
                Path filePath = xmlFile.toPath();
                Path bundlePath = filePath.getParent().getParent().getParent();
                Path relativePath = bundlePath.relativize(filePath);
                eshInfFiles.put(relativePath, xmlFile);

                switch (fileParentDirectory.getName()) {
                    case THING_DIRECTORY: {
                        validateThingTypeFile(xmlFile);
                        break;
                    }
                    case BINDING_DIRECTORY: {
                        validateBindingFile(xmlFile);
                        break;
                    }
                    case CONFIGURATION_DIRECTORY: {
                        validateConfigFile(xmlFile);
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

    private void validateConfigFile(File xmlFile) throws CheckstyleException {
        // The allowed values are described in the config description XSD
        allConfigDescriptions.putAll(evaluateExpressionOnFile(xmlFile, CONFIG_DESCRIPTION_EXPRESSION));

        validateXmlAgainstSchema(xmlFile, configSchema);
    }

    private void validateBindingFile(File xmlFile) throws CheckstyleException {
        // The allowed values are described in the binding XSD
        allConfigDescriptionRefs.putAll(evaluateExpressionOnFile(xmlFile, CONFIG_DESCRIPTION_REF_EXPRESSION));
        allConfigDescriptions.putAll(evaluateExpressionOnFile(xmlFile, CONFIG_DESCRIPTION_EXPRESSION));

        validateXmlAgainstSchema(xmlFile, bindingSchema);
    }

    private void validateThingTypeFile(File xmlFile) throws CheckstyleException {
        // Process the files for all nodes below,
        // the allowed values are described in the thing description XSD
        allSupportedBridges.putAll(evaluateExpressionOnFile(xmlFile, SUPPORTED_BRIDGE_TYPE_REF_EXPRESSION));
        allBridgeTypes.putAll(evaluateExpressionOnFile(xmlFile, BRIDGE_TYPE_EXPRESSION));
        allConfigDescriptionRefs.putAll(evaluateExpressionOnFile(xmlFile, CONFIG_DESCRIPTION_REF_EXPRESSION));
        allConfigDescriptions.putAll(evaluateExpressionOnFile(xmlFile, CONFIG_DESCRIPTION_EXPRESSION));

        validateXmlAgainstSchema(xmlFile, thingSchema);
    }

    private void processBuildProperties(File file) throws CheckstyleException {
        try {
            buildPropertiesFile = parseBuildProperties(file);
        } catch (CheckstyleException e) {
            logger.error("Problem occurred while parsing the file {}", file.getPath(), e);
        }
    }

    private Map<String, File> evaluateExpressionOnFile(File xmlFile, String xPathExpression)
            throws CheckstyleException {
        Map<String, File> collection = new HashMap<>();
        NodeList nodes = getNodes(xmlFile, xPathExpression);

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                collection.put(nodes.item(i).getNodeValue(), xmlFile);
            }
        }
        return collection;
    }

    private NodeList getNodes(File xmlFile, String expression) throws CheckstyleException {
        Document document = parseDomDocumentFromFile(xmlFile);

        XPathExpression xpathExpression = compileXPathExpression(expression);

        NodeList nodes = null;
        try {
            nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            logger.error("Problem occurred while evaluating the expression {} on the {} file.", expression,
                    xmlFile.getName(), e);
        }
        return nodes;
    }

    private void validateXmlAgainstSchema(File xmlFile, String schemaPath) {
        URL schemaURL = getSchemaURL(schemaPath);
        if (schemaURL != null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(schemaURL);

                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(xmlFile));

            } catch (SAXParseException exception) {
                String message = exception.getMessage();
                // Removing the type of the logged message (For example - "cvc-complex-type.2.4.b: ...").
                message = message.substring(message.indexOf(":") + 2);
                int lineNumber = exception.getLineNumber();
                log(lineNumber, message, xmlFile.getPath());
            } catch (IOException | SAXException e) {
                logger.error("Problem occurred while parsing the file " + xmlFile.getName(), e);
            }
        } else {
            logger.warn("Unable to reach {}. XML validation will be skipped.", schemaPath);
        }
    }

    private URL getSchemaURL(String schemaPath) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(schemaPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }
            return url;
        } catch (IOException e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private <K, V> Map<K, V> removeAll(Map<K, V> firstMap, Map<K, V> secondMap) {
        Map<K, V> result = new HashMap<>(firstMap);
        result.keySet().removeAll(secondMap.keySet());
        return result;
    }

    private <K> void logMissingEntries(Map<K, File> collection, String message) {
        for (K element : collection.keySet()) {
            File xmlFile = collection.get(element);
            logMessage(xmlFile.getPath(), 0, xmlFile.getName(), MessageFormat.format(message, element));
        }
    }

    private void logMessage(String filePath, int line, String fileName, String message) {
        MessageDispatcher dispatcher = getMessageDispatcher();
        dispatcher.fireFileStarted(filePath);
        log(line, message, fileName);
        fireErrors(filePath);
        dispatcher.fireFileFinished(filePath);
    }
}
