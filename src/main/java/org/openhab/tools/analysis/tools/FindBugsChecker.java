/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.openhab.tools.analysis.tools.internal.FindBugsVisitors;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * <p>
 * Executes the
 * <a href="https://mvnrepository.com/artifact/com.github.hazendaz.spotbugs/spotbugs-maven-plugin">spotbugs-maven-plugin
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
 * @author Svilen Valkanov
 *
 */

@Mojo(name = "findbugs", requiresDependencyResolution = ResolutionScope.COMPILE)
public class FindBugsChecker extends AbstractChecker {

    /**
     * Relative path to the XML that specifies the bug detectors which should be run. If not set the
     * {@link #DEFAULT_VISITORS_XML} will be used
     */
    @Parameter(property = "findbugs.ruleset")
    private String findbugsRuleset;

    /**
     * Relative path of the XML that specifies the bug instances that will be included from the report. If not set the
     * {@link #DEFAULT_INCLUDE_FILTER_XML} will be used
     */
    @Parameter(property = "findbugs.include")
    private String findbugsInclude;

    /**
     ** Relative path of the XML that specifies the bug instances that will be excluded in the report. If not set the
     * {@link #DEFAULT_EXCLUDE_FILTER_XML} will be used
     */
    @Parameter(property = "findbugs.exclude")
    private String findbugsExclude;

    /**
     * The version of the spotbugs-maven-plugin that will be used
     */
    @Parameter(property = "maven.spotbugs.version", defaultValue = "3.0.6")
    private String spotBugsMavenPluginVersion;

    /**
     * The version of the findbugs-slf4j plugin that will be used
     */
    @Parameter(property = "findbugs.slf4j.version", defaultValue = "1.2.4")
    private String findBugsSlf4jPluginVersion;

    /**
     * The version of the spotbugs that will be used
     */
    @Parameter(property = "spotbugs.version", defaultValue = "3.1.0-RC3")
    private String spotBugsVersion;

    /**
     * A list with artifacts that contain additional checks for FindBugs
     */
    @Parameter
    private List<Dependency> findbugsPlugins = new ArrayList<>();

    /**
     * Location of the properties file that contains configuration options for the
     * findbugs-maven-plugin
     */
    private static final String FINDBUGS_PROPERTIES_FILE = "configuration/findbugs.properties";

    private static final String DEFAULT_EXCLUDE_FILTER_XML = "rulesets/findbugs/exclude.xml";
    private static final String DEFAULT_INCLUDE_FILTER_XML = "rulesets/findbugs/include.xml";
    private static final String DEFAULT_VISITORS_XML = "rulesets/findbugs/visitors.xml";

    private static final String SPOTBUGS_MAVEN_PLUGIN_GOAL = "findbugs";
    private static final String SPOTBUGS_MAVEN_PLUGIN_ARTIFACT_ID = "spotbugs-maven-plugin";
    private static final String SPOTBUGS_MAVEN_PLUGIN_GROUP_ID = "com.github.hazendaz.spotbugs";

    /**
     * Property in the findbugs-maven-plugin that is used to describe the path to the
     * include filter file used from the plugin.
     */
    private static final String FINDBUGS_INCLUDE_FILTER_USER_PROPERTY = "findbugs.includeFilterFile";

    /**
     * Property in the findbugs-maven-plugin that is used to describe the path to the
     * exclude filter file used from the plugin.
     */
    private static final String FINDBUGS_EXCLUDE_FILTER_USER_PROPERTY = "findbugs.excludeFilterFile";

    /**
     * Property in the findbugs-maven-plugin, that specifies a comma-separated list of bug detectors which should be
     * run. The bug detectors are specified by their class names, without any package qualification.
     */
    private static final String FINDBUGS_VISITORS_PROPERTY = "findbugs.visitors";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        Properties userProps = loadPropertiesFromFile(FINDBUGS_PROPERTIES_FILE);

        // Load the include filter file
        String includeLocation = getLocation(findbugsInclude, DEFAULT_INCLUDE_FILTER_XML);
        log.debug("Ruleset location is " + includeLocation);
        userProps.setProperty(FINDBUGS_INCLUDE_FILTER_USER_PROPERTY, includeLocation);

        // Load the exclude filter file
        String excludeLocation = getLocation(findbugsExclude, DEFAULT_EXCLUDE_FILTER_XML);
        log.debug("Filter location is " + excludeLocation);
        userProps.setProperty(FINDBUGS_EXCLUDE_FILTER_USER_PROPERTY, excludeLocation);

        String visitors = getVisitorsString(findbugsRuleset, DEFAULT_VISITORS_XML);
        log.debug("FindBugs visitors " + visitors);
        userProps.setProperty(FINDBUGS_VISITORS_PROPERTY, visitors);
        String outputDir = userProps.getProperty("findbugs.report.dir");

        // The tool itself is a FindBugs plugin
        findbugsPlugins.add(dependency(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion()));
        // Add dependency to the findbugs-slf4j plugin
        findbugsPlugins.add(dependency("jp.skypencil.findbugs.slf4j", "bug-pattern", findBugsSlf4jPluginVersion));
        findbugsPlugins.forEach(logDependency());

        // These configuration properties are not exposed from the findbugs-maven-plugin as user
        // properties, so they have to be set direct in the configuration
        Xpp3Dom config = configuration(element("outputDirectory", outputDir), element("xmlOutputDirectory", outputDir),
                element("findbugsXmlOutputDirectory", outputDir), getFindBugsPlugins(findbugsPlugins));

        // If this dependency is missing, findbugs can not load the core plugin because of classpath
        // issues
        List<Dependency> findbugsDeps = new ArrayList<>();
        findbugsDeps.add(dependency("com.github.spotbugs", "spotbugs", spotBugsVersion));
        findbugsDeps.forEach(logDependency());

        executeCheck(SPOTBUGS_MAVEN_PLUGIN_GROUP_ID, SPOTBUGS_MAVEN_PLUGIN_ARTIFACT_ID, spotBugsMavenPluginVersion,
                SPOTBUGS_MAVEN_PLUGIN_GOAL, config, findbugsDeps);

        log.debug("FindBugs execution has been finished.");
    }

    /**
     * Creates a "plugins" element used in the findbugs-maven-plugin configuration
     */
    private Element getFindBugsPlugins(List<Dependency> plugins) {
        List<Element> pluginList = new LinkedList<>();
        // Add additional dependencies
        if (plugins != null) {
            for (Dependency artifact : plugins) {
                Element element = createPlugin(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                pluginList.add(element);
            }
        }
        return new Element("plugins", pluginList.toArray(new Element[pluginList.size()]));
    }

    private Element createPlugin(String groudId, String artifactId, String version) {
        return element("plugin", element("groupId", groudId), element("artifactId", artifactId),
                element("version", version));
    }

    private String getVisitorsString(String externalLocation, String defaultLocaiton) throws MojoExecutionException {
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
                getLog().warn("Unable to find file " + resolvedPath.toString());
            }
        } else {
            stream = this.getClass().getClassLoader().getResourceAsStream(defaultLocaiton);

        }

        // Serialize the content
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(FindBugsVisitors.class);
            Unmarshaller unmarschaller = context.createUnmarshaller();

            FindBugsVisitors visitors = (FindBugsVisitors) unmarschaller.unmarshal(stream);
            return visitors.toString();
        } catch (JAXBException e) {
            getLog().warn("Unable to load FindBugs visitors", e);
            return null;
        }
    }
}
