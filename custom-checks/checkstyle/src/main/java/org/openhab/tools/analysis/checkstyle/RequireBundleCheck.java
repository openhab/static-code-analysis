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
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BUNDLE_SYMBOLIC_NAME_HEADER_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.FRAGMENT_HOST_HEADER_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_EXTENSION;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.REQUIRE_BUNDLE_HEADER_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the MANIFEST.MF file contains any "Require-Bundle" entries. Exceptions may be configured using the
 * configuration property 'checkstyle.requireBundleCheck.allowedBundles', e.g. "org.junit,org.mokcito,org.hamcrest"
 * which is the default.
 *
 * @author Petar Valchev
 * @author Henning Treu - Allow bundle exceptions from the check.
 *
 */
public class RequireBundleCheck extends AbstractStaticCheck {
    private final Log logger = LogFactory.getLog(RequireBundleCheck.class);

    private List<String> allowedRequireBundles = Collections.emptyList();

    public RequireBundleCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    // configuration property for the allowed RequireBundle entries
    @SuppressWarnings("unchecked")
    public void setAllowedRequireBundles(String[] bundles) {
        allowedRequireBundles = Arrays.asList(bundles);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) {
        try {
            // We use Manifest class here instead of ManifestParser,
            // because it is easier to get the content of the headers
            // in the MANIFEST.MF
            Manifest manifest = new Manifest(new FileInputStream(file));
            Attributes attributes = manifest.getMainAttributes();

            String fragmentHost = attributes.getValue(FRAGMENT_HOST_HEADER_NAME);
            String bundleSymbolicName = attributes.getValue(BUNDLE_SYMBOLIC_NAME_HEADER_NAME);

            boolean testBundle = false;
            if (StringUtils.isNotBlank(fragmentHost) && StringUtils.isNotBlank(bundleSymbolicName)) {
                testBundle = bundleSymbolicName.startsWith(fragmentHost)
                        && bundleSymbolicName.substring(fragmentHost.length()).startsWith(".test");
            }

            String requireBundleHeaderValue = attributes.getValue(REQUIRE_BUNDLE_HEADER_NAME);
            if (requireBundleHeaderValue != null && !testBundle) {

                int lineNumber = findLineNumberSafe(fileText, requireBundleHeaderValue, 0,
                        REQUIRE_BUNDLE_HEADER_NAME + " header line number not found.");
                log(lineNumber, "The MANIFEST.MF file must not contain any Require-Bundle entries. "
                        + "Instead, Import-Package must be used.");
            } else if (requireBundleHeaderValue != null && testBundle) {
                String[] bundleNames = requireBundleHeaderValue.split(",");
                for (String bundleName : bundleNames) {
                    if (!allowedRequireBundles.contains(bundleName)) {
                        int lineNumber = findLineNumberSafe(fileText, requireBundleHeaderValue, 0,
                                "Header value not found.");
                        log(lineNumber,
                                "The MANIFEST.MF file of a test fragment must not contain Require-Bundle entries other than "
                                        + getAllowedBundlesString() + ".");
                        break;
                    }
                }
            }
        } catch (

        FileNotFoundException e) {
            logger.error("An exception was thrown while trying to open the file " + file.getPath(), e);
        } catch (IOException e) {
            logger.error("An exception was thrown while trying to read the file " + file.getPath(), e);
        }
    }

    private String getAllowedBundlesString() {
        StringBuilder sb = new StringBuilder();
        for (String bundleName : allowedRequireBundles) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(bundleName);
        }

        return sb.toString();
    }
}
