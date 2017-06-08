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

import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Executes the
 * <a href="https://maven.apache.org/components/plugins/maven-checkstyle-plugin/">maven-checkstyle-
 * plugin</a> with a predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov
 *
 */
@Mojo(name = "checkstyle", requiresDependencyResolution = ResolutionScope.COMPILE)
public class CheckstyleChecker extends AbstractChecker {

    /**
     * Relative path of the XML configuration to use. If not set the default ruleset file will be used -
     * {@link #DEFAULT_RULE_SET_XML}
     */
    @Parameter(property = "checkstyle.ruleset")
    protected String checkstyleRuleset;

    /**
     * Relative path of the suppressions XML file to use. If not set the default filter file will be used
     * - {@link #DEFAULT_FILTER_XML}
     */
    @Parameter(property = "checkstyle.filter")
    protected String checkstyleFilter;

    /**
     * The version of the maven-checkstyle-plugin that will be used
     */
    @Parameter(property = "maven.checkstyle.version", defaultValue = "2.17")
    private String checkstyleMavenVersion;

    /**
     * A list with artifacts that contain additional checks for Checkstyle
     */
    @Parameter
    private List<Dependency> checkstylePlugins;

    /**
     * Relative path of the properties file to use in the ruleset to configure specific checks
     */
    @Parameter(property = "checkstyle.ruleset.properties")
    private String checkstyleProperties;

    /**
     * Location of the properties file that contains configuration options for the
     * maven-checkstyle-plugin
     */
    private static final String CHECKSTYLE_PROPERTIES_FILE = "configuration/checkstyle.properties";

    // information about the maven-checkstyle-plugin
    private static final String MAVEN_CHECKSTYLE_PLUGIN_GOAL = "checkstyle";
    private static final String MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID = "maven-checkstyle-plugin";
    private static final String MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

    // Default configuration file
    private static final String DEFAULT_RULE_SET_XML = "rulesets/checkstyle/rules.xml";
    private static final String DEFAULT_FILTER_XML = "rulesets/checkstyle/suppressions.xml";

    /**
     * This is a property in the maven-checkstyle-plugin that is used to describe the location of the
     * ruleset file used from the plugin.
     */
    private static final String CHECKSTYLE_RULE_SET_PROPERTY = "checkstyle.config.location";

    /**
     * This is a property in the maven-checkstyle-plugin that is used to describe the location of the
     * suppressions file used from the plugin.
     */
    private static final String CHECKSTYLE_SUPPRESSION_PROPERTY = "checkstyle.suppressions.location";

    private static final String CHECKSTYLE_RULE_SET_PROPERTIES_PROPERTY = "checkstyle.properties.location";

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        ClassLoader classLoader = getMavenRuntimeClasspathClassLoader();
        Properties userProps = loadPropertiesFromFile(classLoader, CHECKSTYLE_PROPERTIES_FILE);

        String ruleset = getLocation(checkstyleRuleset, DEFAULT_RULE_SET_XML);
        log.debug("Ruleset location is " + ruleset);
        userProps.setProperty(CHECKSTYLE_RULE_SET_PROPERTY, ruleset);

        String supression = getLocation(checkstyleFilter, DEFAULT_FILTER_XML);
        log.debug("Filter location is " + supression);
        userProps.setProperty(CHECKSTYLE_SUPPRESSION_PROPERTY, supression);

        if (checkstyleProperties != null) {
            String rulesetProperties = getLocation(checkstyleProperties, "");
            log.debug("Ruleset properties location is " + rulesetProperties);
            userProps.setProperty(CHECKSTYLE_RULE_SET_PROPERTIES_PROPERTY, rulesetProperties);
        }

        // Maven may load an older version, if no version is specified
        Dependency checkstyle = dependency("com.puppycrawl.tools", "checkstyle", "7.7");
        Dependency[] allDependencies = getDependencies(checkstyle, checkstylePlugins);

        Xpp3Dom config = configuration(element("sourceDirectory", mavenProject.getBasedir().toString()));

        executeCheck(MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID, MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID, checkstyleMavenVersion,
                MAVEN_CHECKSTYLE_PLUGIN_GOAL, config, allDependencies);

        log.debug("Checkstyle execution has been finished.");

    }

}
