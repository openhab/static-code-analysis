/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.readme;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.utils.StringUtils;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.ListBlock;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.openhab.tools.analysis.checkstyle.api.NoResultException;
import org.openhab.tools.analysis.utils.LineFormatterFunction;

import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * This visitor processes headers, lists and code sections and logs errors when
 * needed.
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 */
class MarkdownVisitor extends AbstractVisitor {

    private static final String EMPTY_LINE_AFTER_HEADER_MSG = "Missing an empty line after the Markdown header ('#').";
    private static final String EMPTY_LINE_BEFORE_LIST_MSG = "The line before a Markdown list must be empty.";
    private static final String EMPTY_LINE_AFTER_LIST_MSG = "The line after a Markdown list must be empty.";
    private static final String EMPTY_LINE_BEFORE_CODE_MSG = "The line before code formatting section must be empty.";
    private static final String EMPTY_LINE_AFTER_CODE_MSG = "The line after code formatting section must be empty.";
    private static final String EMPTY_CODE_BLOCK_WARNING = "There is an empty or unclosed code formatting section. Please correct it.";
    private static final String HEADER_AT_END_OF_FILE = "There is a header at the end of the Markdown file. Please consider adding some content below.";
    private final Log logger = LogFactory.getLog(MarkdownVisitor.class);

    /**
     * This field stores the line number where the processing of the source is up to.
     * Every time a MarkDown element is visited the pointer stores its starting line number in the source.
     * And when it is processed the pointer stores its ending line.
     */
    private int currentLinePointer = 0;

    /**
     * A callback is used in order to use the protected methods of {@link AbstractStaticCheck}
     */
    private MarkdownVisitorCallback callback;
    private LineFormatterFunction lineFormatter;
    private FileText fileText;

    public MarkdownVisitor(MarkdownVisitorCallback callBack, FileText fileText) {
        this.callback = callBack;
        this.fileText = fileText;
    }

    public MarkdownVisitor(MarkdownVisitorCallback callBack, FileText fileText, LineFormatterFunction lineFormatter) {
        this(callBack, fileText);
        this.lineFormatter = lineFormatter;
    }

     /**
     * Example of heading: #HomeMatic Binding
     */
    @Override
    public void visit(Heading heading) {
        String headerContent = getLiteralOfElement(heading);
        validateHeader(headerContent);
    }

    /**
     * This method processes the Headings in the README which the Markdown
     * parser returns.
     *
     * @param headerValue - a String containing the literal of found Heading node
     */
    private void validateHeader(String headerValue) {
        if (headerValue != null) {
            int headerLineNumber;
            try {
                headerLineNumber = callback.findLineNumber(fileText, headerValue, currentLinePointer, lineFormatter);
                currentLinePointer = headerLineNumber;
                boolean isHeaderAtEndOfFile = headerLineNumber == fileText.size();
                if (isHeaderAtEndOfFile) {
                    callback.log(headerLineNumber, HEADER_AT_END_OF_FILE);
                } else {
                    boolean isNextLineEmpty = StringUtils.isBlank(fileText.get(headerLineNumber));
                    if (!isNextLineEmpty) {
                        callback.log(headerLineNumber, EMPTY_LINE_AFTER_HEADER_MSG);
                    }
                }
            } catch (NoResultException e) {
                logger.error("A header cannot be processed properly: " + headerValue, e);
            }
        } else {
            String message = MessageFormat.format(
                    "Occurred an error while processing the Markdown file {0}. The header value is null.",
                    fileText.getFile().getAbsolutePath());
            logger.warn(message);
        }
    }

