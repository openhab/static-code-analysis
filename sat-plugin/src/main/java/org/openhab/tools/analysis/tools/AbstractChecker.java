/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * Base class for MOJOs that call Maven plugins
 *
 * @author Svilen Valkanov
 *
 */
public abstract class AbstractChecker extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * If set to true no checks will be executed
     */
    @Parameter(property = "sat.skip", alias = "skip")
    protected boolean isSkipEnabled;

    /**
     * The Plugin Descriptor
     */
    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    protected PluginDescriptor plugin;

    /**
     * Loads properties from file into the Maven user properties
     *
     * @param relativePath
     *            - relative path to the properties file
     * @return - the loaded properties
     * @throws MojoExecutionException
     *             - when the properties file can not be found or loaded
     */
    protected Properties loadPropertiesFromFile(String relativePath) throws MojoExecutionException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(relativePath);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException | NullPointerException e) {
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
     * @param groupId
     *            - groupId of the plugin
     * @param artifactId
     *            - artifactId of the plugin
     * @param version
     *            - version of the plugin
     * @param goal
     *            - plugin goal to be executed
     * @param configuration
     *            - configuration of the plugin
     * @param dependencies
     *            - plugin dependencies
     * @throws MojoExecutionException
     *             - If there are any exceptions locating or executing the MOJO
     */
    protected void executeCheck(String groupId, String artifactId, String version, String goal, Xpp3Dom configuration,
            List<Dependency> dependencies) throws MojoExecutionException {
        if (isSkipEnabled) {
            getLog().debug("Skipping all checks due to skip property set to true");
            return;
        }
        Plugin plugin = MojoExecutor.plugin(groupId, artifactId, version, dependencies);

        MojoExecutor.executeMojo(plugin, goal, configuration,
                MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

    /**
     * Gets the location of a resource, external or internal. If
     * {@code externalRelativePath} is given, it will try to get the path to this
     * file, otherwise will get the {@link URL} to the {@code internalRelativePath}
     *
     * @param externalRelativePath
     *            - relative path to the execution directory of a resource not
     *            included in the current project
     * @param internalRelativePath
     *            - relative path of a resource included in the current project
     * @return location of a resource (absolute path or url)
     * @throws MojoExecutionException
     *             - if error occurs while trying to get the location
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
        return d -> getLog().info("Adding dependency to " + d.getArtifactId() + ":" + d.getVersion());
    }
    
    /**
     * 
     * @param location - The location where you want the xml generated
     * @param document - The document representing the xml we want generated
     * @throws FileNotFoundException -  if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException - if there is a problem writing the file
     */
    protected void writeXml(String location, Document document) throws FileNotFoundException, IOException {
      //Unfortunately XMLWriter does not implement AutoClosable so it cannot be used with try with resources.
        XMLWriter writer = null;
        try (FileOutputStream stream = new FileOutputStream(location)) {
            writer = new XMLWriter(stream, OutputFormat.createPrettyPrint());
            writer.write(document);
        } finally {
            if(writer != null) {
                writer.close();
            }                
        }
    }
}
