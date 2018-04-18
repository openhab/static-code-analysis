package org.openhab.tools.analysis.tools.test;

import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.openhab.tools.analysis.tools.CheckstyleChecker;

public class CheckstyleCheckerMock extends CheckstyleChecker {    
    public void executeCheck(String groupId, String artifactId, String version, String goal, Xpp3Dom configuration, List<Dependency> dependencies) throws MojoExecutionException {
        super.executeCheck(groupId, artifactId, version, goal, configuration, dependencies);
    }
    
    public void setMavenProject(MavenProject value) {
        this.mavenProject = value;
    }
    
    public void setPluginDescriptor(PluginDescriptor value) {
        this.plugin = value;
    }
    
    public void setMavenSession(MavenSession session) {
        this.mavenSession = session;
    }
    
    public String getLocation(String externalRelativePath, String internalRelativePath) throws MojoExecutionException {
        //Set up in the tests
        return super.getLocation(externalRelativePath, internalRelativePath);
    }
}
