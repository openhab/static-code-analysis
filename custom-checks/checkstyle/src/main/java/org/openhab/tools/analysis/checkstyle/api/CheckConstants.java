/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.api;

/**
 * Contains constants used in the implementation of checks and tests for them
 *
 * @author Svilen Valkanov
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
    public static final String ESH_INF_DIRECTORY = "ESH-INF";

    // Properties
    public static final String BIN_INCLUDES_PROPERTY_NAME = "bin.includes";
    public static final String OUTPUT_PROPERTY_NAME = "output..";
    public static final String SOURCE_PROPERTY_NAME = "source..";

    // OSGi MANIFEST.MF properties
    public final static String REQUIRE_BUNDLE_HEADER_NAME = "Require-Bundle";
    public final static String FRAGMENT_HOST_HEADER_NAME = "Fragment-Host";
    public final static String BUNDLE_SYMBOLIC_NAME_HEADER_NAME = "Bundle-SymbolicName";
    public final static String EXPORT_PACKAGE_HEADER_NAME = "Export-Package";
    public final static String SERVICE_COMPONENT_HEADER_NAME = "Service-Component";

}
