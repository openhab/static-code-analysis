/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.POM_XML_FILE_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.TARGET_DIRECTORY;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.XML_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.w3c.dom.Document;

import com.google.common.io.Files;
import com.ibm.icu.text.MessageFormat;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.sun.tools.jdeps.Main;

/**
 * 
 * Verifies if a project is restricted to specified Java Compact Profile.
 * 
 * Uses the Oracle Java jdpes tool. The check will be skipped, if the tool is
 * not present.
 * 
 * This check should be executed after the compilation phase is executed and a
 * .jar file is built
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class CompactProfileCheck extends AbstractStaticCheck {

    private static final String JDEPS_DOTOUTPUT = "-dotoutput";
    private static final Log logger = LogFactory
            .getLog(CompactProfileCheck.class);
    private static final String POM_ARTIFACT_ID_XPATH_EXPRESSION = "/project/artifactId/text()";
    private static final String POM_VERSION_XPATH_EXPRESSION = "/project/parent/version/text()";
    private static final String JDEPS_REPORT_DIR_NAME = "jdeps";

    private static final String JDEPS_RESULT_FILE_EXTENSION = ".dot";
    private static final String JDEPS_PROFILE = "-profile";
    private static final String JDEPS_HELP = "-help";
    private static final Profile DEFAULT_PROFILE = Profile.COMPACT_PROFILE_2;

    // Expected: "checkstyle.compactProfileCheckTest" -> "java.io (compact1)";
    private static final Pattern PROFILE_PATTERN = Pattern
            .compile("\\((.*?)\\)");
    private static final Pattern PACKAGE_PATTERN = Pattern
            .compile("\"(.*?)\".*\"(.*?)\"");
    private static final String MESSAGE_PATTERN = "Package {0} depends on {1}, which is a superset of {2}";

    boolean isJdepsAvailable = false;

    private Profile maximumProfile = DEFAULT_PROFILE;

    public void setMaximumProfile(String maximumProfile) {
        try {
            this.maximumProfile = Profile.fromString(maximumProfile);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown profile " + maximumProfile
                    + ". Fall back to default settings " + DEFAULT_PROFILE);
            this.maximumProfile = DEFAULT_PROFILE;
        }
    }

    public CompactProfileCheck() {
        setFileExtensions(XML_EXTENSION);
    }

    @Override
    public void beginProcessing(String charset) {
        StringWriter out = new StringWriter();
        Main.run(new String[]{JDEPS_HELP}, new PrintWriter(out));

        if (out.toString().contains(JDEPS_PROFILE)) {
            isJdepsAvailable = true;
        } else {
            isJdepsAvailable = false;
            logger.warn(
                    "Jdeps execution will be skipped as the version being used does not support Compact Profile information !");
        }
    }

    @Override
    protected void processFiltered(File file, FileText fileText)
            throws CheckstyleException {
        if (file.getName().equals(POM_XML_FILE_NAME) && isJdepsAvailable) {
            String jarFileName = getArtifactName(fileText);

            if (jarFileName != null) {
                Path targetDir = file.getParentFile().toPath()
                        .resolve(TARGET_DIRECTORY);
                Path jdepsReportDir = targetDir.resolve(JDEPS_REPORT_DIR_NAME);

                String jarFilePath = targetDir.resolve(jarFileName)
                        .toAbsolutePath().toString();
                String jdepsReportDirPath = jdepsReportDir.toAbsolutePath()
                        .toString();

                // Jdeps config
                String[] args = new String[4];
                args[0] = JDEPS_PROFILE;
                args[1] = JDEPS_DOTOUTPUT;
                args[2] = jdepsReportDirPath;
                args[3] = jarFilePath;

                StringWriter out = new StringWriter();
                Main.run(args, new PrintWriter(out));

                if (out.toString().contains("Error")) {
                    logger.error("Jdeps can not process file '" + jarFilePath
                            + "' : " + out.toString());
                    return;
                }

                analyzeResults(jdepsReportDir
                        .resolve(jarFileName + JDEPS_RESULT_FILE_EXTENSION));
            }
        }
    }

    private void analyzeResults(Path jdepsResult) {
        try {
            for (String line : Files.readLines(jdepsResult.toFile(),
                    Charset.defaultCharset())) {

                Matcher profileMatcher = PROFILE_PATTERN.matcher(line);
                Matcher packageMatcher = PACKAGE_PATTERN.matcher(line);

                if (profileMatcher.find() && packageMatcher.find()) {
                    String profileString = profileMatcher.group(1);
                    String sourcePackage = packageMatcher.group(1);
                    String dependingPackage = packageMatcher.group(2);

                    try {
                        Profile profile = Profile.fromString(profileString);

                        // Compare to the configured profile
                        if (profile.compareTo(maximumProfile) > 0) {

                            log(0, MessageFormat.format(MESSAGE_PATTERN,
                                    sourcePackage, dependingPackage,
                                    maximumProfile));
                        }
                    } catch (IllegalArgumentException e) {
                        logger.debug("Dependency to '" + dependingPackage + "' will be skipped as there is no profile information : " + profileString);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read jdeps output file "
                    + jdepsResult.toString(), e);
        }
    }

    private String getArtifactName(FileText fileText)
            throws CheckstyleException {
        Document xmlDocument = parseDomDocumentFromFile(fileText);

        try {
            XPathExpression artifactIdExpression = compileXPathExpression(
                    POM_ARTIFACT_ID_XPATH_EXPRESSION);
            String artifactID = (String) artifactIdExpression
                    .evaluate(xmlDocument, XPathConstants.STRING);

            XPathExpression versiondExpression = compileXPathExpression(
                    POM_VERSION_XPATH_EXPRESSION);
            String artifactVersion = (String) versiondExpression
                    .evaluate(xmlDocument, XPathConstants.STRING);

            return String.format("%s-%s.jar", artifactID, artifactVersion);
        } catch (XPathExpressionException e) {
            logger.error("Invalid XPath expression ! ", e);
            return null;
        }
    }

    public enum Profile {

        // Enums implement comparable in the order in which the constants are
        // declared
        NOT_FOUND("not found"),
        COMPACT_PROFILE_1("compact1"),
        COMPACT_PROFILE_2("compact2"),
        COMPACT_PROFILE_3("compact3"),
        FULL_SE("Full JRE");

        private String representation;

        Profile(String string) {
            this.representation = string;
        }

        @Override
        public String toString() {
            return representation;
        }

        static Profile fromString(String value) {
            for (Profile profile : Profile.values()) {
                if (profile.toString().equals(value)) {
                    return profile;
                }
            }
            throw new IllegalArgumentException(
                    "No profile with value " + value + " is defined ");
        }
    }
}
