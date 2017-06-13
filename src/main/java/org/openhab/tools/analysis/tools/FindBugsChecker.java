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
 * Executes the <a href="http://gleclaire.github.io/findbugs-maven-plugin/index.html">findbugs-maven-plugin</a>
 * with a predefined ruleset file and configuration properties
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
     * The version of the findbugs-maven-plugin that will be used
     */
    @Parameter(property = "maven.findbugs.version", defaultValue = "3.0.4")
    private String findBugsMavenPluginVersion;

    /**
     * The version of the findbugs-slf4j plugin that will be used
     */
    @Parameter(property = "findbugs.slf4j.version", defaultValue = "1.2.4")
    private String findBugsSlf4jPluginVersion;

    /**
     * The version of the findbugs that will be used
     */
    @Parameter(property = "findbugs.version", defaultValue = "3.0.1")
    private String findBugsVersion;

    /**
     * A list with artifacts that contain additional checks for FindBugs
     */
    @Parameter
    private List<Dependency> findbugsPlugins;

    /**
     * Location of the properties file that contains configuration options for the
     * findbugs-maven-plugin
     */
    private static final String FINDBUGS_PROPERTIES_FILE = "configuration/findbugs.properties";

    private static final String DEFAULT_EXCLUDE_FILTER_XML = "rulesets/findbugs/exclude.xml";
    private static final String DEFAULT_INCLUDE_FILTER_XML = "rulesets/findbugs/include.xml";
    private static final String DEFAULT_VISITORS_XML = "rulesets/findbugs/visitors.xml";

    private static final String FINDBUGS_MAVEN_PLUGIN_GOAL = "findbugs";
    private static final String FINDBUGS_MAVEN_PLUGIN_ARTIFACT_ID = "findbugs-maven-plugin";
    private static final String FINDBUGS_MAVEN_PLUGIN_GROUP_ID = "org.codehaus.mojo";

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

        ClassLoader cl = getMavenRuntimeClasspathClassLoader();
        Properties userProps = loadPropertiesFromFile(cl, FINDBUGS_PROPERTIES_FILE);

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

        // These configuration properties are not exposed from the findbugs-maven-plugin as user
        // properties, so they have to be set direct in the configuration
        Xpp3Dom config = configuration(element("outputDirectory", outputDir), element("xmlOutputDirectory", outputDir),
                element("findbugsXmlOutputDirectory", outputDir), getFindBugsPlugins());

        // If this dependency is missing, findbugs can not load the core plugin because of classpath
        // issues
        Dependency findBugsDep = dependency("com.google.code.findbugs", "findbugs", findBugsVersion);

        // Add dependency to the findbugs-slf4j plugin
        Dependency findBugsSlf4j = dependency("jp.skypencil.findbugs.slf4j", "bug-pattern", findBugsSlf4jPluginVersion);
        findbugsPlugins.add(findBugsSlf4j);

        Dependency[] allDependencies = getDependencies(findBugsDep, findbugsPlugins);

        executeCheck(FINDBUGS_MAVEN_PLUGIN_GROUP_ID, FINDBUGS_MAVEN_PLUGIN_ARTIFACT_ID, findBugsMavenPluginVersion,
                FINDBUGS_MAVEN_PLUGIN_GOAL, config, allDependencies);

        log.debug("FindBugs execution has been finished.");
    }

    /**
     * Creates a "plugins" element used in the findbugs-maven-plugin configuration
     */
    private Element getFindBugsPlugins() {
        List<Element> pluginList = new LinkedList<>();
        // Add static code analysis as plugin
        pluginList.add(createPlugin(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion()));

        // Add additional dependencies
        if (findbugsPlugins != null) {
            for (Dependency artifact : findbugsPlugins) {
                Element element = createPlugin(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                pluginList.add(element);
            }
        }
        Element plugins = new Element("plugins", pluginList.toArray(new Element[pluginList.size()]));
        return plugins;
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
            stream = getMavenRuntimeClasspathClassLoader().getResourceAsStream(defaultLocaiton);

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
