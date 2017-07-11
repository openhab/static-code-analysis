/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Checks if an about.html file is valid.
 * Verifies if the about.html file is added to the build.properties file
 *
 * @author Petar Valchev - Initial Implementation
 * @author Svilen Valkanov - Add check for inclusion in build.properties file
 */
public class AboutHtmlCheck extends AbstractStaticCheck {
    private static final String SUSPEND_CHECKS_MSG = "No checks for the about.html file will be done.";
    private static final String VALID_ABOUT_HTML_FILE_LINK_MSG = "Here is an example of a valid about.html file: ";
    private static final String MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG = "about.html file must be added to the bin.includes property";

    private static final String LICENSE_HEADER = "License";
    private static final String PARAGRAPH_TAG = "p";
    private static final String HEADER_3_TAG = "h3";

    private final Logger logger = LoggerFactory.getLogger(AboutHtmlCheck.class);

    private String validAboutHtmlFileContent;

    private String validAboutHtmlFileURL;

    public AboutHtmlCheck() {
        setFileExtensions(HTML_EXTENSION, PROPERTIES_EXTENSION);
    }

    // configuration property for url to a valid about.html file
    public void setValidAboutHtmlFileURL(String validAboutHtmlFileURL) {
        this.validAboutHtmlFileURL = validAboutHtmlFileURL;
    }

    @Override
    public void beginProcessing(String charset) {
        InputStream validAboutHtmlInputStream = null;
        try {
            URL url = new URL(validAboutHtmlFileURL);
            validAboutHtmlInputStream = url.openStream();
            // get the content of a valid about.html file,
            // so that we can compare the processed about.html
            // files with it
            validAboutHtmlFileContent = IOUtils.toString(validAboutHtmlInputStream);
        } catch (IOException e) {
            logger.error("An exception was thrown, while trying to read the about.html url: {}. {} {}",
                    validAboutHtmlFileURL, SUSPEND_CHECKS_MSG, e.getMessage());
        } finally {
            IOUtils.closeQuietly(validAboutHtmlInputStream);
        }
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        String fileName = file.getName();

        if (BUILD_PROPERTIES_FILE_NAME.equals(fileName)) {
            // The check will not log an error if build properties file is missing at all
            // We have other check for this case - RequiredFilesCheck
            boolean isAboutHtmlIncluded = checkBuildPropertiesFile(file, BIN_INCLUDES_PROPERTY_NAME,
                    ABOUT_HTML_FILE_NAME);
            if (!isAboutHtmlIncluded) {
                log(0, MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG, file.getPath());
            }
        } else if (validAboutHtmlFileContent != null && ABOUT_HTML_FILE_NAME.equals(fileName)) {
            if (!isEmpty(file)) {
                Document fileDocument = parseHTMLDocumentFromFile(file);
                checkLicenseHeader(fileDocument);
                checkLicenseParagraph(fileDocument);
            } else {
                log(0, "Empty about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG + validAboutHtmlFileURL);
            }
        }
    }

    private boolean checkBuildPropertiesFile(File file, String property, String value) throws CheckstyleException {
        if (!isEmpty(file)) {
            try {
                IBuild buildPropertiesFile = parseBuildProperties(file);
                IBuildEntry binIncludes = buildPropertiesFile.getEntry(property);

                return binIncludes != null && binIncludes.contains(value);

            } catch (CheckstyleException e) {
                logger.error("Error occured while processing {} file", BUILD_PROPERTIES_FILE_NAME, e);
            }
        }
        return false;
    }

    private void checkLicenseHeader(Document processedAboutHtmlFileDocument) throws CheckstyleException {
        Elements processedAboutHtmlFileHeaderTags = processedAboutHtmlFileDocument.getElementsByTag(HEADER_3_TAG);
        if (!isElementProvided(processedAboutHtmlFileHeaderTags, LICENSE_HEADER)) {
            log(0, "Invalid or missing license header in the about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG
                    + validAboutHtmlFileURL);
        }
    }

    private void checkLicenseParagraph(Document processedAboutHtmlFileDocument) {
        Document validAboutHtmlFileDocument = Jsoup.parse(validAboutHtmlFileContent);

        Elements validAboutHtmlFileParagraphTags = validAboutHtmlFileDocument.getElementsByTag(PARAGRAPH_TAG);
        // the paragraph with index 1 in the valid about.html file
        // is the license paragraph
        Element validAboutHtmlFileLicenseParagraph = validAboutHtmlFileParagraphTags.get(1);
        String validAboutHtmlFileLicenseParagraphContent = validAboutHtmlFileLicenseParagraph.html();
        Elements processedFileParagraphTags = processedAboutHtmlFileDocument.getElementsByTag(PARAGRAPH_TAG);

        if (!isElementProvided(processedFileParagraphTags, validAboutHtmlFileLicenseParagraphContent)) {
            log(0, "Invalid or missing license paragraph in the about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG
                    + validAboutHtmlFileURL);
        }
    }

    private boolean isElementProvided(Elements elements, String searchedElement) {
        for (Element element : elements) {
            String elementContent = element.html();
            if (elementContent.replaceAll("\\s", "").equals(searchedElement.replaceAll("\\s", ""))) {
                return true;
            }
        }
        return false;
    }
}
