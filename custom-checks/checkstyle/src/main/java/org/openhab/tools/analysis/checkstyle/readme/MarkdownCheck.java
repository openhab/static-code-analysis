/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.tools.analysis.checkstyle.readme;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BIN_INCLUDES_PROPERTY_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BUILD_PROPERTIES_FILE_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MARKDONW_EXTENSION;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.PROPERTIES_EXTENSION;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.README_MD_FILE_NAME;

import java.io.File;

import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;

/**
 * Checks the README.md files for:
 * <ul>
 * <li>missing empty lines after headers.
 * <li>missing empty lines before and after code sections.
 * <li>missing empty lines before and after lists.
 * </ul>
 *
 * Checks the build.properties for:
 * <ul>
 * <li>the README.MD shouldn't be added in build.properties.
 * <li>the doc folder shouldn't be added in build.properties.
 * </ul>
 * <a href="https://www.eclipse.org/smarthome/documentation/development/bindings/docs.html">Eclipse Smarthome
 * Documentation Guidelines
 * info</a>
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 * @author Lyubomir Papazov - Change the Markdown parser to flexmark
 */
public class MarkdownCheck extends AbstractStaticCheck {
    private static final String ADDED_README_FILE_IN_BUILD_PROPERTIES_MSG = "README.MD file must not be added to the bin.includes property";
    private static final String ADDED_DOC_FOLDER_IN_BUILD_PROPERTIES_MSG = "The doc folder must not be added to the bin.includes property";
    private static final String DOC_FOLDER_NAME = "doc";

    public MarkdownCheck() {
        setFileExtensions(MARKDONW_EXTENSION, PROPERTIES_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        switch (file.getName()) {
            case BUILD_PROPERTIES_FILE_NAME:
                checkBuildProperties(fileText);
                break;
            case README_MD_FILE_NAME:
                checkReadMe(fileText);
                break;
        }
    }

    private void checkBuildProperties(FileText fileText) throws CheckstyleException {
        // The check will not log an errors if build properties file is missing
        // We have other check regarding this case - RequiredFilesCheck
        IBuild buildPropertiesEntry = parseBuildProperties(fileText);
        boolean isReadMeIncluded = checkBuildPropertiesEntry(buildPropertiesEntry, BIN_INCLUDES_PROPERTY_NAME,
                README_MD_FILE_NAME);
        if (isReadMeIncluded) {
            log(0, ADDED_README_FILE_IN_BUILD_PROPERTIES_MSG);
        }
        boolean isDocFolderIncluded = checkBuildPropertiesEntry(buildPropertiesEntry, BIN_INCLUDES_PROPERTY_NAME,
                DOC_FOLDER_NAME);
        if (isDocFolderIncluded) {
            log(0, ADDED_DOC_FOLDER_IN_BUILD_PROPERTIES_MSG);
        }
    }

    private void checkReadMe(FileText fileText) {

        MutableDataSet options = new MutableDataSet();
        // By setting this option to true, the parser provides line numbers in the original markdown text for each node
        options.set(Parser.TRACK_DOCUMENT_LINES, true);

        Node readmeMarkdownNode = parseMarkdown(fileText, options);
        // CallBack is used in order to use the protected log method of the AbstractStaticCheck in the Visitor
        MarkdownVisitorCallback callBack = new MarkdownVisitorCallback() {
            @Override
            public void log(int line, String message) {
                MarkdownCheck.this.log(line + 1, message);
            }
        };
        MarkdownVisitor visitor = new MarkdownVisitor(callBack, fileText);
        visitor.visit(readmeMarkdownNode);
    }

    /**
     * Checks whether the given value is added in the build.properties file.
     */
    private boolean checkBuildPropertiesEntry(IBuild buildPropertiesFile, String property, String value)
            throws CheckstyleException {
        IBuildEntry binIncludes = buildPropertiesFile.getEntry(property);
        return binIncludes != null && binIncludes.contains(value);
    }
}
