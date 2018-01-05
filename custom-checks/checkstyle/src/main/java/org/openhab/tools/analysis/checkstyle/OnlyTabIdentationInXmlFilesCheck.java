/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.XML_EXTENSION;

import java.io.File;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks whether whitespace characters are use instead of tabs in xml files 
 * indentations and generates warnings in such cases.
 * 
 * @author Lyubomir Papazov - initial contribution
 *
 */
public class OnlyTabIdentationInXmlFilesCheck extends AbstractStaticCheck {

    private static final String PATTERN_TO_BE_FOLLOWED = "^\\t*[<].*";
    private static final String WARNING_MESSAGE = "There were whitespace characters used for indentation. Please use tab characters instead";
    private boolean onlyShowFirstWarning;
    
    
    public OnlyTabIdentationInXmlFilesCheck() {
        setFileExtensions(XML_EXTENSION);
    }
    
    public void setOnlyShowFirstWarning(Boolean showFirstExceptionOnly)
    {
        this.onlyShowFirstWarning = showFirstExceptionOnly;
    }
    
    @Override
    protected void processFiltered(File file, FileText fileText) {
        processXmlTabIdentationCheck(fileText);
    }
    
    private void processXmlTabIdentationCheck(FileText fileText) {
        for (int lineNumber = 0; lineNumber < fileText.size(); lineNumber++) {
            String line = fileText.get(lineNumber);
            if (!line.matches(PATTERN_TO_BE_FOLLOWED)) {
                //log the 1-based index of the file
                log(lineNumber + 1, WARNING_MESSAGE);
                if (onlyShowFirstWarning) {
                    return;
                }
            }
        }
    }
}
