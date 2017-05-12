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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if a build.properties file is valid.
 *
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Changes in the logic for the source property
 *
 */
public class BuildPropertiesCheck extends AbstractStaticCheck {
    private static final String MISSING_PROPERTY_MSG = "Missing %s property in the %s file.";
    private static final String MISSING_VALUE_MSG = "Property  %s in the %s file is missing value: ";

    private static final String MISSING_BIN_INCLUDES_PROPERTY_MSG = String.format(MISSING_PROPERTY_MSG,
            BIN_INCLUDES_PROPERTY_NAME, BUILD_PROPERTIES_FILE_NAME);

    private static final String MISSING_BIN_INCLUDES_VALUE_MSG = String.format(MISSING_VALUE_MSG,
            BIN_INCLUDES_PROPERTY_NAME, BUILD_PROPERTIES_FILE_NAME);
    private static final String MISSING_OUTPUT_VALUE_MSG = String.format(MISSING_VALUE_MSG, OUTPUT_PROPERTY_NAME,
            BUILD_PROPERTIES_FILE_NAME);
    private static final String MISSING_SRC_VALUE_MSG = String.format(MISSING_VALUE_MSG, SOURCE_PROPERTY_NAME,
            BUILD_PROPERTIES_FILE_NAME);

    private static final String EMPTY_FILE_MSG = String.format("Empty %s file", BUILD_PROPERTIES_FILE_NAME);

    /*
     * Used for configuration properties
     */
    private List<String> expectedBinIncludesValues;
    private List<String> expectedOutputValues;
    private List<String> possibleSourceValues;

    public BuildPropertiesCheck() {
        setFileExtensions(PROPERTIES_EXTENSION);
    }

    /**
     * Sets the configuration property for the expected values for
     * the bin.includes property in the build.properties file.
     */
    public void setExpectedBinIncludesValues(String[] binIncludesValues) {
        this.expectedBinIncludesValues = Arrays.asList(binIncludesValues);
    }

    /**
     * Sets the Configuration property for the expected values for
     * the output property in the build.properties file.
     */
    public void setExpectedOutputValues(String[] outputValues) {
        this.expectedOutputValues = Arrays.asList(outputValues);
    }

    /**
     * Sets the configuration property for the possible values for
     * the source property in the build.properties file.
     * From the possible values only these that point to an existing directory
     * are expected in the build.properties file
     */
    public void setPossibleSourceValues(String[] possibleSourceValues) {
        // We will have to remove elements from the collection.
        // Using Arrays.asList returns a list backed by the original which does't allow removal
        this.possibleSourceValues = new ArrayList<String>(Arrays.asList(possibleSourceValues));
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        String fileName = file.getName();
        if (fileName.equals(BUILD_PROPERTIES_FILE_NAME)) {
            if (!isEmpty(file)) {
                processBuildProperties(file, lines);
            } else {
                log(0, EMPTY_FILE_MSG);
            }
        }
    }

    private void processBuildProperties(File file, List<String> lines) throws CheckstyleException {
        // We ignore the exceptions thrown by the parseBuildProperties() method. A corrupt build.properties file will
        // fail the Maven build in the compile phase, so we should not care about this case in the validate phase
        IBuild buildPropertiesFile = parseBuildProperties(file);

        IBuildEntry binIncludesValue = buildPropertiesFile.getEntry(BIN_INCLUDES_PROPERTY_NAME);
        if (binIncludesValue != null) {
            checkForExpectedValue(BIN_INCLUDES_PROPERTY_NAME, binIncludesValue, expectedBinIncludesValues,
                    MISSING_BIN_INCLUDES_VALUE_MSG, lines, true);
        } else {
            // bin.includes property is the single required property
            log(0, MISSING_BIN_INCLUDES_PROPERTY_MSG);
        }

        // some bundles don't contain any source code (only include some library),
        // so the source and output properties are not required
        IBuildEntry outputPropertyValue = buildPropertiesFile.getEntry(OUTPUT_PROPERTY_NAME);
        if (outputPropertyValue != null) {
            checkForExpectedValue(OUTPUT_PROPERTY_NAME, outputPropertyValue, expectedOutputValues,
                    MISSING_OUTPUT_VALUE_MSG, lines, false);
        }

        IBuildEntry sourcePropertyValue = buildPropertiesFile.getEntry(SOURCE_PROPERTY_NAME);
        if (sourcePropertyValue != null) {
            // the build properties file is located directly in the base directory of the bundle
            File bundleBaseDir = file.getParentFile();
            removeNonExistingSourceDirs(bundleBaseDir);
            checkForExpectedValue(SOURCE_PROPERTY_NAME, sourcePropertyValue, possibleSourceValues,
                    MISSING_SRC_VALUE_MSG, lines, false);
        }
    }

    /**
     * Checks if a property contains all expected values and logs messages if some of the expected values are missing
     *
     * @param property - the name of the property,needed only to locate the line in the file
     * @param propertyValue - the value of the property
     * @param expectedPropertyValues - expected values
     * @param missingValueMessage - message to be used, when a value is missing
     * @param lines - a list with the lines of the file
     * @param strictSyntax - if set to true, the values should end with "/", otherwise they could end with
     */
    private void checkForExpectedValue(String property, IBuildEntry propertyValue, List<String> expectedPropertyValues,
            String missingValueMessage, List<String> lines, boolean strictSyntax) {
        List<String> values = Arrays.asList(propertyValue.getTokens());

        if (!strictSyntax) {
            removeSubstringAtEnd(values, "/");
            removeSubstringAtEnd(expectedPropertyValues, "/");
        }

        if (expectedPropertyValues != null && values != null) {
            if (!values.containsAll(expectedPropertyValues)) {
                List<String> missingValues = removeAll(expectedPropertyValues, values);

                for (String missingValue : missingValues) {
                    log(findLineNumber(lines, property, 0), missingValueMessage + missingValue);
                }

            }
        }
    }

    private void removeSubstringAtEnd(List<String> values, String substr) {
        ListIterator<String> iterator = values.listIterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (value.endsWith(substr)) {
                iterator.set(value.substring(0, value.length() - 1));
            }
        }
    }

    private void removeNonExistingSourceDirs(File bundleBaseDir) {
        for (Iterator<String> iterator = possibleSourceValues.iterator(); iterator.hasNext();) {
            String relativePath = iterator.next();
            File file = new File(bundleBaseDir, relativePath);
            if (!file.exists()) {
                iterator.remove();
            }
        }
    }

    private <T> List<T> removeAll(List<T> first, List<T> second) {
        List<T> result = new ArrayList<>(first);
        result.removeAll(second);
        return result;
    }
}
