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
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the MANIFEST.MF file has any version constraints on imported and exported packages.
 *
 *
 * @author Petar Valchev - Initial contribution.
 * @author Aleksandar Kovachev - Refactored the code and added check for exported packages.
 * @author Svlien Valkanov - Renamed the check, bug fixtures
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
    protected void processFiltered(File manifestFile, FileText fileText) throws CheckstyleException {
        BundleInfo manifest = parseManifestFromFile(fileText);

        checkVersionOfImportedPackages(manifest, fileText);

        checkVersionOfExportedPackages(manifest, fileText);
    }

    private void checkVersionOfImportedPackages(BundleInfo manifest, FileText fileText) {
        Set<BundleRequirement> requiredBundles = manifest.getRequires();
        Set<BundleRequirement> importPackages = manifest.getImports();

        int lineNumber = 0;
        for (BundleRequirement importPackage : importPackages) {
            String importName = importPackage.getName();
            if (importPackage.getVersion() != null && !isIgnoredPackage(ignoreImportedPackages, importName)) {
                lineNumber = findLineNumberSafe(fileText, importName, lineNumber,
                        "Imported package line number not found.");
                log(lineNumber, String.format(VERSION_USED_MSG, importName));

            }
        }

        // Start again from the beginning of the file
        lineNumber = 0;
        for (BundleRequirement requiredBundle : requiredBundles) {
            if (requiredBundle.getVersion() != null) {
                String name = requiredBundle.getName();
                lineNumber = findLineNumberSafe(fileText, name, lineNumber, "Required bundle line number not found.");
                log(lineNumber, String.format(VERSION_USED_MSG, name));
            }
        }
    }

    private void checkVersionOfExportedPackages(BundleInfo manifest, FileText fileText) {
        Set<ExportPackage> exports = manifest.getExports();

        int lineNumber = 0;
        for (ExportPackage exportPackage : exports) {
            String exportedPackageName = exportPackage.getName();

            // If the package version is not with the default version of the exported packages by the ManifestParser
            // and the package is not ignored from the configuration.
            if (!exportPackage.getVersion().equals(BundleInfo.DEFAULT_VERSION)
                    && !isIgnoredPackage(ignoreExportedPackages, exportedPackageName)) {
                lineNumber = findLineNumberSafe(fileText, exportedPackageName, lineNumber,
                        "Exported package line number not found.");
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
