/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.apache.ivy.osgi.core.BundleRequirement;
import org.apache.ivy.osgi.core.ExportPackage;
import org.apache.ivy.osgi.core.ManifestParser;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if all of the exported packages are imported by the bundle itself
 *
 * @author Mihaela Memova
 */
public class ImportExportedPackagesCheck extends AbstractStaticCheck {
    private static final String NOT_IMPORTED_PACKAGE_MESSAGE = "The exported package is not imported";
    private static final String EXPORT_PACKAGES_HEADER = "Export-Package:";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ImportExportedPackagesCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        int lineNumberBeforeExportsStart = findLineNumber(lines, EXPORT_PACKAGES_HEADER, 0) - 1;

        try {
            Set<ExportPackage> exports = ManifestParser.parseManifest(file).getExports();
            Set<BundleRequirement> imports = ManifestParser.parseManifest(file).getImports();

            for (ExportPackage export : exports) {
                if (!isPackageImported(imports, export)) {
                    int lineToLog = findLineNumber(lines, export.toString(), lineNumberBeforeExportsStart);
                    log(lineToLog, NOT_IMPORTED_PACKAGE_MESSAGE);
                }
            }
        } catch (IOException e) {
            logger.error("An error occured while processing the file {}", file.getPath(), e);
        } catch (ParseException e) {
            logger.error("An error occured while trying to parse the MANIFEST {}", file.getPath(), e);
        }
    }

    /**
     * Checks if a particular package is imported. The default implementation of
     * {@link Set#contains(Object)} cannot be used since an {@link ExportPackage} object
     * is searched in a set of {link {@link BundleRequirement} objects.
     *
     * @param imports - set of all imported packages
     * @param searchedPackage - the package that has to be checked
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
