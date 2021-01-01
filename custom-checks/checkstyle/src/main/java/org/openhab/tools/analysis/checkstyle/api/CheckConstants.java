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
package org.openhab.tools.analysis.checkstyle.api;

import java.nio.file.Path;

/**
 * Contains constants used in the implementation of checks and tests for them
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class CheckConstants {
    // File extensions
    public static final String PROPERTIES_EXTENSION = "properties";
    public static final String XML_EXTENSION = "xml";
    public static final String HTML_EXTENSION = "html";
    public static final String MANIFEST_EXTENSION = "MF";
    public static final String CLASSPATH_EXTENSION = "classpath";
    public static final String MARKDONW_EXTENSION = "md";
    public static final String JAVA_EXTENSION = "java";

    // File names
    public static final String BUILD_PROPERTIES_FILE_NAME = "build.properties";
    public static final String MANIFEST_FILE_NAME = "MANIFEST.MF";
    public static final String NOTICE_FILE_NAME = "NOTICE";
    public static final String POM_XML_FILE_NAME = "pom.xml";
    public static final String CLASSPATH_FILE_NAME = ".classpath";
    public static final String README_MD_FILE_NAME = "README.md";

    // Directory names
    public static final String OSGI_INF_DIRECTORY_NAME = "OSGI-INF";
    public static final String META_INF_DIRECTORY_NAME = "META-INF";
    public static final String OH_INF_DIRECTORY = "OH-INF";
    public static final String OH_INF_PATH = Path.of("src", "main", "resources", OH_INF_DIRECTORY).toString();

    // Properties
    public static final String BIN_INCLUDES_PROPERTY_NAME = "bin.includes";
    public static final String OUTPUT_PROPERTY_NAME = "output..";
    public static final String SOURCE_PROPERTY_NAME = "source..";

    // OSGi MANIFEST.MF properties
    public static final String REQUIRE_BUNDLE_HEADER_NAME = "Require-Bundle";
    public static final String FRAGMENT_HOST_HEADER_NAME = "Fragment-Host";
    public static final String BUNDLE_SYMBOLIC_NAME_HEADER_NAME = "Bundle-SymbolicName";
    public static final String EXPORT_PACKAGE_HEADER_NAME = "Export-Package";
    public static final String SERVICE_COMPONENT_HEADER_NAME = "Service-Component";
}
