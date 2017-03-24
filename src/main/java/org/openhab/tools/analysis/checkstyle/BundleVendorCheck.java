/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks if a manifest file contains the expected bundle vendor
 *
 * @author Martin van Wingerden
 */
public class BundleVendorCheck extends AbstractStaticCheck {
    private final static String MANIFEST_EXTENSION = "MF";
    private static final String REQUIRED_PREFIX = "Bundle-Vendor: ";
    private List<String> allowedValues;

    public BundleVendorCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    public void setAllowedValues(String[] allowedValues) {
        this.allowedValues = Arrays.asList(allowedValues);
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        if (isEmpty(file)) {
            // not our task to report
            return;
        }

        List<String> bundleVendors = lines.stream()
                .filter(line -> line.toLowerCase().startsWith("bundle-vendor"))
                .collect(Collectors.toList());

        boolean tooMany = false;
        if (bundleVendors.size() == 0) {
            log(0, "\"Bundle-Vendor\" is missing");
            return;
        } else if (bundleVendors.size() > 1) {
            tooMany = true;
        }

        int lineNumber = 0;
        for (String bundleVendor : bundleVendors) {
            lineNumber = findLineNumber(lines, bundleVendor, lineNumber);

            if (tooMany) {
                log(lineNumber, "Only one \"Bundle-Vendor\" was expected.");
            }
            if (!bundleVendor.startsWith(REQUIRED_PREFIX)) {
                log(lineNumber, "Expect eg. \"Bundle-Vendor: openHAB\" got \"" + bundleVendor + "\"");
            } else {
                String onlyValue = bundleVendor.replace(REQUIRED_PREFIX, "");
                if (!allowedValues.contains(onlyValue)) {
                    log(lineNumber, "Unexpected \"" + onlyValue + "\", only allowed options: " + allowedValues);
                }
            }
        }
    }
}
