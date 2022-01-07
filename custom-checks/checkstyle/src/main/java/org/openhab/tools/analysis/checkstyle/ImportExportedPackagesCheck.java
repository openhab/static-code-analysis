/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Set;

import org.apache.ivy.osgi.core.BundleRequirement;
import org.apache.ivy.osgi.core.ExportPackage;
import org.apache.ivy.osgi.core.ManifestParser;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if all of the exported packages are imported by the bundle itself
 *
 * @author Mihaela Memova - Initial contribution
 */
public class ImportExportedPackagesCheck extends AbstractStaticCheck {
    private static final String NOT_IMPORTED_PACKAGE_MESSAGE = "The exported package `{0}` is not imported";

    private final Logger logger = LoggerFactory.getLogger(ImportExportedPackagesCheck.class);

    public ImportExportedPackagesCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        try {
            Set<ExportPackage> exports = ManifestParser.parseManifest(file).getExports();
            Set<BundleRequirement> imports = ManifestParser.parseManifest(file).getImports();

            if (!exports.isEmpty()) {
                int lineToLog = findLineNumberSafe(fileText, EXPORT_PACKAGE_HEADER_NAME, 0,
                        EXPORT_PACKAGE_HEADER_NAME + " header line number not found.");

                for (ExportPackage export : exports) {
                    if (!isPackageImported(imports, export)) {
                        log(lineToLog, MessageFormat.format(NOT_IMPORTED_PACKAGE_MESSAGE, export.toString()));
                    }
                }
            }

        } catch (IOException e) {
            logger.error("An error occured while processing the file {}", file.getPath(), e);
        } catch (ParseException e) {
            logger.error("An error occured while trying to parse the MANIFEST: {}", file.getPath(), e);
        }
    }

    /**
     * Checks if a particular package is imported. The default implementation of
     * {@link Set#contains(Object)} cannot be used since an {@link ExportPackage} object
     * is searched in a set of {link {@link BundleRequirement} objects.
     *
     * @param imports set of all imported packages
     * @param searchedPackage the package that has to be checked
     * @return true if the imports contains the package, false otherwise
     */
    private boolean isPackageImported(Set<BundleRequirement> imports, ExportPackage searchedPackage) {
        for (Object o : imports) {
            if ((o.toString()).equals(searchedPackage.toString())) {
                return true;
            }
        }
        return false;
    }
}
