/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.tools;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.google.common.collect.Lists;

/**
 * Executes the <a href=
 * "https://maven.apache.org/components/plugins/maven-checkstyle-plugin/">maven-checkstyle-
 * plugin</a> with a predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov
 *
 */
@Mojo(name = "checkstyle", requiresDependencyResolution = ResolutionScope.COMPILE)
public class CheckstyleChecker extends AbstractChecker {

    /**
     * Relative path of the XML configuration to use. If not set the default ruleset
     * file will be used - {@link #DEFAULT_RULE_SET_XML}
     */
    @Parameter(property = "checkstyle.ruleset")
    protected String checkstyleRuleset;

    /**
     * Relative path of the suppressions XML file to use. If not set the default
     * filter file will be used - {@link #DEFAULT_FILTER_XML}
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
    private List<Dependency> checkstylePlugins = new ArrayList<>();

    /**
     * Relative path of the properties file to use in the ruleset to configure
     * specific checks
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
    private static final String MEASUREMENTS_SPLIT = " : ";

    /**
     * This is a property in the maven-checkstyle-plugin that is used to describe
     * the location of the ruleset file used from the plugin.
     */
    private static final String CHECKSTYLE_RULE_SET_PROPERTY = "checkstyle.config.location";

    /**
     * This is a property in the maven-checkstyle-plugin that is used to describe
     * the location of the suppressions file used from the plugin.
     */
    private static final String CHECKSTYLE_SUPPRESSION_PROPERTY = "checkstyle.suppressions.location";

    private static final String CHECKSTYLE_RULE_SET_PROPERTIES_PROPERTY = "checkstyle.properties.location";

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        Properties userProps = loadPropertiesFromFile(CHECKSTYLE_PROPERTIES_FILE);

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

        checkstylePlugins.add(dependency("org.openhab.tools.sat.custom-checks", "checkstyle", plugin.getVersion()));
        // Maven may load an older version, if no version is specified
        checkstylePlugins.add(dependency("com.puppycrawl.tools", "checkstyle", "8.9"));
        checkstylePlugins.forEach(logDependency());

        Xpp3Dom config = configuration(element("sourceDirectory", mavenProject.getBasedir().toString()));

        executeCheck(MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID, MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID, checkstyleMavenVersion,
                MAVEN_CHECKSTYLE_PLUGIN_GOAL, config, checkstylePlugins);

        calculateAverageMeasurements();

        log.debug("Checkstyle execution has been finished.");
    }

    private void calculateAverageMeasurements() {
        try {
            Stream<String> lines = Files.lines(Paths.get("measurements.txt"));
            Map<String, Collection<Long>> checksToMeasurements = new HashMap<>();
            lines.map(x -> x.split(MEASUREMENTS_SPLIT)).forEach(x -> {
                String checkName = x[0];
                Long measuredTime = Long.parseLong(x[1]);
                if (checksToMeasurements.containsKey(checkName)) {
                    checksToMeasurements.get(checkName).add(measuredTime);
                } else {
                    checksToMeasurements.put(checkName, Lists.newArrayList(measuredTime));
                }
            });
            
            lines.close();

            Collection<String> avarages = checksToMeasurements.entrySet().stream().map(x -> {
                String key = x.getKey();
                Collection<Long> measurements = x.getValue();
                double value = measurements.stream().mapToLong(t->t).sum() / measurements.size();
                return key + ';' + Math.round(value);
            }).collect(Collectors.toList());

            new File("measurements.txt").deleteOnExit();
            try (PrintWriter writer = new PrintWriter("avarages.csv")) {
                writer.println("Check name;Time,ms");
                avarages.forEach(writer::println);
            }
        } catch (IOException e) {
            getLog().error("Error in opening measurements.txt file", e);
        }
    }

}
