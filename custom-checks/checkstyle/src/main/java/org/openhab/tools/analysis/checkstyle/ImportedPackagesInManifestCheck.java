/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ivy.osgi.core.BundleInfo;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * A check that verifies that the provided packages are added to the MANIFEST.MF
 * if they are used in the java classes.
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public class ImportedPackagesInManifestCheck extends AbstractStaticCheck {
    private static final String WARNING_MESSAGE_TEMPLATE = "The package %s needs to be added to the imported packages in the MANIFEST.MF file";
    private static final String NOT_REQUIRED_PACKAGE_MESSAGE = "The package %s should not be imported in the MANIFEST.MF";
    private static final String IMPORT = "import";
    private static final String STATIC_IMPORT = "import static";

    private Set<String> importsFromJava;
    private Collection<String> importsInManifest;
    private String fileName;
    private String packageName;
    private Collection<String> ignoredPackages;
    private Collection<String> notRequiredPackages;

    public ImportedPackagesInManifestCheck() {
        setFileExtensions(JAVA_EXTENSION, MANIFEST_EXTENSION);
        importsFromJava = new HashSet<>();
        importsInManifest = new ArrayList<>();
    }

    /**
     * @param ignoredPackages
     *            - packages that should be ignored by the check
     */
    public void setIgnoredPackages(String[] ignoredPackages) {
        this.ignoredPackages = Arrays.asList(ignoredPackages);
    }

    /**
     * @param notRequiredPackages
     *            - packages that should not be imported in the MANIFEST.MF file,
     *            for example packages that are not required at runtime
     */
    public void setNotRequiredPackages(String[] notRequiredPackages) {
        this.notRequiredPackages = Arrays.asList(notRequiredPackages);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (FilenameUtils.getExtension(file.getName()).equals(JAVA_EXTENSION)) {
            collectImportedPackagesFromJavaFile(fileText);
        } else {
            processManifestFile(fileText, file);
        }
    }

    @Override
    public void finishProcessing() {
        importsFromJava.removeIf(x -> x.contains(packageName));
        importsFromJava.removeIf(javaImport -> importsInManifest.stream().anyMatch(javaImport::contains));
        importsFromJava.forEach(x -> logMessage(fileName, 0, x, String.format(WARNING_MESSAGE_TEMPLATE, x)));
        super.finishProcessing();
    }

    private void collectImportedPackagesFromJavaFile(FileText fileText) {
        Collection<String> imports = collectFileImports(fileText.toLinesArray());

        Collection<String> normalImports = imports.stream().filter(x -> !x.startsWith(STATIC_IMPORT))
                .map(x -> x.substring(x.lastIndexOf(" ") + 1, x.lastIndexOf("."))).collect(Collectors.toList());

        Collection<String> staticImports = imports.stream().filter(x -> x.startsWith(STATIC_IMPORT))
                .map(x -> x.substring(x.lastIndexOf(" ") + 1, StringUtils.lastOrdinalIndexOf(x, ".", 2)))
                .collect(Collectors.toList());

        importsFromJava.addAll(normalImports);
        importsFromJava.addAll(staticImports);
    }

    private Collection<String> collectFileImports(String[] lines) {
        Collection<String> imports = new ArrayList<>();
        boolean start = false;
        for (String line : lines) {
            if (start) {
                if (!line.isEmpty() && !line.startsWith(IMPORT)) {
                    break;
                }

                if (line.startsWith(IMPORT)) {
                    imports.add(line);
                }
            } else {
                if (line.startsWith(IMPORT)) {
                    start = true;
                    imports.add(line);
                }
            }
        }

        return imports;
    }

    private void processManifestFile(FileText fileText, File file) throws CheckstyleException {
        BundleInfo info = parseManifestFromFile(fileText);
        fileName = file.getAbsolutePath();
        packageName = info.getSymbolicName();

        importsInManifest = info.getImports().stream().map(x -> x.getName()).collect(Collectors.toSet());
        this.notRequiredPackages.stream().filter(x -> this.importsInManifest.contains(x))
                .forEach(x -> log(0, String.format(NOT_REQUIRED_PACKAGE_MESSAGE, x)));
        importsInManifest.addAll(this.ignoredPackages);
    }
}
