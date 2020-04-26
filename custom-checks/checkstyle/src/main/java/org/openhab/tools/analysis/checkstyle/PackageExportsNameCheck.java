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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.ivy.osgi.core.BundleInfo;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if all packages that are not exported are marked as internal.
 *
 * @author Aleksandar Kovachev - Initial contribution
 * @author Petar Valchev - Added a verification of non existent packages
 * @author Svilen Valkanov - Refactored the check
 */
public class PackageExportsNameCheck extends AbstractStaticCheck {
    private static final String CORRECT_NAMING_OF_NOT_EXPORTED_PACKAGES_MESSAGE = "The package %s"
            + " should be marked as \"internal\" if it is not exported.";

    private static final String PACKAGE_PATTERN = "[a-zA-Z0-9\\._-]*";

    private final Logger logger = LoggerFactory.getLogger(PackageExportsNameCheck.class);

    private String[] sourceDirectories;
    private String[] excludedPackages;

    /**
     * Sets the configuration property for source directories.
     *
     * @param sourceDirectories source directories
     */
    public void setSourceDirectories(String[] sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
    }

    /**
     * Sets the configuration property for excluding packages.
     *
     * @param excludePackages excluded packages
     */
    public void setExcludedPackages(String[] excludePackages) {
        this.excludedPackages = excludePackages;
    }

    public PackageExportsNameCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File manifestFile, FileText fileText) throws CheckstyleException {
        BundleInfo bundleInfo = parseManifestFromFile(fileText);
        Set<String> uniqueManifestExports = bundleInfo.getExports().stream().map(export -> export.getName())
                .collect(Collectors.toSet());

        File projectDirectory = manifestFile.getParentFile().getParentFile();
        Path projectDirectoryPath = projectDirectory.toPath();

        Set<String> sourcePackages = new HashSet<>();
        try {
            for (String sourcePath : sourceDirectories) {
                Path relativeSourcePath = Paths.get(sourcePath);
                Path sourceDirectoryPath = projectDirectoryPath.resolve(relativeSourcePath);
                sourcePackages.addAll(getFilteredPackagesFromSourceDirectory(sourceDirectoryPath));
            }

            sourcePackages.removeAll(uniqueManifestExports);
            for (String packageName : sourcePackages) {
                log(0, String.format(CORRECT_NAMING_OF_NOT_EXPORTED_PACKAGES_MESSAGE, packageName));
            }
        } catch (IOException e) {
            logger.error("Problem occurred while processing directories. "
                    + "The check will exit without logging any warnings!", e);
        }
    }

    /**
     * Filter and return the packages from the source directory. Only not excluded packages will be returned.
     *
     * @param sourcePath The full path of the source directory
     * @return {@link Set } of {@link String }s with the package names.
     * @throws IOException if an I/O error is thrown while visiting files
     */
    private Set<String> getFilteredPackagesFromSourceDirectory(Path sourcePath) throws IOException {
        Set<String> packages = new HashSet<>();
        // No symbolic links are expected in the source directory
        if (Files.exists(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    Path packageRelativePath = sourcePath.relativize(path.getParent());
                    String packageName = packageRelativePath.toString()
                            .replaceAll(Matcher.quoteReplacement(File.separator), ".");

                    if (!isExcluded(packageName)) {
                        packages.add(packageName);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return packages;
    }

    /**
     * Checks if the package should be excluded
     *
     * @param packageName The package name to be checked.
     * @return <b>true</b> if the package name should be excluded
     */
    private boolean isExcluded(String packageName) {
        for (String excludePackageName : excludedPackages) {
            excludePackageName = excludePackageName.replaceAll("\\.\\*", PACKAGE_PATTERN);

            Pattern pattern = Pattern.compile(excludePackageName);
            Matcher matcher = pattern.matcher(packageName);

            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
