/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.readme;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BIN_INCLUDES_PROPERTY_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.BUILD_PROPERTIES_FILE_NAME;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MARKDONW_EXTENSION;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.PROPERTIES_EXTENSION;
import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.README_MD_FILE_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.commonmark.node.Block;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.ListBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;

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
 */
public class MarkdownCheck extends AbstractStaticCheck {
    private static final String ADDED_README_FILE_IN_BUILD_PROPERTIES_MSG = "README.MD file must not be added to the bin.includes property";
    private static final String ADDED_DOC_FOLDER_IN_BUILD_PROPERTIES_MSG = "The doc folder must not be added to the bin.includes property";
    private static final String README_IO_ERROR_MESSAGE = "An exception was caught during processing markdown readme file {}";
    private static final String DOC_FOLDER_NAME = "doc";
    private final Logger logger = LoggerFactory.getLogger(MarkdownCheck.class);

    public MarkdownCheck() {
        setFileExtensions(MARKDONW_EXTENSION, PROPERTIES_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        switch (file.getName()) {
            case BUILD_PROPERTIES_FILE_NAME:
                checkBuildProperties(file);
                break;
            case README_MD_FILE_NAME:
                checkReadMe(file, fileText.toLinesArray());
                break;
        }
    }

    private void checkBuildProperties(File file) throws CheckstyleException {
        // The check will not log an errors if build properties file is missing
        // We have other check regarding this case - RequiredFilesCheck
        IBuild buildPropertiesEntry = parseBuildProperties(file);
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

    private void checkReadMe(File file, String[] lines) {
        // Don't need all block types visited that's why only these are enabled
        Set<Class<? extends Block>> enabledBlockTypes = new HashSet<>(
                Arrays.asList(Heading.class, ListBlock.class, FencedCodeBlock.class, IndentedCodeBlock.class));
        Parser parser = Parser.builder().enabledBlockTypes(enabledBlockTypes).build();

        try {
            Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            Node readmeMarkdownNode = parser.parseReader(reader);
            
            // CallBack is used in order to use the protected methods of the AbstractStaticCheck in the Visitor
            MarkdownVisitorCallback callBack = new MarkdownVisitorCallback() {
                @Override
                public int findLineNumber(List<String> fileContent, String searchedText, int startLineNumber) {
                    return MarkdownCheck.this.findLineNumber(lines, searchedText, startLineNumber);
                }

                @Override
                public void log(int line, String message) {
                    MarkdownCheck.this.log(line, message);
                }
            };
            LinkedList<String> linesList= new LinkedList<String>(Arrays.asList(lines));
            MarkDownVisitor visitor = new MarkDownVisitor(callBack, file, linesList);
            readmeMarkdownNode.accept(visitor);
        } catch (IOException e) {
            logger.error(README_IO_ERROR_MESSAGE, file.getAbsolutePath(), e);
        }
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
