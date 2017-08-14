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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractEshInfXmlCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Validate the thing-types, binding and config xml-s against their xsd schemas.<br>
 * Check if all files from ESH-INF are included in the build.properties file.
 *
 * @author Aleksandar Kovachev - Initial implementation
 * @author Svlien Valkanov - Some code refactoring and cleanup, added check for the build.properties file
 *
 */
public class EshInfXmlValidationCheck extends AbstractEshInfXmlCheck {

    private static final String MESSAGE_NOT_INCLUDED_XML_FILE = "The file {0} isn't included in the build.properties file. Good approach is to include all files by adding `ESH-INF/` value to the bin.includes property.";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<Path, File> eshInfFiles = new HashMap<>();
    private IBuild buildPropertiesFile;

    private String thingSchema;
    private String bindingSchema;
    private String configSchema;

    /**
     * Sets the configuration property for the thing schema file.
     *
     * @param thingSchema URL of the thing schema file
     */
    public void setThingSchema(String thingSchema) {
        this.thingSchema = thingSchema;
    }

    /**
     * Sets the configuration property for the binding schema file.
     *
     * @param bindingSchema URL of the binding schema file
     */
    public void setBindingSchema(String bindingSchema) {
        this.bindingSchema = bindingSchema;
    }

    /**
     * Sets the configuration property for the config schema file.
     *
     * @param configSchema URL of the config schema file
     */
    public void setConfigSchema(String configSchema) {
        this.configSchema = configSchema;
    }

    public EshInfXmlValidationCheck() {
        setFileExtensions(XML_EXTENSION, PROPERTIES_EXTENSION);
    }

    @Override
    public void finishProcessing() {
        // Check for missing ESH-INF files in the build.properties
        checkBuildProperties();
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        logger.debug("Processing the {}", file.getName());

        if (file.getName().equals(BUILD_PROPERTIES_FILE_NAME)) {
            processBuildProperties(file);
        } else {
            super.processFiltered(file, fileText);
        }
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

    @Override
    protected void checkConfigFile(File xmlFile) throws CheckstyleException {
        addToEshFiles(xmlFile);
        validateXmlAgainstSchema(xmlFile, configSchema);
    }

    @Override
    protected void checkBindingFile(File xmlFile) throws CheckstyleException {
        addToEshFiles(xmlFile);
        validateXmlAgainstSchema(xmlFile, bindingSchema);
    }

    @Override
    protected void checkThingTypeFile(File xmlFile) throws CheckstyleException {
        addToEshFiles(xmlFile);
        validateXmlAgainstSchema(xmlFile, thingSchema);
    }

    private void processBuildProperties(File file) throws CheckstyleException {
        try {
            buildPropertiesFile = parseBuildProperties(file);
        } catch (CheckstyleException e) {
            logger.error("Problem occurred while parsing the file {}", file.getPath(), e);
        }
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
                logger.error("Problem occurred while parsing the file {}", xmlFile.getName(), e);
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

    private <K> void logMissingEntries(Map<K, File> collection, String message) {
        for (K element : collection.keySet()) {
            File xmlFile = collection.get(element);
            logMessage(xmlFile.getPath(), 0, xmlFile.getName(), MessageFormat.format(message, element));
        }
    }

    private void addToEshFiles(File xmlFile) {
        Path filePath = xmlFile.toPath();
        Path bundlePath = filePath.getParent().getParent().getParent();
        Path relativePath = bundlePath.relativize(filePath);
        eshInfFiles.put(relativePath, xmlFile);
    }
}
