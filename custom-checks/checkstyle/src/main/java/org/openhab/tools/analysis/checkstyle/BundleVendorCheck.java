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

import org.openhab.tools.analysis.checkstyle.api.AbstractManifestAttributeCheck;

/**
 * Checks if a manifest file contains the expected bundle vendor
 *
 * @author Martin van Wingerden
 */
public class BundleVendorCheck extends AbstractManifestAttributeCheck {
    private static final String ATTRIBUTE = "Bundle-Vendor";
    private static final String ATTRIBUTE_EXAMPLE_VALUE = "openHAB";
    private static final int SINGLE_OCCURRENCE = 1;

    public BundleVendorCheck() {
        super(ATTRIBUTE, ATTRIBUTE_EXAMPLE_VALUE, SINGLE_OCCURRENCE);
    }
}
