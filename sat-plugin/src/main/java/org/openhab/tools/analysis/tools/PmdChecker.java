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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Executes the
 * <a href="https://maven.apache.org/plugins/maven-pmd-plugin/index.html">maven-pmd-plugin</a> with
 * a predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov - Initial contribution
 */
@Mojo(name = "pmd", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class PmdChecker extends AbstractChecker {

    private static final String DEFAULT_RULESET_XML = "rulesets/pmd/rules.xml";

    private static final String CUSTOM_RULESET_XML = "rulesets/pmd/customrules.xml";

    private static final String DEFAULT_FILTER_XML = "rulesets/pmd/suppressions.properties";

    /**
     * Relative path of the XML configuration to use. If not set the default ruleset file will be used -
     * {@link #DEFAULT_RULESET_XML}
     */
    @Parameter(property = "pmd.ruleset")
    protected String pmdRuleset;

    /**
     * Relative path of the suppressions XML file to use. If not set the default filter file will be used
     * - {@link #DEFAULT_FILTER_XML}
     */
    @Parameter(property = "pmd.excludeFromFailureFile")
    protected String pmdFilter;

    /**
     * The version of the maven-pmd-plugin that will be used
     */
    @Parameter(property = "maven.pmd.version", defaultValue = "3.26.0")
    private String mavenPmdVersion;

    /**
     * A list with artifacts that contain additional checks for PMD
     */
    @Parameter
    private List<Dependency> pmdPlugins = new ArrayList<>();

    private static final String PMD_VERSION = "7.16.0";
    /**
     * Location of the properties files that contains configuration options for the maven-pmd-plugin
     */
    private static final String PMD_PROPERTIES_FILE = "configuration/pmd.properties";

    private static final String MAVEN_PMD_PLUGIN_ARTIFACT_ID = "maven-pmd-plugin";
    private static final String MAVEN_PMD_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_PMD_PLUGIN_GOAL = "pmd";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        Properties userProps = loadPropertiesFromFile(PMD_PROPERTIES_FILE);

        String excludeFromFailureLocation = getLocation(pmdFilter, DEFAULT_FILTER_XML);
        log.debug("Exclude filter file location is " + excludeFromFailureLocation);
        userProps.setProperty("pmd.excludeFromFailureFile", excludeFromFailureLocation);

        String defaultRulesetLocation = getLocation(pmdRuleset, DEFAULT_RULESET_XML);
        log.debug("Default ruleset location is " + defaultRulesetLocation);

        String customRulesetLocation = getLocation(pmdRuleset, CUSTOM_RULESET_XML);
        log.debug("Custom ruleset location is " + customRulesetLocation);

        // These configuration properties are not exposed from the maven-pmd-plugin as user properties,
        // so they have to be set direct in the configuration
        Xpp3Dom configuration = configuration(
                element("targetDirectory", userProps.getProperty("pmd.custom.targetDirectory")),
                element("compileSourceRoots", userProps.getProperty("pmd.custom.compileSourceRoots")),
                element("rulesets", element("ruleset", defaultRulesetLocation),
                        element("ruleset", customRulesetLocation)));
        pmdPlugins.add(dependency("org.openhab.tools.sat.custom-checks", "pmd", plugin.getVersion()));
        pmdPlugins.add(dependency("net.sourceforge.pmd", "pmd-core", PMD_VERSION));
        pmdPlugins.add(dependency("net.sourceforge.pmd", "pmd-java", PMD_VERSION));
        pmdPlugins.add(dependency("net.sourceforge.pmd", "pmd-javascript", PMD_VERSION));
        pmdPlugins.add(dependency("net.sourceforge.pmd", "pmd-jsp", PMD_VERSION));
        pmdPlugins.forEach(logDependency());

        executeCheck(MAVEN_PMD_PLUGIN_GROUP_ID, MAVEN_PMD_PLUGIN_ARTIFACT_ID, mavenPmdVersion, MAVEN_PMD_PLUGIN_GOAL,
                configuration, pmdPlugins);

        log.debug("PMD execution has been finished.");
    }
}
