/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_EXTENSION;

import java.io.File;
import java.util.Set;

import org.apache.ivy.osgi.core.BundleInfo;
import org.apache.ivy.osgi.core.BundleRequirement;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if every bundle has optional Import-Package dependency to org.eclipse.jdt.annotation and generates a <b>
 * WARNING </b> if it is missing.
 *
 * @author Kristina Simova - Initial contribution
 */
public class AnnotationDependencyCheck extends AbstractStaticCheck {
    private static final String DEPENDENCY_NAME = "org.eclipse.jdt.annotation";
    private static final String DEPENDENCY_RESOLUTION = "optional";
    private static final String IMPORT_PACKAGE_HEADER_NAME = "Import-Package";
    private static final String WARNING_MESSAGE_MISSING_DEPENDENCY_RESOLUTION = "Every bundle should have optional Import-Package dependency to org.eclipse.jdt.annotation.";

    public AnnotationDependencyCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (isEmpty(fileText)) {
            // it should be handled by another check
            return;
        }
        BundleInfo bundleInfo = parseManifestFromFile(fileText);
        Set<BundleRequirement> bundleRequirements = bundleInfo.getRequirements();
        int lineNo = findLineNumberSafe(fileText, IMPORT_PACKAGE_HEADER_NAME, 0,
                "Could not find Import-Package dependency!");
        if (lineNo == 0) {
            // it means that the MANIFEST.MF file has no Import-Package dependency
            return;
        }
        boolean hasOptionalDependencyResolution = false;
        for (BundleRequirement requirement : bundleRequirements) {
            String dependencyName = requirement.getName();
            String dependencyResolution = requirement.getResolution();
            if (DEPENDENCY_NAME.equals(dependencyName) && DEPENDENCY_RESOLUTION.equals(dependencyResolution)) {
                hasOptionalDependencyResolution = true;
                break;
            }
        }
        if (!hasOptionalDependencyResolution) {
            log(lineNo, WARNING_MESSAGE_MISSING_DEPENDENCY_RESOLUTION);
        }
    }
}
