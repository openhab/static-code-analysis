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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.EXPORT_PACKAGE_HEADER_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_EXTENSION;

import java.io.File;
import java.util.Set;

import org.apache.ivy.osgi.core.BundleInfo;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if a manifest file exports internal packages.
 *
 * @author Svilen Valkanov
 *
 */
public class ExportInternalPackageCheck extends AbstractStaticCheck {

    public ExportInternalPackageCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (isEmpty(fileText)) {
            log(0, "Manifest file is empty!", 0);
            return;
        }
        BundleInfo manifest = parseManifestFromFile(fileText);
        Set<?> exports = manifest.getExports();

        if (!exports.isEmpty()) {
            int lineNumber = findLineNumberSafe(fileText, EXPORT_PACKAGE_HEADER_NAME, 0,
                    "Header line number not found.");

            for (Object export : exports) {
                String packageName = export.toString();
                if (packageName.contains(".internal")) {
                    log(lineNumber, "Remove internal package export " + packageName, 0);
                }
            }
        }
    }
}
