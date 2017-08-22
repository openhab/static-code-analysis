/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.readme;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;

/**
 * This visitor is used in the {@link MarkdownVisitor} to get the count of list items in a Markdown list.
 * 
 * @author Erdoan Hadzhiyusein - Initial contribution
 */
public class MarkdownListVisitor extends AbstractVisitor {
    private int listLenght;

    /**
     * Each list has ListItem nodes and it is usually enough to know the count of them.
     * This of course doesn't guarantee finding the exact size of the list because there can be multiline list items,
     * but it will give an approximate lineNumber where to expect the end of the list.
     * (For the cases when the last line's literal can be found in the list earlier.)
     */
    @Override
    public void visit(ListItem listItem) {
        this.listLenght++;
        // using lastChild because if there is a nested list it will be represented as last child node of the ListItem
        Node lastChildNode = listItem.getLastChild();
        if (lastChildNode instanceof ListBlock) {
            // if there is a nested list in the current list the visitor visits it too
            lastChildNode.accept(this);
        }
    }

    public int getListLenght() {
        return listLenght;
    }
}
