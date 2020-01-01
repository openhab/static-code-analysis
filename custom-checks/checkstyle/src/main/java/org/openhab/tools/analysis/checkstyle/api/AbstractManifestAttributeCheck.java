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
package org.openhab.tools.analysis.checkstyle.api;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_EXTENSION;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if a manifest file contains the expected attribute
 *
 * @author Martin van Wingerden
 */
public class AbstractManifestAttributeCheck extends AbstractStaticCheck {
    private final String attribute;
    private final String exampleValue;
    private final int maxOccurrences;
    private final String lowerCasePrefix;
    private final String requiredPrefix;
    private List<String> allowedValues;

    public AbstractManifestAttributeCheck(String attribute, String exampleValue, int maxOccurrences) {
        setFileExtensions(MANIFEST_EXTENSION);
        this.attribute = attribute;
        this.exampleValue = exampleValue;
        this.maxOccurrences = maxOccurrences;
        lowerCasePrefix = attribute.toLowerCase();
        requiredPrefix = attribute + ": ";
    }

    public void setAllowedValues(String[] allowedValues) {
        this.allowedValues = Arrays.asList(allowedValues);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (isEmpty(fileText)) {
            // not our task to report
            return;
        }
        String[] lines = fileText.toLinesArray();

        List<String> bundleVendors = Arrays.stream(lines).filter(line -> line.toLowerCase().startsWith(lowerCasePrefix))
                .collect(Collectors.toList());

        boolean tooMany = false;
        if (bundleVendors.size() == 0) {
            log(0, String.format("\"%s\" is missing", attribute));
            return;
        } else {
            if (bundleVendors.size() > maxOccurrences) {
                tooMany = true;
            }
        }

        int lineNumber = 0;
        for (String bundleVendor : bundleVendors) {
            lineNumber = findLineNumberSafe(fileText, bundleVendor, lineNumber, "Bundle vendor line number not found.");

            if (tooMany) {
                log(lineNumber, String.format("Only %d \"%s\" was expected.", maxOccurrences, attribute));
            }
            if (!bundleVendor.startsWith(requiredPrefix)) {
                log(lineNumber,
                        String.format("Expect eg. \"%s%s\" got \"%s\"", requiredPrefix, exampleValue, bundleVendor));
            } else {
                String onlyValue = bundleVendor.replace(requiredPrefix, "");
                if (!allowedValues.contains(onlyValue)) {
                    log(lineNumber,
                            String.format("Unexpected \"%s\", only allowed options: %s", onlyValue, allowedValues));
                }
            }
        }
    }
}
