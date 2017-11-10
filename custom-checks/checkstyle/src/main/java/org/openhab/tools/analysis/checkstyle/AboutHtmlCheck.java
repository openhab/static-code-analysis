/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.openhab.tools.analysis.utils.CachingHttpClient;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

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

    private final Log logger = LogFactory.getLog(AboutHtmlCheck.class);

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
        CachingHttpClient<String> cachingClient = new CachingHttpClient<>(data -> new String(data));

        try {
            URL url = new URL(validAboutHtmlFileURL);
            // get the content of a valid about.html file,
            // so that we can compare the processed about.html
            // files with it
            validAboutHtmlFileContent = cachingClient.get(url);
        } catch (IOException e) {
            String message = MessageFormat.format("Unable to get about.html file from {0} : {1}", validAboutHtmlFileURL,
                    e.getMessage());
            logger.error(message, e);
        }
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        String fileName = file.getName();

        if (BUILD_PROPERTIES_FILE_NAME.equals(fileName)) {
            // The check will not log an error if build properties file is missing at all
            // We have other check for this case - RequiredFilesCheck
            boolean isAboutHtmlIncluded = checkBuildPropertiesFile(fileText, BIN_INCLUDES_PROPERTY_NAME,
                    ABOUT_HTML_FILE_NAME);
            if (!isAboutHtmlIncluded) {
                log(0, MISSING_ABOUT_HTML_IN_BUILD_PROPERTIES_MSG, file.getPath());
            }
        } else if (ABOUT_HTML_FILE_NAME.equals(fileName)) {
            if (!isEmpty(fileText)) {
                if (validAboutHtmlFileContent != null) {
                    Document fileDocument = parseHTMLDocumentFromFile(fileText);
                    checkLicenseHeader(fileDocument);
                    checkLicenseParagraph(fileDocument);
                } else {
                    logger.warn("About.html validation will be skipped as the about.html file download failed");
                }
            } else {
                log(0, "Empty about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG + validAboutHtmlFileURL);
            }
        }
    }

    private boolean checkBuildPropertiesFile(FileText fileText, String property, String value)
            throws CheckstyleException {
        if (!isEmpty(fileText)) {
            try {
                IBuild buildPropertiesFile = parseBuildProperties(fileText);
                IBuildEntry binIncludes = buildPropertiesFile.getEntry(property);

                return binIncludes != null && binIncludes.contains(value);

            } catch (CheckstyleException e) {
                String message = MessageFormat.format("Error occured while processing {0} file",
                        BUILD_PROPERTIES_FILE_NAME);
                logger.error(message, e);
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
