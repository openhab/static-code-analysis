/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Checks the README.md files for:
 * <ul>
 * <li>missing empty lines after headers.
 * <li>missing empty lines before and after code sections.
 * <li>missing empty lines before and after lists.
 * </ul>
 *
 * <a href="https://www.openhab.org/docs/developer/guidelines.html">openHAB Coding Guidelines</a>
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 * @author Lyubomir Papazov - Change the Markdown parser to flexmark
 */
public class MarkdownCheck extends AbstractStaticCheck {

    public MarkdownCheck() {
        setFileExtensions(MARKDOWN_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        if (file.getName().equals(README_MD_FILE_NAME)) {
            checkReadMe(fileText);
        }
    }

    private void checkReadMe(FileText fileText) {
        MutableDataSet options = new MutableDataSet();
        // By setting this option to true, the parser provides line numbers in the original markdown text for each node
        options.set(Parser.TRACK_DOCUMENT_LINES, true);

        Node readmeMarkdownNode = parseMarkdown(fileText, options);
        // CallBack is used in order to use the protected log method of the AbstractStaticCheck in the Visitor
        MarkdownVisitorCallback callBack = (line, message) -> MarkdownCheck.this.log(line + 1, message);
        MarkdownVisitor visitor = new MarkdownVisitor(callBack, fileText);
        visitor.visit(readmeMarkdownNode);
    }
}
