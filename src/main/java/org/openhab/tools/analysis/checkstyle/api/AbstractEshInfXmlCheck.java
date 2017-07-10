/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.api;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Abstract class for checks that will validate .xml files located in the ESH-INF directory.
 *
 * More information can be found
 * <a href="https://eclipse.org/smarthome/documentation/development/bindings/xml-reference.html">here</a>
 *
 * @author Aleksandar Kovachev - Initial implementation
 * @author Svlien Valkanov - Some code refactoring and cleanup, added check for the build.properties file
 *
 */
public abstract class AbstractEshInfXmlCheck extends AbstractStaticCheck {
    public static final String THING_DIRECTORY = "thing";
    public static final String BINDING_DIRECTORY = "binding";
    public static final String CONFIGURATION_DIRECTORY = "config";

    private static final String MESSAGE_EMPTY_FILE = "The file {0} should not be empty.";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractEshInfXmlCheck() {
        setFileExtensions(XML_EXTENSION);
    }

    @Override
    public void beginProcessing(String charset) {
        logger.debug("Executing the {}", this.getClass().getSimpleName());
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        String fileName = file.getName();

        if (FilenameUtils.getExtension(fileName).equals(XML_EXTENSION)) {
            processXmlFile(file);
        }
    }

    private void processXmlFile(File xmlFile) throws CheckstyleException {
        if (isEmpty(xmlFile)) {
            log(0, MessageFormat.format(MESSAGE_EMPTY_FILE, xmlFile.getName()), xmlFile.getPath());
        } else {

            File fileParentDirectory = xmlFile.getParentFile();
            boolean isESHParentDirectory = ESH_INF_DIRECTORY.equals(fileParentDirectory.getParentFile().getName());

            if (isESHParentDirectory) {
                switch (fileParentDirectory.getName()) {
                    case THING_DIRECTORY: {
                        checkThingTypeFile(xmlFile);
                        break;
                    }
                    case BINDING_DIRECTORY: {
                        checkBindingFile(xmlFile);
                        break;
                    }
                    case CONFIGURATION_DIRECTORY: {
                        checkConfigFile(xmlFile);
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

    /**
     * Validate a .xml file located in the ESH-INF/config directory
     *
     * @param xmlFile the file to validate
     * @throws CheckstyleException when exception occurred during XML processing
     */
    protected abstract void checkConfigFile(File xmlFile) throws CheckstyleException;

    /**
     * Validate a .xml file located in the ESH-INF/binding directory
     *
     * @param xmlFile the file to validate
     * @throws CheckstyleException when exception occurred during XML processing
     */
    protected abstract void checkBindingFile(File xmlFile) throws CheckstyleException;

    /**
     * Validate a .xml file located in the ESH-INF/thing directory
     *
     * @param xmlFile the file to validate
     * @throws CheckstyleException when exception occurred during XML processing
     */
    protected abstract void checkThingTypeFile(File xmlFile) throws CheckstyleException;

}
