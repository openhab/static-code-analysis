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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

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
    private static final String SKIPPING_CHECK_TEMPLATE = "Skipping check %s due to %s type being skipped";
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
     * If set to true no checkstyle checks will be executed.
     */
    @Parameter(property = "skip.checkstyle")
    private boolean isCheckstyleSkipped;

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
     * A collection of all the check types that will be skipped during the execution
     * of checkstyle. If not set all checks are executed.
     */
    @Parameter(property = "checkstyle.skippedFileTypes")
    private Collection<String> skippedFileTypes;

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

    private static final String CHECKSTYLE_DTD_SCHEMA = "http://www.puppycrawl.com/dtds/configuration_1_3.dtd";

    // Setters will be used in the test
    public void setSkippedCheckTypes(Collection<String> value) {
        this.skippedFileTypes = value;
    }

    public void setIsCheckstyleSkipped(boolean value) {
        this.isCheckstyleSkipped = value;
    }

    public void setCheckstyleMavenVersion(String value) {
        this.checkstyleMavenVersion = value;
    }

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (isCheckstyleSkipped) {
            log.debug("Skipping all checkstyle checks due to skipCheckstyle property set to true");
            return;
        }

        Properties userProps = loadPropertiesFromFile(CHECKSTYLE_PROPERTIES_FILE);
        String checkstyleRulesXmlLocation = mavenProject.getModel().getBuild().getDirectory() + File.separator
                + "checkstyle-rules.xml";
        generateRulesXmlFile(checkstyleRulesXmlLocation);
        log.debug("Ruleset location is " + checkstyleRulesXmlLocation);
        userProps.setProperty(CHECKSTYLE_RULE_SET_PROPERTY, checkstyleRulesXmlLocation);

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

        log.debug("Checkstyle execution has been finished.");

    }

    private void generateRulesXmlFile(String location) throws MojoExecutionException {
        try {
            String rulesLocation = getLocation(checkstyleRuleset, DEFAULT_RULE_SET_XML);

            URL url = new URL(rulesLocation);
            SAXReader reader = new SAXReader();
            Document document = reader.read(url);
            List<Node> typeNodes = document.selectNodes("//type");
            typeNodes.forEach(node -> {
                Element nodeAsElement = (Element) node;
                String checkTypes = nodeAsElement.attributeValue("value");
                for (String skippedCheckType : skippedFileTypes) {
                    if (checkTypes.contains(skippedCheckType)) {
                        getLog().debug(String.format(SKIPPING_CHECK_TEMPLATE,
                                node.getParent().attribute("name").getText(), skippedCheckType));
                        // If the skipped check types contain the value of the current type the module
                        // associated with it is detached.
                        node.getParent().detach();
                    }
                }

                // We always detach the type node because it is not part of the expected
                // checkstyle rules.xml
                node.detach();
            });

            document.getDocType().setSystemID(CHECKSTYLE_DTD_SCHEMA);
            
          writeXml(location, document);
        } catch (DocumentException | IOException e) {
            getLog().error("Error in generating the checkstyle rules.xml file", e);
        }
    }
}
