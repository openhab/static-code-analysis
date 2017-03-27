/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
 *
 * @author Petar Valchev
 *
 */
public class AboutHtmlCheck extends AbstractStaticCheck {
    private static final String SUSPEND_CHECKS_MSG = "No checks for the about.html file will be done.";

    private static final String VALID_ABOUT_HTML_FILE_LINK_MSG = "Here is an example of a valid about.html file: https://eclipse.org/legal/epl/about.html";

    private static final String HTML_EXTENSTION = "html";
    private static final String ABOUT_HTML_FILE_NAME = "about.html";

    private static final String LICENSE_HEADER = "License";
    private static final String PARAGRAPH_TAG = "p";
    private static final String HEADER_3_TAG = "h3";

    private final Logger logger = LoggerFactory.getLogger(AboutHtmlCheck.class);

    private String validAboutHtmlFileContent;

    private String validAboutHtmlFileURL;

    public AboutHtmlCheck() {
        setFileExtensions(HTML_EXTENSTION);
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
            logger.error("An exception was thrown, while trying to read the about.html url: {}. {}",
                    validAboutHtmlFileURL, SUSPEND_CHECKS_MSG, e);
        } finally {
            IOUtils.closeQuietly(validAboutHtmlInputStream);
        }
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        String fileName = file.getName();
        if (validAboutHtmlFileContent != null && fileName.equals(ABOUT_HTML_FILE_NAME)) {
            if (!isEmpty(file)) {
                Document fileDocument = parseHTMLDocumentFromFile(file);
                checkLicenseHeader(fileDocument);
                checkLicenseParagraph(fileDocument);
            } else {
                log(0, "Empty about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG);
            }
        }
    }

    private void checkLicenseHeader(Document processedAboutHtmlFileDocument) throws CheckstyleException {
        Elements processedAboutHtmlFileHeaderTags = processedAboutHtmlFileDocument.getElementsByTag(HEADER_3_TAG);
        if (!isElementProvided(processedAboutHtmlFileHeaderTags, LICENSE_HEADER)) {
            log(0, "Inavlid or missing license header in the about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG);
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
            log(0, "Inavlid or missing license paragraph in the about.html file. " + VALID_ABOUT_HTML_FILE_LINK_MSG);
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
