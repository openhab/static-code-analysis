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
package org.openhab.tools.analysis.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * Base class for MOJOs that call Maven plugins
 *
 * @author Svilen Valkanov - Initial contribution
 */
public abstract class AbstractChecker extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * The Plugin Descriptor
     */
    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    protected PluginDescriptor plugin;

    /**
     * Loads properties from file into the Maven user properties
     *
     * @param relativePath relative path to the properties file
     * @return the loaded properties
     * @throws MojoExecutionException when the properties file can not be found or loaded
     */
    protected Properties loadPropertiesFromFile(String relativePath) throws MojoExecutionException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(relativePath);
        if (inputStream == null) {
            throw new MojoExecutionException(
                    "Can't load properties from file " + relativePath + " (resource not found)");
        }

        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new MojoExecutionException("Can't load properties from file " + relativePath, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                getLog().warn("Failed to close Input Stream ", e);
            }
        }

        Properties userProps = mavenProject.getProperties();

        Enumeration<?> e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            userProps.setProperty(key, properties.getProperty(key));
        }

        getLog().debug("Properties file " + relativePath + " loaded and properties set.");
        return userProps;
    }

    /**
     * Executes a Maven plugin using the {@link MojoExecutor}
     *
     * @param groupId groupId of the plugin
     * @param artifactId artifactId of the plugin
     * @param version version of the plugin
     * @param goal plugin goal to be executed
     * @param configuration configuration of the plugin
     * @param dependencies plugin dependencies
     * @throws MojoExecutionException If there are any exceptions locating or executing the MOJO
     */
    protected void executeCheck(String groupId, String artifactId, String version, String goal, Xpp3Dom configuration,
            List<Dependency> dependencies) throws MojoExecutionException {
        Plugin plugin = MojoExecutor.plugin(groupId, artifactId, version, dependencies);

        MojoExecutor.executeMojo(plugin, goal, configuration,
                MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

    /**
     * Gets the location of a resource, external or internal. If {@code externalRelativePath} is given, it
     * will try to get the path to this file, otherwise will get the {@link URL} to the {@code internalRelativePath}
     *
     * @param externalRelativePath relative path to the execution directory of a resource not included in the current
     *            project
     * @param internalRelativePath relative path of a resource included in the current project
     * @return location of a resource (absolute path or url)
     * @throws MojoExecutionException if error occurs while trying to get the location
     */
    protected String getLocation(String externalRelativePath, String internalRelativePath)
            throws MojoExecutionException {
        if (externalRelativePath != null) {
            Path executionDir = Paths.get(mavenSession.getExecutionRootDirectory());
            Path externalDir = Paths.get(externalRelativePath);
            Path resolvedPath = executionDir.resolve(externalDir);
            return resolvedPath.toString();
        } else {
            URL url = this.getClass().getClassLoader().getResource(internalRelativePath);
            return url.toString();
        }
    }

    /**
     * Uses the Maven logger to log a message about added dependency
     *
     * @return Consumer
     */
    protected Consumer<? super Dependency> logDependency() {
        return d -> getLog().debug("Adding dependency to " + d.getArtifactId() + ":" + d.getVersion());
    }
}
