/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A MOJO that opens a diff file containing only the names of the changed files
 * and then finds the artifactId of the maven module that contains these files.
 * The diff file is {@link #diffOutputFile} and should be created by the #{@link ScmDiffMojo}.
 * This MOJO should be either executed after the #{@link ScmDiffMojo} in the same phase, or executed in a later phase. 
 * On execution, {@link FindChangedBundlesMojo} creates affectedArtifactIds maven property
 * that contains the comma separated artifactIds of modules in which there are changes.
 *
 * @author Lyubomir Papazov - Initial contribution
 *
 */
@Mojo(name="find-changed-bundles")
public class FindChangedBundlesMojo extends AbstractMojo {

    private static final String ARTIFACT_ID_SEPARATOR = ",";
    private static final String ARTIFACT_ID_XPATH = "/project/artifactId/child::text()";
    public static final String AFFECTED_ARTIFACT_IDS_PROPERTY = "affectedArtifactIds";

    /**
     * The name of the diff file from which changed files should be read.
     */
    @Parameter(property = "diffOutputFile", defaultValue = ScmDiffMojo.DEFAULT_DIFF_OUTPUT_FILE)
    protected String  diffOutputFile;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    private Set<String> pomsForChangedFiles;
    private Log log;
    private XPathExpression exprArtifactId;

    public FindChangedBundlesMojo() {
        log = getLog();
        createArtifactIdXPathExpression();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path pathToDiffFile = Paths.get(project.getBasedir().getAbsolutePath(), diffOutputFile);
        try {
            Stream<String> stream = Files.lines(pathToDiffFile);
            //For multiple changed files in the same module, findPomForFile method returns the same pom for each file.
            //We are only interested in the distinct pom files.
            pomsForChangedFiles = stream.map(path -> findPomForFile(path)).distinct().collect(Collectors.toSet());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error occurred while processing pom files, listed in %s", pathToDiffFile));
        }

        Stream<String >artifactIds = pomsForChangedFiles.stream().map(path -> new File(path)).map(f -> getArtifactIdFromFile(f));

        String commaSeparatedArtifactIds = artifactIds.collect(Collectors.joining(ARTIFACT_ID_SEPARATOR));
        project.getProperties().setProperty(AFFECTED_ARTIFACT_IDS_PROPERTY, commaSeparatedArtifactIds);
    }

    private void createArtifactIdXPathExpression() {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            exprArtifactId = xpath.compile(ARTIFACT_ID_XPATH);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(String.format("Unable to compile the following XPath: %s", ARTIFACT_ID_XPATH), e);
        }
    }

    private String getArtifactIdFromFile(File pomXmlFile) {
        Document pomXmlDocument = null;

        try (InputStream pomXmlFileStream = new FileInputStream(pomXmlFile)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            pomXmlDocument = dBuilder.parse(pomXmlFileStream);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(String.format("Unable to create document from the file %s", pomXmlFile.getPath()), e);
        }

        NodeList nodes = null;
        try {
            nodes = (NodeList) exprArtifactId.evaluate(pomXmlDocument, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            log.error("Unable to find artifactId", e);
        }
        if (nodes == null || nodes.getLength() > 1) {
            throw new RuntimeException("Exactly one artifactId should be discovered by the Xpath expression");
        }
        return nodes.item(0).getNodeValue();
    }

    private String findPomForFile(String relativePath) {
        File changedFile = new File(relativePath);
        String parentDir = changedFile.getAbsoluteFile().getParentFile().getAbsolutePath();
        File pomFile = new File(parentDir, "pom.xml");
        if (pomFile.exists()) {
            return pomFile.getAbsolutePath();
        } else {
            return findPomForFile(parentDir);
        }
    }
}