    /**
     * A fencedCodeBlock is a text-formatted like a programming code, example:
     * <code>private String name=null;</code>
     */
    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        String codeLiteral = fencedCodeBlock.getLiteral();
        validateCodeSection(codeLiteral);
    }

    /**
     * Processes FencedCodeBlock nodes the parser returns.
     *
     * @param codeBlock - multiLine String containing the literal of the code block
     */
    private void validateCodeSection(String codeBlock) {
        int codeStartingLineNumber = 0;
        int codeEndingLineNumber = 0;

        // Splitting the String in case it is multiline, else the array would
        // have only one element
        String codeSectionLines[] = codeBlock.split("\\r?\\n");
        if (!StringUtils.isBlank(codeBlock)) {
            try {
                codeStartingLineNumber = callback.findLineNumber(fileText, codeSectionLines[0], currentLinePointer, lineFormatter);
                codeStartingLineNumber--;
                currentLinePointer = codeStartingLineNumber;
                // Start from the line above the code section
                verifyBeforeCodeSection(codeStartingLineNumber);
                codeEndingLineNumber = codeStartingLineNumber + codeSectionLines.length;
                codeEndingLineNumber++;
                currentLinePointer = codeEndingLineNumber;
                verifyAfterCodeSection(codeEndingLineNumber);
            } catch (NoResultException e) {
                logger.error("A code section wasn't processed properly! " + codeBlock, e);
            }
        } else {
            callback.log(currentLinePointer, EMPTY_CODE_BLOCK_WARNING);
        }
    }

    private void verifyBeforeCodeSection(int codeStartingLineNumber) {
        if (codeStartingLineNumber == 1) {
            callback.log(codeStartingLineNumber, EMPTY_LINE_BEFORE_CODE_MSG);
        } else {
            // -2 because the line before the code is occupied with code section opening- ```
            int lineBeforeCodeSection = codeStartingLineNumber - 2;
            boolean isLineBeforeCodeSectionEmpty = StringUtils.isBlank(fileText.get(lineBeforeCodeSection));
            if (!isLineBeforeCodeSectionEmpty) {
                callback.log(codeStartingLineNumber, EMPTY_LINE_BEFORE_CODE_MSG);
            }
        }
    }

    private void verifyAfterCodeSection(int codeEndingLineNumber) {
        boolean isCodeSectionAtEndOfFile = (codeEndingLineNumber == fileText.size());
        // There is another check that logs errors if there is not an empty line at the end of file
        // named NewLineAtEndOfFileCheck
        if (!isCodeSectionAtEndOfFile) {
            if (!StringUtils.isBlank(fileText.get(codeEndingLineNumber))) {
                callback.log(codeEndingLineNumber, EMPTY_LINE_AFTER_CODE_MSG);
            }
        }
    }

    /**
     * Common method for processing visited {@link BulletList } and {@link OrderedList}
     *
     * @param listBlock - a block type which is common parent of {@link BulletList } and {@link OrderedList}
     */
    private void processListBlock(ListBlock listBlock) {
        String firstLineOfList = getFirstLineInList(listBlock);
        String lastLineOfList = getLastLineInList(listBlock);
        if (lastLineOfList == null) {
            // in case it is one lined list (last line would be null)
            lastLineOfList = firstLineOfList;
        }
        int listLenght = getListLenght(listBlock);
        markDownListProcessing(firstLineOfList, lastLineOfList, listLenght);
    }

    private int getListLenght(ListBlock listBlock) {
        MarkdownListVisitor listVisitor = new MarkdownListVisitor();
        listBlock.accept(listVisitor);
        return listVisitor.getListLenght();
    }

    @Override
    public void visit(BulletList bulletList) {
        processListBlock(bulletList);
    }

    @Override
    public void visit(OrderedList orderedList) {
        processListBlock(orderedList);
    }

    /**
     * This method processes list blocks in the specified Markdown file.
     *
     * @param firstLineOfList - the literal of the first list item
     * @param lastLineOfList - the literal of the last list item
     * @param listLenght - the number of list items (note that they can be multiLine)
     */
    private void markDownListProcessing(String firstLineOfList, String lastLineOfList, int listLenght) {
        int listStartingLineNumber = 0;
        int listEndingLineNumber = 0;
        try {
            listStartingLineNumber = callback.findLineNumber(fileText, firstLineOfList, currentLinePointer, lineFormatter);
        } catch (NoResultException e) {
            logger.error("A list starting cannot be processed properly: " + firstLineOfList, e);
        }
        if (listStartingLineNumber == 1) {
            callback.log(listStartingLineNumber, EMPTY_LINE_BEFORE_LIST_MSG);
        } else {
            // pointer goes before the list ending to narrow the searching scope
            // sometimes there could be 2 list items with same literal
            currentLinePointer = listStartingLineNumber + listLenght - 2;
            try {
                listEndingLineNumber = callback.findLineNumber(fileText, lastLineOfList, currentLinePointer,  lineFormatter);
                currentLinePointer = listEndingLineNumber;
                verifyLineBeforeListBlock(listStartingLineNumber);
                verifyLineAfterListBlock(listEndingLineNumber);
            } catch (NoResultException e) {
                callback.log(listStartingLineNumber, "Unable to process the last line of the list that starts on line: " + listStartingLineNumber);
                logger.error("A list ending cannot be processed properly: " + lastLineOfList, e);
            }
        }

    }

    private void verifyLineBeforeListBlock(int listStartingLineNumber) {
        // Starting number is decreased with 2 lines because previous line is get and there is 0 indexation
        boolean isPreviousLineEmpty = !StringUtils.isBlank(fileText.get(listStartingLineNumber - 2));
        if (isPreviousLineEmpty) {
            // the -1 is used when logging the error to mark the exact line which have to be empty
            callback.log(listStartingLineNumber - 1, EMPTY_LINE_BEFORE_LIST_MSG);
        }
    }

    private void verifyLineAfterListBlock(int listEndingLineNumber) {
        boolean isListAtEndOfFile = (listEndingLineNumber == fileText.size());
        if (!isListAtEndOfFile) {
            boolean isNextLineEmpty = StringUtils.isBlank(fileText.get(listEndingLineNumber));
            if (!isNextLineEmpty) {
                callback.log(listEndingLineNumber, EMPTY_LINE_AFTER_LIST_MSG);
            }
        }
        // If the list block is the last entry in the markdown file, the check for an empty
        // line after the list will not be performed in order to avoid reporting false positives.
    }

    /**
     * If there is a specific type of header or list item this method recursively gets its literal
     *
     * @param node - The node that is being processed to get its literal
     * @return - returns null if the literal wasn't processed properly
     */
    private String getLiteralOfElement(Node node) {
        if (node != null) {
            if (node instanceof Text) {
                Text text = (Text) node;
                return text.getLiteral();
            } else if (node instanceof Code) {
                // return the literal of a code-formatted list items
                Code code = (Code) node;
                return code.getLiteral();
            } else if (node instanceof IndentedCodeBlock) {
                // In case there is a multiline code block as a child of list item
                IndentedCodeBlock code = (IndentedCodeBlock) node;
                String codeSectionLines[] = code.getLiteral().split("(\\r?\\n)|(\\r)");
                if (codeSectionLines.length != 0) {
                    return codeSectionLines[codeSectionLines.length - 1];
                }
            } else {

                // The children are processed recursively till text or code node is found
                // First child is used because Text and Code nodes are always first child nodes
                return getLiteralOfElement(node.getFirstChild());
            }
        }
        return null;
    }

    /**
     * The first child represents the first list item in the list block. Then recursively check if there are
     * any child nodes of this item and get the literal of it. Recursion is needed because there could be lists with
     * different structure and formatting.
     *
     * @param node - the listblock which first line is wanted
     * @return - returns the literal of the first line in the list
     **/
    private String getFirstLineInList(Node node) {
        Node firstChildNode = node.getFirstChild();
        // A paragraph is always the first parent of the leaf node
        boolean isLeafListItem = node instanceof Paragraph;
        String literalOfFirstChild = null;
        if (firstChildNode != null && !isLeafListItem) {
            // recursively searching the first list item and its nested lists
            literalOfFirstChild = getFirstLineInList(firstChildNode);
        } else {
            literalOfFirstChild = getLiteralOfElement(node);
        }
        return literalOfFirstChild;
    }

    /**
     * The last child represents the last list item in the list block. Then recursively check if there are
     * any child nodes of this item and get the literal of it. Recursion is needed because there could be lists with
     * different structure and formatting.
     *
     * @param node - the listblock of which last line is wanted
     * @return - returns the literal of the last line in the list
     **/
    private String getLastLineInList(Node node) {
        Node lastChildNode = node.getLastChild();
        boolean isLeafListItem = node instanceof Paragraph;
        String literalOfLastChild = null;
        if (lastChildNode != null && !isLeafListItem) {
            // recursively searching the last list item and its nested lists
            // if there is a nested code block its last line is taken
            literalOfLastChild = getLastLineInList(lastChildNode);

        } else {
            // this method is used to get the literal of leaf items or items which
            // could be nested inside typical leaf items
            literalOfLastChild = getLiteralOfElement(node);
        }
        return literalOfLastChild;
    }
}
