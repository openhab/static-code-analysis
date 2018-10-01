/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.changes;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

import java.util.ArrayList;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * A MOJO that creates a file, containing the names of all changed files whithin the module,
 * for every maven module. In order for the names of new files to be in the diff file, 
 * this MOJO should be used after new files have been staged for commit.
 * The name of the file is set by {@link #diffOutputFile}.
 * The created diff files could be later processed by {@link FindChangedBundlesMojo}
 *
 * @author Lyubomir Papazov - Initial Contribution
 *
 */
@Mojo(name = "create-git-diff")
public class ScmDiffMojo extends AbstractMojo {

    //This command contains both the branch name and the options because currently it is not possible
    //to add options such as name-only to the scm:diff command.
    //FIXME When this PR is merged https://github.com/apache/maven-scm/pull/75 the name-only option will be available.
    //The double quotations are needed to escape the whitespace between master and --name-only
    private static final String SCM_OPTION_BRANCH_AND_NAME_ONLY = "\"\"master --name-only \"\"";
    private static final String SCM_OPTION_ONLY_SHOW_CHANGED_FILES_IN_CURRENT_FOLDER = "./";

    private static final String MAVEN_SCM_PLUGIN_ARTIFACT_ID = "maven-scm-plugin";
    private static final String MAVEN_SCM_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_SCM_DIFF_GOAL = "diff";

    static final String DEFAULT_DIFF_OUTPUT_FILE = "target/scm-changed-artifact-ids.diff";

    /**
     * The name of the diff file in which changed files will be written.
     */
    @Parameter(property = "diffOutputFile", defaultValue = DEFAULT_DIFF_OUTPUT_FILE)
    protected String diffOutputFile;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(property = "maven.scm.version", defaultValue = "1.10.0")
    private String scmMavenVersion;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Xpp3Dom config = configuration(element("startScmVersionType", "branch"),
                element("startScmVersion", SCM_OPTION_BRANCH_AND_NAME_ONLY), element("endScmVersionType", "branch"),
                element("endScmVersion", SCM_OPTION_ONLY_SHOW_CHANGED_FILES_IN_CURRENT_FOLDER), element("outputFile", diffOutputFile));

        Plugin plugin = MojoExecutor.plugin(MAVEN_SCM_PLUGIN_GROUP_ID, MAVEN_SCM_PLUGIN_ARTIFACT_ID, scmMavenVersion,
                new ArrayList<>());

        MojoExecutor.executeMojo(plugin, MAVEN_SCM_DIFF_GOAL, config,
                MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

}
