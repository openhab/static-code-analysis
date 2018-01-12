/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BIN_INCLUDES_PROPERTY_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BUILD_PROPERTIES_FILE_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.OUTPUT_PROPERTY_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.PROPERTIES_EXTENSION;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.SOURCE_PROPERTY_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiPredicate;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;


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
    private List<String> possibleOutputValues;
    private List<String> possibleSourceValues;

    public BuildPropertiesCheck() {
        setFileExtensions(PROPERTIES_EXTENSION);
    }

    /**
     * Sets the configuration property for the expected values for
     * the bin.includes property in the build.properties file.
     *
     * @param binIncludesValues values of the bin.includes property
     */
    public void setExpectedBinIncludesValues(String[] binIncludesValues) {
        this.expectedBinIncludesValues = Arrays.asList(binIncludesValues);
    }

    /**
     * Sets the Configuration property for the expected values for
     * the output property in the build.properties file.
     *
     * @param outputValues values of the output property
     */
    public void setPossibleOutputValues(String[] outputValues) {
        this.possibleOutputValues = Arrays.asList(outputValues);
    }

    /**
     * Sets the configuration property for the possible values for
     * the source property in the build.properties file.
     * From the possible values only these that point to an existing directory
     * are expected in the build.properties file
     *
     * @param possibleSourceValues possible source values
     */
    public void setPossibleSourceValues(String[] possibleSourceValues) {
        // We will have to remove elements from the collection.
        // Using Arrays.asList returns a list backed by the original which does't allow removal
        this.possibleSourceValues = new ArrayList<String>(Arrays.asList(possibleSourceValues));
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        String fileName = file.getName();
        if (fileName.equals(BUILD_PROPERTIES_FILE_NAME)) {
            if (!isEmpty(fileText)) {
                processBuildProperties(fileText);
            } else {
                log(0, EMPTY_FILE_MSG);
            }
        }
    }

    private void processBuildProperties(FileText fileText) throws CheckstyleException {
        // We ignore the exceptions thrown by the parseBuildProperties() method. A corrupt build.properties file will
        // fail the Maven build in the compile phase, so we should not care about this case in the validate phase
        IBuild buildPropertiesFile = parseBuildProperties(fileText);

        IBuildEntry binIncludesValue = buildPropertiesFile.getEntry(BIN_INCLUDES_PROPERTY_NAME);
        if (binIncludesValue != null) {
            List<String> missingValues = findMissingValues(binIncludesValue, expectedBinIncludesValues, true,
                    (a, b) -> a.containsAll(b));
            logMissingValues(fileText, BIN_INCLUDES_PROPERTY_NAME, missingValues, MISSING_BIN_INCLUDES_VALUE_MSG);
        } else {
            // bin.includes property is the single required property
            log(0, MISSING_BIN_INCLUDES_PROPERTY_MSG);
        }

        // some bundles don't contain any source code (only include some library),
        // so the source and output properties are not required
        IBuildEntry outputPropertyValue = buildPropertiesFile.getEntry(OUTPUT_PROPERTY_NAME);
        if (outputPropertyValue != null) {
            List<String> possibleMissingValues = findMissingValues(outputPropertyValue, possibleOutputValues, false,
                    (a, b) -> CollectionUtils.containsAny(a, b));

            // We would not like to log all possible values in a separate message
            if (!possibleMissingValues.isEmpty()) {
                List<String> valuesToLog = new ArrayList<String>();
                valuesToLog.add("Any of " + possibleOutputValues.toString());
                logMissingValues(fileText, OUTPUT_PROPERTY_NAME, valuesToLog, MISSING_OUTPUT_VALUE_MSG);
            }

        }

        IBuildEntry sourcePropertyValue = buildPropertiesFile.getEntry(SOURCE_PROPERTY_NAME);
        if (sourcePropertyValue != null) {
            // the build properties file is located directly in the base directory of the bundle
            File bundleBaseDir = fileText.getFile().getParentFile();
            removeNonExistingSourceDirs(bundleBaseDir);
            List<String> missingValues = findMissingValues(sourcePropertyValue, possibleSourceValues, false,
                    (a, b) -> a.containsAll(b));
            logMissingValues(fileText, SOURCE_PROPERTY_NAME, missingValues, MISSING_SRC_VALUE_MSG);
        }
    }

    /**
     * Checks if a property contains all expected values and logs messages if some of the expected values are missing
     *
     * @param propertyValue - the value of the property
     * @param expectedPropertyValues - expected values
     * @param strictSyntax - if set to true, the values should end with "/", otherwise they could end with
     */
    private List<String> findMissingValues(IBuildEntry propertyValue, List<String> expectedPropertyValues,
            boolean strictSyntax, BiPredicate<List<String>, List<String>> condition) {
        List<String> values = Arrays.asList(propertyValue.getTokens());

        if (!strictSyntax) {
            removeSubstringAtEnd(values, "/");
            removeSubstringAtEnd(expectedPropertyValues, "/");
        }

        if (expectedPropertyValues != null && values != null) {
            if (!condition.test(values, expectedPropertyValues)) {
                return removeAll(expectedPropertyValues, values);
            }
        }
        return Collections.emptyList();
    }

    /**
     *
     * @param missingValueMessage - message to be used, when a value is missing
     *
     */
    private void logMissingValues(FileText fileText, String property, List<String> missingValues, String messsage) {
        for (String missingValue : missingValues) {
            int lineNumber = findLineNumberSafe(fileText, property, 0, "Property line number not found.");
            log(lineNumber, messsage + missingValue);
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
