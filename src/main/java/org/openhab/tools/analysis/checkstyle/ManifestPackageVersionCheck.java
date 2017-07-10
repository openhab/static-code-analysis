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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ivy.osgi.core.BundleInfo;
import org.apache.ivy.osgi.core.BundleRequirement;
import org.apache.ivy.osgi.core.ExportPackage;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if the MANIFEST.MF file has any version constraints on imported and exported packages.
 *
 *
 * @author Petar Valchev - Initial contribution.
 * @author Aleksandar Kovachev - Refactored the code and added check for exported packages.
 * @author Svlien Valkanov - Renamed the check
 */
public class ManifestPackageVersionCheck extends AbstractStaticCheck {
    private static final String VERSION_USED_MSG = "The version of the package %s should not be specified";

    private static final String PACKAGE_PATTERN = "[a-zA-Z0-9\\._-]*";

    private List<String> ignoreImportedPackages;
    private List<String> ignoreExportedPackages;

    public ManifestPackageVersionCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    /**
     * Sets the configuration property for ignored imported packages.
     *
     * @param ignoreImportedPackages imported packages that will be ignored
     */
    public void setIgnoreImportedPackages(String[] ignoreImportedPackages) {
        this.ignoreImportedPackages = Arrays.asList(ignoreImportedPackages);
    }

    /**
     * Sets the configuration property for ignored exported packages.
     *
     * @param ignoreExportedPackages exported packages that will be ignored
     */
    public void setIgnoreExportedPackages(String[] ignoreExportedPackages) {
        this.ignoreExportedPackages = Arrays.asList(ignoreExportedPackages);
    }

    @Override
    protected void processFiltered(File manifestFile, List<String> lines) throws CheckstyleException {
        BundleInfo manifest = parseManifestFromFile(manifestFile);

        checkVersionOfImportedPackages(manifest, lines);

        checkVersionOfExportedPackages(manifest, lines);
    }

    private void checkVersionOfImportedPackages(BundleInfo manifest, List<String> lines) {
        Set<BundleRequirement> imports = manifest.getRequirements();

        int lineNumber = 0;
        for (BundleRequirement requirement : imports) {
            String requirementName = requirement.getName();
            if (requirement.getVersion() != null && !isIgnoredPackage(ignoreImportedPackages, requirementName)) {
                lineNumber = findLineNumber(lines, requirementName, lineNumber);
                log(lineNumber, String.format(VERSION_USED_MSG, requirementName));
            }
        }
    }

    private void checkVersionOfExportedPackages(BundleInfo manifest, List<String> lines) {
        Set<ExportPackage> exports = manifest.getExports();

        int lineNumber = 0;
        for (ExportPackage exportPackage : exports) {
            String exportedPackageName = exportPackage.getName();

            // If the package version is not with the default version of the exported packages by the ManifestParser
            // and the package is not ignored from the configuration.
            if (!exportPackage.getVersion().equals(BundleInfo.DEFAULT_VERSION)
                    && !isIgnoredPackage(ignoreExportedPackages, exportedPackageName)) {
                lineNumber = findLineNumber(lines, exportedPackageName, lineNumber);
                log(lineNumber, String.format(VERSION_USED_MSG, exportedPackageName));
            }
        }
    }

    /**
     * Checks if a package is ignored
     *
     * @param ignoredPackages - the packages that are ignored, regex expressions are allowed
     * @param packageName - the package to check
     * @return true if the package is ignored, false otherwise
     */
    private boolean isIgnoredPackage(List<String> ignoredPackages, String packageName) {
        for (String packages : ignoredPackages) {
            packages = packages.replaceAll("\\.\\*", PACKAGE_PATTERN);

            Pattern pattern = Pattern.compile(packages);
            Matcher matcher = pattern.matcher(packageName);

            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
