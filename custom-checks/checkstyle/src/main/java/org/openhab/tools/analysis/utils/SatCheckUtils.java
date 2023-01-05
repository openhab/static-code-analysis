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
package org.openhab.tools.analysis.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;

/**
 * Class that contains utility methods for checks.
 *
 * @author Kristina Simova - Initial contribution
 * @author Tanya Georgieva - Added methods that are looking for children nodes of a given type
 */
public final class SatCheckUtils {

    /**
     * Creates a new FullIdent starting from the given node and returns the fully qualified name as a String.
     *
     * @param typeAST a type node
     * @return the fully qualified name as a String
     */
    public static String createFullyQualifiedName(DetailAST typeAST) {
        return CheckUtil.createFullType(typeAST).getText();
    }

    /**
     * Method that returns the first (direct or indirect) child node of a given type or null if such does not exist
     *
     * @param ast the ast
     * @param type the type of child node we are looking for
     * @return the first child node of a given type or null if such does not exist
     */
    public static DetailAST getFirstNodeOfType(DetailAST ast, int type) {
        if (ast == null) {
            return null;
        }
        if (ast.getType() == type) {
            return ast;
        }
        DetailAST nextNode = getFirstNodeOfType(ast.getFirstChild(), type);
        DetailAST nextSibling = getFirstNodeOfType(ast.getNextSibling(), type);
        // if the current node does not have a child node return its sibling
        return nextNode != null ? nextNode : nextSibling;
    }

    /**
     * Method that searches for all direct children nodes of a given type and returns them
     *
     * @param ast the ast
     * @param type the type of children nodes we are looking for
     * @return list of the ast's children of a given type
     */
    public static List<DetailAST> getAllChildrenNodesOfType(DetailAST ast, int type) {
        LinkedList<DetailAST> childrenNodes = new LinkedList<>();
        Queue<DetailAST> visitedNodes = new LinkedList<>();
        visitedNodes.add(ast.getFirstChild());

        while (!visitedNodes.isEmpty()) {
            DetailAST currentNode = visitedNodes.poll();
            if (currentNode.getType() == type) {
                childrenNodes.add(currentNode);
            }
            DetailAST nodeNextSibling = currentNode.getNextSibling();
            if (nodeNextSibling != null) {
                visitedNodes.add(nodeNextSibling);
            }
        }
        return childrenNodes;
    }

    /**
     * Method that searches for all direct and indirect children nodes of a given type and returns them
     *
     * @param requiredNodes list of the required nodes
     * @param ast the ast
     * @param type the token type to match
     * @return list of the required nodes
     */
    public static List<DetailAST> getAllNodesOfType(LinkedList<DetailAST> requiredNodes, DetailAST ast, int type) {
        if (ast == null) {
            return new LinkedList<>();
        }
        if (ast.getType() == type) {
            requiredNodes.add(ast);
        }
        getAllNodesOfType(requiredNodes, ast.getFirstChild(), type);
        getAllNodesOfType(requiredNodes, ast.getNextSibling(), type);
        return requiredNodes;
    }
}
