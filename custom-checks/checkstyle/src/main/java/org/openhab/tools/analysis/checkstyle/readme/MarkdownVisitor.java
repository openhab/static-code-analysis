/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.apache.commons.lang3.StringUtils;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

import com.puppycrawl.tools.checkstyle.api.FileText;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitorBase;

/**
 * This visitor processes headers, lists and code sections and logs errors when
 * needed.
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 * @author Lyubomir Papazov - Change the parsering library to flexmark and adjust the code to work with it
 */
class MarkdownVisitor extends NodeVisitorBase {

    private static final String EMPTY_LINE_AFTER_HEADER_MSG = "Missing an empty line after the Markdown header ('#').";
    private static final String EMPTY_LINE_BEFORE_LIST_MSG = "The line before a Markdown list must be empty.";
    private static final String EMPTY_LINE_AFTER_LIST_MSG = "The line after a Markdown list must be empty.";
    private static final String EMPTY_LINE_BEFORE_CODE_MSG = "The line before code formatting section must be empty.";
    private static final String EMPTY_LINE_AFTER_CODE_MSG = "The line after code formatting section must be empty.";
    private static final String EMPTY_CODE_BLOCK_WARNING = "There is an empty or unclosed code formatting section. Please correct it.";
    private static final String HEADER_AT_END_OF_FILE = "There is a header at the end of the Markdown file. Please consider adding some content below.";
    private static final String IMAGES_IN_DOC_FOLDER_MSG = "Images must be located in the doc/ folder.";

    private static final String REGEX_NEW_LINES = "\\\r?\\\n";

    /**
     * A callback is used in order to use the protected methods of {@link AbstractStaticCheck}
     */
    private MarkdownVisitorCallback callback;

    private FileText fileText;

    public MarkdownVisitor(MarkdownVisitorCallback callBack, FileText fileText) {
        this.callback = callBack;
        this.fileText = fileText;
    }

    /**
     * Example of heading: #HomeMatic Binding
     */
    public void visit(Heading heading) {
        validateHeadingPosition(heading.getEndLineNumber());
    }

    private void validateHeadingPosition(int zeroBasedHeaderLineNumber) {
        boolean isHeaderAtEndOfFile = zeroBasedHeaderLineNumber == fileText.size() - 1;
        if (isHeaderAtEndOfFile) {
            // log the one=based line number
            callback.log(zeroBasedHeaderLineNumber, HEADER_AT_END_OF_FILE);
        } else {
            // FileText uses zero-based indexes
            boolean isNextLineEmpty = StringUtils.isBlank(fileText.get(zeroBasedHeaderLineNumber + 1));
            if (!isNextLineEmpty) {
                // log the one=based line number
                callback.log(zeroBasedHeaderLineNumber, EMPTY_LINE_AFTER_HEADER_MSG);
            }
        }
    }

    /**
     * A fencedCodeBlock is a text-formatted like a programming code, example:
     * <code>private String name=null;</code>
     */
    public void visit(FencedCodeBlock fencedCodeBlock) {
        validateCodeSectionPosition(fencedCodeBlock.getLineNumber(), fencedCodeBlock.getEndLineNumber(),
                fencedCodeBlock);
    }

    private void validateCodeSectionPosition(int zeroBasedStartLineNumber, int zeroBasedEndLineNumber,
            Node codeBlockText) {
        Node codeSection = codeBlockText.getFirstChild();
        // Check if the code section is empty or blank
        if (codeSection != null && !StringUtils.isBlank(codeSection.getChars().toString())) {
            // The code block is not the first line, and the previous line is not empty
            if (zeroBasedStartLineNumber == 0 || !StringUtils.isBlank(fileText.get(zeroBasedStartLineNumber - 1))) {
                // log the one-based line number
                callback.log(zeroBasedStartLineNumber, EMPTY_LINE_BEFORE_CODE_MSG);
            }

            if (zeroBasedEndLineNumber != fileText.size() - 1
                    && !StringUtils.isBlank(fileText.get(zeroBasedEndLineNumber + 1))) {
                // log the one-based line number
                callback.log(zeroBasedEndLineNumber, EMPTY_LINE_AFTER_CODE_MSG);
            }
        } else {
            // log the one-based line number
            callback.log(zeroBasedStartLineNumber, EMPTY_CODE_BLOCK_WARNING);
        }
    }

    /**
     * Common method for processing visited {@link BulletList } and {@link OrderedList}
     *
     * @param listBlock a block type which is common parent of {@link BulletList } and {@link OrderedList}
     */
    private void processListBlock(ListBlock listBlock) {
        checkEmptyLineBefore(listBlock);
        checkEmptyLineAfterList(listBlock);
    }

    private void checkEmptyLineBefore(ListBlock listBlock) {
        int firstLineOfList = listBlock.getLineNumber();

        boolean isInnerList = listBlock.getParent() instanceof ListItem;
        if (isInnerList) {
            // Not checking if the line above is empty if it's another list item
            return;
        } else {
            boolean isListFirstLineInFile = firstLineOfList == 0;
            // The first line of the file can NOT be list
            if (isListFirstLineInFile || !StringUtils.isBlank(fileText.get(firstLineOfList - 1))) {
                // Log the one-based first line of the list
                callback.log(firstLineOfList, EMPTY_LINE_BEFORE_LIST_MSG);
            }
        }
    }

    private void checkEmptyLineAfterList(ListBlock listBlock) {
        ListItem lastListItem = (ListItem) listBlock.getLastChild();
        Node lastListItemContent = lastListItem.getLastChild();

        boolean isListEnd = lastListItemContent instanceof Paragraph;
        if (isListEnd) {
            String[] lastListItemLines = lastListItemContent.getChars().toString().split(REGEX_NEW_LINES);
            if (lastListItemLines.length > 1) {
                for (int i = 1; i < lastListItemLines.length; i++) {
                    if (!lastListItemLines[i].startsWith(" ")) {
                        // Log the one-based line where there is an empty line
                        callback.log(lastListItemContent.getLineNumber(), EMPTY_LINE_AFTER_LIST_MSG);
                        break;
                    }
                }
            }
        }
    }

    public void visit(ListBlock list) {
        list.getChildIterator().forEachRemaining(this::visit);
        processListBlock(list);
    }

    public void visit(Image image) {
        String url = image.getUrl().toString();
        if (!url.startsWith("doc/") && !url.startsWith("http://") && !url.startsWith("https://")) {
            callback.log(image.getLineNumber(), IMAGES_IN_DOC_FOLDER_MSG);
        }
    }

    @Override
    protected void visit(Node node) {
        if (node instanceof FencedCodeBlock) {
            visit((FencedCodeBlock) node);
        } else if (node instanceof Heading) {
            visit((Heading) node);
        } else if (node instanceof ListBlock) {
            visit((ListBlock) node);
        } else if (node instanceof Image) {
            visit((Image) node);
        } else {
            visitChildren(node);
        }
    }
}
