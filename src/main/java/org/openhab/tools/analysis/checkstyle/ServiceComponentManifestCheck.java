/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Check if all the declarative services are included in the MANIFEST.MF.
 *
 * @author Aleksandar Kovachev - Initial contribution
 * @author Petar Valchev - Changed the verification of the Service-Component
 *         header
 * @author Dimitar Ivanov - Common wildcard adaptions
 * @author Svilen Valkanov - Check the build.properties file
 */
public class ServiceComponentManifestCheck extends AbstractStaticCheck {
    private static final String WILDCARD = "*";
    private static final String SERVICE_COMPONENT_HEADER = "Service-Component";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> manifestServiceComponents = new ArrayList<>();
    private List<String> componentXmlFiles = new ArrayList<>();

    private boolean loggedBestApproachMessage = false;

    private String serviceComponentHeaderValue;
    private int serviceComponentHeaderLineNumber;
    private String manifestPath;
    private String buildPropertiesPath;

    /**
     * Paths relative to the bundle base directory
     **/
    private List<Path> componentXmlRelativePaths = new ArrayList<>();
    private IBuild buildPropertiesFile;

    public ServiceComponentManifestCheck() {
        logger.debug("Executing {}: Check if all the declarative services are included in the {}",
                this.getClass().getName(), MANIFEST_FILE_NAME);
        setFileExtensions(MANIFEST_EXTENSION, XML_EXTENSION, PROPERTIES_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {

        Path absolutePath = file.toPath();
        int osgiInfIndex = getIndex(absolutePath, OSGI_INF_DIRECTORY_NAME);
        String fileExtension = FilenameUtils.getExtension(file.getName());

        // The components are .xml files located in OSGI-INF folder
        if (fileExtension.equals(XML_EXTENSION) && osgiInfIndex > -1) {
            // All the defined components are collected to be processed later
            componentXmlFiles.add(file.getName());

            // Get the relative path
            Path relativePath = absolutePath.subpath(osgiInfIndex, absolutePath.getNameCount());
            componentXmlRelativePaths.add(relativePath);
        }

        if (file.getName().equals(MANIFEST_FILE_NAME)) {
            verifyManifest(file, lines);
        }

        if (file.getName().equals(BUILD_PROPERTIES_FILE_NAME)) {
            processBuildPropertiesFile(file);
        }
    }

    private int getIndex(Path path, String dirName) {
        int result = -1;
        for (int i = 0; i < path.getNameCount(); i++) {
            if (path.getName(i).toString().equals(dirName)) {
                result = i;
                break;
            }
        }
        return result;
    }

    private void processBuildPropertiesFile(File file) {
        try {
            buildPropertiesFile = parseBuildProperties(file);
            buildPropertiesPath = file.getPath();
        } catch (CheckstyleException e) {
            logger.error("Problem occurred while parsing the file {}", file.getPath(), e);
        }
    }

    @Override
    public void finishProcessing() {
        verifyManifestWildcardDeclaredServiceComponents();
        verifyManifestExplicitlyDeclaredServices();
        verifyBuildPropertiesFile();
    }

    private void verifyBuildPropertiesFile() {
        if (buildPropertiesPath != null) {
            IBuildEntry binIncludes = buildPropertiesFile.getEntry(IBuildEntry.BIN_INCLUDES);
            if (binIncludes != null) {
                String[] includedTokens = binIncludes.getTokens();
                // Exclude the component files that are added to the bin.includes property
                for (String included : includedTokens) {
                    for (Iterator<Path> iterator = componentXmlRelativePaths.iterator(); iterator.hasNext();) {
                        Path componentXmlFile = iterator.next();
                        if (componentXmlFile.startsWith(included)) {
                            iterator.remove();
                        }
                    }
                }
            }

            for (Path path : componentXmlRelativePaths) {
                logMessage(buildPropertiesPath, 0, BUILD_PROPERTIES_FILE_NAME,
                        MessageFormat.format(
                                "The service component {0} isn`t included in the build.properties file."
                                        + " Good approach is to include all files by adding `OSGI-INF/` value to the bin.includes property.",
                                path));
            }
        }
    }

    private void verifyManifestWildcardDeclaredServiceComponents() {
        // We use iterator, because we will modify the list while iterating
        // through it
        Iterator<String> manifestServiceComponentsIterator = manifestServiceComponents.iterator();

        while (manifestServiceComponentsIterator.hasNext()) {
            String manifestServiceComponent = manifestServiceComponentsIterator.next();

            if (manifestServiceComponent.equals(WILDCARD)) {
                logBestApproachMessage();

                // The service component is declared as OSGI-INF/* and all the services are included. There is
                // no need of further comparison of the two lists
                manifestServiceComponents.clear();
                componentXmlFiles.clear();
                break;
            }

            // Now check all the .xml service component definitions
            String manifestServiceComponentName = StringUtils.substringBefore(manifestServiceComponent,
                    "." + XML_EXTENSION);

            if (manifestServiceComponentName.contains(WILDCARD)) {
                // *.xml is used in the service component declaration
                if (manifestServiceComponentName.equals(WILDCARD)) {
                    if (manifestServiceComponents.size() > 1) {
                        // if there is any explicit service declaration in
                        // addition to *.xml
                        logMessage(serviceComponentHeaderLineNumber,
                                "If you are using OSGI-INF/*.xml, do not include any of the services explicitly. "
                                        + "Otherwise they will be included more than once.");
                    }

                    // The service component is declared as *.xml and all the services are included. There is no need of
                    // further comparison of the two lists
                    manifestServiceComponents.clear();
                    componentXmlFiles.clear();
                    break;
                } else {
                    // Wildcard other than *.xml is used
                    logBestApproachMessage();
                    Pattern pattern = Pattern.compile(manifestServiceComponentName);
                    boolean matchedPattern = false;

                    // we use iterator, because we will modify the list while
                    // iterating through it
                    Iterator<String> componentXmlFilesIterator = componentXmlFiles.iterator();
                    while (componentXmlFilesIterator.hasNext()) {
                        String componentXml = componentXmlFilesIterator.next();
                        Matcher matcher = pattern.matcher(componentXml);
                        if (matcher.find()) {
                            // if any of the services matches the manifest
                            // service component regex,
                            // remove them from the list, so that we can verify
                            // only the service components,
                            // that are declared with their full name later
                            componentXmlFilesIterator.remove();
                            matchedPattern = true;
                        }
                    }

                    if (!matchedPattern) {
                        logMessage(serviceComponentHeaderLineNumber,
                                String.format("The service component %s does not match any of the exisitng services.",
                                        manifestServiceComponent));
                    }

                    // remove the regex service component definition,
                    // so that we can verify only the service components,
                    // that are declared with their full name later
                    manifestServiceComponentsIterator.remove();
                }
            } else {
                // if no wildcard is used and the service is declared explicitly
                logBestApproachMessage();
            }
        }
    }

    private void verifyManifestExplicitlyDeclaredServices() {
        // list in which we will store all the common elements of
        // manifestServiceComponents and componentXmlFiles
        List<String> intersection = new ArrayList<>(manifestServiceComponents);
        intersection.retainAll(componentXmlFiles);

        // log a message for every not included service in the manifest
        componentXmlFiles.removeAll(intersection);
        for (String service : componentXmlFiles) {
            if (serviceComponentHeaderLineNumber == -1) {
                // if there is no Service-Component header
                logMessage(0, String.format("The service %s is not included in the MANIFEST.MF file. "
                        + "Are you sure that there is no need to be included?", service));
            } else {
                logMessage(serviceComponentHeaderLineNumber,
                        String.format("The service %s is not included in the MANIFEST.MF file. "
                                + "Are you sure that there is no need to be included?", service));
            }
        }

        // log a message for every service component definition,
        // that does not have a corresponding service
        manifestServiceComponents.removeAll(intersection);
        for (String service : manifestServiceComponents) {
            logMessage(serviceComponentHeaderLineNumber,
                    String.format("The service %s does not exist in the OSGI-INF folder.", service));
        }
    }

    private void verifyManifest(File file, List<String> lines) {
        manifestPath = file.getPath();
        try {
            Manifest manifest = new Manifest(new FileInputStream(file));
            Attributes attributes = manifest.getMainAttributes();

            serviceComponentHeaderValue = attributes.getValue(SERVICE_COMPONENT_HEADER);
            serviceComponentHeaderLineNumber = findLineNumber(lines, SERVICE_COMPONENT_HEADER, 0);

            if (serviceComponentHeaderValue != null) {
                List<String> serviceComponentsList = Arrays.asList(serviceComponentHeaderValue.trim().split(","));
                for (String serviceComponent : serviceComponentsList) {
                    // We assume that the defined service component refers to existing file
                    File serviceComponentFile = new File(serviceComponent);
                    String serviceComponentParentDirectoryName = serviceComponentFile.getParentFile().getName();

                    if (!serviceComponentParentDirectoryName.equals(OSGI_INF_DIRECTORY_NAME)) {
                        // if the parent directory of the service is not
                        // OSGI-INF
                        logMessage(serviceComponentHeaderLineNumber,
                                String.format(
                                        "Incorrect directory for services - %s. "
                                                + "The best practice is services metadata files to be placed directly in OSGI-INF directory.",
                                        serviceComponentParentDirectoryName));
                    }

                    String serviceComponentName = serviceComponentFile.getName();

                    // We will process either .xml or OSGi-INF/* service components
                    if (serviceComponentName.endsWith(XML_EXTENSION) || serviceComponentName.endsWith(WILDCARD)) {
                        manifestServiceComponents.add(serviceComponentName);
                    } else {
                        logMessage(serviceComponentHeaderLineNumber,
                                String.format(
                                        "The service %s is with invalid extension."
                                                + "Only XML metadata files for services description are expected in the OSGI-INF directory.",
                                        serviceComponentName));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Problem occurred while parsing the file {}", file.getPath(), e);
        }
    }

    private void logBestApproachMessage() {
        if (!loggedBestApproachMessage) {
            logMessage(serviceComponentHeaderLineNumber, "A good approach is to use OSGI-INF/*.xml "
                    + "instead of including the services metadata files separately or using common wildcard.");
            loggedBestApproachMessage = true;
        }
    }

    private void logMessage(int line, String message) {
        logMessage(manifestPath, line, MANIFEST_FILE_NAME, message);
    }
}
