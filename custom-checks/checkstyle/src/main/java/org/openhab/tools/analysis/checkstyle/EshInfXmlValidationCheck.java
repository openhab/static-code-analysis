/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractEshInfXmlCheck;
import org.openhab.tools.analysis.utils.CachingHttpClient;
import org.openhab.tools.analysis.utils.ContentReceviedCallback;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Validate the thing-types, binding and config xml-s against their xsd schemas.<br>
 * Check if all files from ESH-INF are included in the build.properties file.
 *
 * @author Aleksandar Kovachev - Initial implementation
 * @author Svlien Valkanov - Some code refactoring and cleanup,
 *         added check for the build.properties file,
 *         download schema files only once
 *
 */
public class EshInfXmlValidationCheck extends AbstractEshInfXmlCheck {
    private static final String MESSAGE_NOT_INCLUDED_XML_FILE = "The file {0} isn't included in the build.properties file. Good approach is to include all files by adding `ESH-INF/` value to the bin.includes property.";

    private final Log logger = LogFactory.getLog(this.getClass());

    private Map<Path, File> eshInfFiles = new HashMap<>();
    private IBuild buildPropertiesFile;

    private String thingSchema;
    private String bindingSchema;
    private String configSchema;

    private static Schema thingSchemaFile;
    private static Schema bindingSchemaFile;
    private static Schema configSchemaFile;

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
    public void beginProcessing(String charset) {
        ContentReceviedCallback<Schema> callback = new ContentReceviedCallback<Schema>() {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            @Override
            public Schema transform(byte[] content) {
                try {
                    InputStream is = new ByteArrayInputStream(content);
                    return schemaFactory.newSchema(new StreamSource(is));
                } catch (SAXException e) {
                    logger.error("Unable to parse schema ", e);
                    return null;
                }
            }
        };

        CachingHttpClient<Schema> cachingClient = new CachingHttpClient<>(callback);

        bindingSchemaFile = getXSD(bindingSchema, cachingClient);
        thingSchemaFile = getXSD(thingSchema, cachingClient);
        configSchemaFile = getXSD(configSchema, cachingClient);

        super.beginProcessing(charset);
    }

    @Override
    public void finishProcessing() {
        // Check for missing ESH-INF files in the build.properties
        checkBuildProperties();
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        logger.debug("Processing the " + file.getName());

        if (file.getName().equals(BUILD_PROPERTIES_FILE_NAME)) {
            processBuildProperties(fileText);
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
    protected void checkConfigFile(FileText xmlFileText) throws CheckstyleException {
        File xmlFile = xmlFileText.getFile();
        addToEshFiles(xmlFile);
        validateXmlAgainstSchema(xmlFile, configSchemaFile);
    }

    @Override
    protected void checkBindingFile(FileText xmlFileText) throws CheckstyleException {
        File xmlFile = xmlFileText.getFile();
        addToEshFiles(xmlFile);
        validateXmlAgainstSchema(xmlFile, bindingSchemaFile);
    }

    @Override
    protected void checkThingTypeFile(FileText xmlFileText) throws CheckstyleException {
        File xmlFile = xmlFileText.getFile();
        addToEshFiles(xmlFile);
        validateXmlAgainstSchema(xmlFile, thingSchemaFile);
    }

    private void processBuildProperties(FileText fileText) throws CheckstyleException {
        try {
            buildPropertiesFile = parseBuildProperties(fileText);
        } catch (CheckstyleException e) {
            logger.error("Problem occurred while parsing the file " + fileText.getFile().getPath(), e);
        }
    }

    private void validateXmlAgainstSchema(File xmlFile, Schema schema) {
        if (schema != null) {
            try {
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
            logger.warn("XML validation will be skipped as the schema file download failed.");
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

    private Schema getXSD(String schemaUrlString, CachingHttpClient<Schema> client) {
        try {
            URL schemaUrl = new URL(schemaUrlString);
            return client.get(schemaUrl);
        } catch (IOException e) {
            String message = MessageFormat.format("Unable to get XSD file {0} : {1}", schemaUrlString, e.getMessage());
            logger.error(message, e);
            return null;
        }

    }
}
