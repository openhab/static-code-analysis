/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.tools.analysis.tools;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.openhab.tools.analysis.tools.internal.SpotBugsVisitors;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * <p>
 * Executes the
 * <a href="https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs-maven-plugin">spotbugs-maven-plugin
 * </a> which is a fork of the
 * <a href="http://gleclaire.github.io/findbugs-maven-plugin/index.html">findbugs-maven-plugin</a>
 * with a predefined ruleset file and configuration properties
 * </p>
 *
 * <p>
 * The checker uses <a href="https://github.com/spotbugs/spotbugs">SpotBugs</a>
 * which is the successor of <a href="https://github.com/findbugsproject/findbugs">FindBugs</a>.
 * SpotBugs is fully backward compatible with FindBugs.
 * </p>
 *
 * @author Svilen Valkanov - Initial contribution
 */
@Mojo(name = "spotbugs", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class SpotBugsChecker extends AbstractChecker {

    /**
     * Relative path to the XML that specifies the bug detectors which should be run. If not set the
     * {@link #DEFAULT_VISITORS_XML} will be used
     */
    @Parameter(property = "spotbugs.ruleset")
    private String spotbugsRuleset;

    /**
     * Relative path of the XML that specifies the bug instances that will be included from the report. If not set the
     * {@link #DEFAULT_INCLUDE_FILTER_XML} will be used
     */
    @Parameter(property = "spotbugs.include")
    private String spotbugsInclude;

    /**
     ** Relative path of the XML that specifies the bug instances that will be excluded in the report. If not set the
     * {@link #DEFAULT_EXCLUDE_FILTER_XML} will be used
     */
    @Parameter(property = "spotbugs.exclude")
    private String spotbugsExclude;

    /**
     * The version of the spotbugs-maven-plugin that will be used
     */
    @Parameter(property = "maven.spotbugs.version", defaultValue = "4.9.8.2")
    private String spotbugsMavenPluginVersion;

    /**
     * The version of the findbugs-slf4j plugin that will be used
     */
    @Parameter(property = "findbugs.slf4j.version", defaultValue = "1.5.0")
    private String findBugsSlf4jPluginVersion;

    /**
     * The version of the spotbugs that will be used
     */
    @Parameter(property = "spotbugs.version", defaultValue = "4.9.8")
    private String spotBugsVersion;

    /**
     * A list with artifacts that contain additional checks for SpotBugs
     */
    @Parameter
    private List<Dependency> spotbugsPlugins = new ArrayList<>();

    /**
     * Location of the properties file that contains configuration options for the
     * spotbugs-maven-plugin
     */
    private static final String SPOTBUGS_PROPERTIES_FILE = "configuration/spotbugs.properties";

    private static final String DEFAULT_EXCLUDE_FILTER_XML = "rulesets/spotbugs/exclude.xml";
    private static final String DEFAULT_INCLUDE_FILTER_XML = "rulesets/spotbugs/include.xml";
    private static final String DEFAULT_VISITORS_XML = "rulesets/spotbugs/visitors.xml";

    private static final String SPOTBUGS_PLUGIN_GROUP_ID = "com.github.spotbugs";
    private static final String SPOTBUGS_PLUGIN_ARTIFACT_ID = "spotbugs";
    private static final String SPOTBUGS_MAVEN_PLUGIN_ARTIFACT_ID = "spotbugs-maven-plugin";
    private static final String SPOTBUGS_MAVEN_PLUGIN_GOAL = "spotbugs";

    /**
     * Property in the spotbugs-maven-plugin that is used to describe the path to the
     * include filter file used from the plugin.
     */
    private static final String SPOTBUGS_INCLUDE_FILTER_USER_PROPERTY = "spotbugs.includeFilterFile";

    /**
     * Property in the spotbugs-maven-plugin that is used to describe the path to the
     * exclude filter file used from the plugin.
     */
    private static final String SPOTBUGS_EXCLUDE_FILTER_USER_PROPERTY = "spotbugs.excludeFilterFile";

    /**
     * Property in the spotbugs-maven-plugin, that specifies a comma-separated list of bug detectors which should be
     * run. The bug detectors are specified by their class names, without any package qualification.
     */
    private static final String SPOTBUGS_VISITORS_PROPERTY = "spotbugs.visitors";

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();

        Properties userProps = loadPropertiesFromFile(SPOTBUGS_PROPERTIES_FILE);

        // Load the include filter file
        String includeLocation = getLocation(spotbugsInclude, DEFAULT_INCLUDE_FILTER_XML);
        log.debug("Ruleset location is " + includeLocation);
        userProps.setProperty(SPOTBUGS_INCLUDE_FILTER_USER_PROPERTY, includeLocation);

        // Load the exclude filter file
        String excludeLocation = getLocation(spotbugsExclude, DEFAULT_EXCLUDE_FILTER_XML);
        log.debug("Filter location is " + excludeLocation);
        userProps.setProperty(SPOTBUGS_EXCLUDE_FILTER_USER_PROPERTY, excludeLocation);

        String visitors = getVisitorsString(spotbugsRuleset);
        log.debug("SpotBugs visitors " + visitors);
        userProps.setProperty(SPOTBUGS_VISITORS_PROPERTY, visitors);
        String outputDir = userProps.getProperty("spotbugs.report.dir");

        // The tool itself is a SpotBugs plugin
        spotbugsPlugins.add(dependency("org.openhab.tools.sat.custom-checks", "findbugs", plugin.getVersion()));
        // Add dependency to the findbugs-slf4j plugin
        spotbugsPlugins.add(dependency("jp.skypencil.findbugs.slf4j", "bug-pattern", findBugsSlf4jPluginVersion));
        spotbugsPlugins.forEach(logDependency());

        // These configuration properties are not exposed from the spotbugs-maven-plugin as user
        // properties, so they have to be set direct in the configuration
        Xpp3Dom config = configuration(element("outputDirectory", outputDir), element("xmlOutputDirectory", outputDir),
                element("spotbugsXmlOutputDirectory", outputDir), getSpotBugsPlugins(spotbugsPlugins));

        // If this dependency is missing, spotbugs can not load the core plugin because of classpath
        // issues
        List<Dependency> spotBugsDeps = new ArrayList<>();
        spotBugsDeps.add(dependency(SPOTBUGS_PLUGIN_GROUP_ID, SPOTBUGS_PLUGIN_ARTIFACT_ID, spotBugsVersion));
        spotBugsDeps.forEach(logDependency());

        executeCheck(SPOTBUGS_PLUGIN_GROUP_ID, SPOTBUGS_MAVEN_PLUGIN_ARTIFACT_ID, spotbugsMavenPluginVersion,
                SPOTBUGS_MAVEN_PLUGIN_GOAL, config, spotBugsDeps);

        log.debug("SpotBugs execution has been finished.");
    }

    /**
     * Creates a "plugins" element used in the spotbugs-maven-plugin configuration
     */
    private Element getSpotBugsPlugins(List<Dependency> plugins) {
        List<Element> pluginList = new LinkedList<>();
        // Add additional dependencies
        if (plugins != null) {
            for (Dependency artifact : plugins) {
                Element element = createPlugin(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                pluginList.add(element);
            }
        }
        return new Element("plugins", pluginList.toArray(new Element[0]));
    }

    private Element createPlugin(String groupId, String artifactId, String version) {
        return element("plugin", element("groupId", groupId), element("artifactId", artifactId),
                element("version", version));
    }

    private String getVisitorsString(String externalLocation) {
        // Get the XML file as a Stream
        InputStream stream = null;
        if (externalLocation != null) {
            Path executionDir = Paths.get(mavenSession.getExecutionRootDirectory());
            Path externalDir = Paths.get(externalLocation);
            Path resolvedPath = executionDir.resolve(externalDir);
            File file = resolvedPath.toFile();
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                getLog().warn("Unable to find file " + resolvedPath);
            }
        } else {
            stream = this.getClass().getClassLoader().getResourceAsStream(SpotBugsChecker.DEFAULT_VISITORS_XML);
        }

        // Serialize the content
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(SpotBugsVisitors.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            SpotBugsVisitors visitors = (SpotBugsVisitors) unmarshaller.unmarshal(stream);
            return visitors.toString();
        } catch (JAXBException e) {
            getLog().warn("Unable to load SpotBugs visitors", e);
            return null;
        }
    }
}
