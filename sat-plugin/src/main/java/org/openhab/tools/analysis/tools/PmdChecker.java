/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.tools;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.dependency;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Executes the <a href=
 * "https://maven.apache.org/plugins/maven-pmd-plugin/index.html">maven-pmd-plugin</a>
 * with a predefined ruleset file and configuration properties
 *
 * @author Svilen Valkanov
 *
 */
@Mojo(name = "pmd", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PmdChecker extends AbstractChecker {
    private static final String CATEGORY_SKIP_MESSAGE = "Skipping check %s due to category %s being skipped";
    private static final String DEFAULT_RULESET_XML = "rulesets/pmd/rules.xml";

    private static final String DEFAULT_FILTER_XML = "rulesets/pmd/suppressions.properties";

    /**
     * Relative path of the XML configuration to use. If not set the default ruleset
     * file will be used - {@link #DEFAULT_RULESET_XML}
     */
    @Parameter(property = "pmd.ruleset")
    protected String pmdRuleset;

    /**
     * Relative path of the suppressions XML file to use. If not set the default
     * filter file will be used - {@link #DEFAULT_FILTER_XML}
     */
    @Parameter(property = "pmd.excludeFromFailureFile")
    protected String pmdFilter;

    /**
     * The version of the maven-pmd-plugin that will be used
     */
    @Parameter(property = "maven.pmd.version", defaultValue = "3.9.0")
    private String mavenPmdVersion;

    /**
     * If set to true pmd checks will not be executed
     */
    @Parameter(property = "skip.pmd")
    private boolean isPmdSkipped;

    /**
     * A list with artifacts that contain additional checks for PMD
     */
    @Parameter
    private List<Dependency> pmdPlugins = new ArrayList<>();

    /**
     * A collection of all categories that will be skipped during the exeuction of
     * PMD. If not set all categories of checks will be executed
     */
    @Parameter(property = "pmd.skippedCategories")
    private Collection<String> skippedCategories;

    private static final String PMD_VERSION = "6.2.0";
    /**
     * Location of the properties files that contains configuration options for the
     * maven-pmd-plugin
     */
    private static final String PMD_PROPERTIES_FILE = "configuration/pmd.properties";

    private static final String MAVEN_PMD_PLUGIN_ARTIFACT_ID = "maven-pmd-plugin";
    private static final String MAVEN_PMD_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_PMD_PLUGIN_GOAL = "pmd";

    // For test purposes
    public void setIsPmdSkipped(boolean value) {
        this.isPmdSkipped = value;
    }

    public void setMavenProject(MavenProject value) {
        this.mavenProject = value;
    }

    public void setSkippedCategories(Collection<String> value) {
        this.skippedCategories = value;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        if (isPmdSkipped) {
            log.debug("Skipping all PMD checks due to skipPmd property set to true");
            return;
        }

        Properties userProps = loadPropertiesFromFile(PMD_PROPERTIES_FILE);

        String excludeFromFailureLocation = getLocation(pmdFilter, DEFAULT_FILTER_XML);
        log.debug("Exclude filter file location is " + excludeFromFailureLocation);
        userProps.setProperty("pmd.excludeFromFailureFile", excludeFromFailureLocation);

        String pmdRulesLocation = getLocation(pmdRuleset, DEFAULT_RULESET_XML);
        if (!skippedCategories.isEmpty()) {
            pmdRulesLocation = mavenProject.getModel().getBuild().getDirectory() + File.separator + "pmd-rules.xml";
            generateRulesXmlFile(pmdRulesLocation);
        }

        log.debug("Ruleset location is " + pmdRulesLocation);
        // These configuration properties are not exposed from the maven-pmd-plugin as
        // user properties,
        // so they have to be set direct in the configuration
        Xpp3Dom configuration = configuration(
                element("targetDirectory", userProps.getProperty("pmd.custom.targetDirectory")),
                element("compileSourceRoots", userProps.getProperty("pmd.custom.compileSourceRoots")),
                element("rulesets", element("ruleset", pmdRulesLocation)));

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

    private void generateRulesXmlFile(String location) throws MojoExecutionException {
        try {
            String rulesLocation = getLocation(pmdRuleset, DEFAULT_RULESET_XML);
            URL url = new URL(rulesLocation);
            SAXReader reader = new SAXReader();
            Document document = reader.read(url);
            List<Node> ruleNodes = document.getRootElement().elements("rule");
            ruleNodes.forEach(node -> {
                Element nodeAsElement = (Element) node;
                String ruleType = nodeAsElement.attributeValue("ref");
                for (String category : this.skippedCategories) {
                    if (ruleType.contains(category)) {
                        getLog().debug(String.format(CATEGORY_SKIP_MESSAGE, ruleType, category));
                        // We detach the rule if it belongs to a skipped category
                        node.detach();
                    }
                }
            });

            writeXml(location, document);
        } catch (DocumentException | IOException e) {
            getLog().error("Error in generating the PMD rules.xml file", e);
        }
    }
}
