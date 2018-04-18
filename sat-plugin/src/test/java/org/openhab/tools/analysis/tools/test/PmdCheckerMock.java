package org.openhab.tools.analysis.tools.test;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.openhab.tools.analysis.tools.PmdChecker;

public class PmdCheckerMock extends PmdChecker {
    public void executeCheck(String groupId, String artifactId, String version, String goal, Xpp3Dom configuration,
            List<Dependency> dependencies) throws MojoExecutionException {
        super.executeCheck(groupId, artifactId, version, goal, configuration, dependencies);
    }

    public String getLocation(String externalRelativePath, String internalRelativePath) throws MojoExecutionException {
        return super.getLocation(externalRelativePath, internalRelativePath);
    }
}
