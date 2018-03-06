/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.openhab.tools.analysis.checkstyle.api.CheckConstants;
import org.openhab.tools.analysis.utils.SatCheckUtils;

import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtils;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * Checks if a bundle uses internal types in its public API
 * 
 * @author Velin Yordanov - Initial contribution
 *
 */
public class PrivateReferencesCheck extends AbstractStaticCheck {
    private static final String LOG_MESSAGE = "Internal type usage detected in public API: Exported type %s contains internal type %s in the method definition of %s";
    private final Log logger = LogFactory.getLog(getClass());

    private Collection<String> internalTypes;
    private Map<String, Collection<String>> fullTypeNamesToImports;

    //contains the fullTypeName as a key and as value contains a map of method types as a key to the method name as value
    private Map<String, Map<Collection<String>, String>> fullTypeNamesToMethods;

    public PrivateReferencesCheck() {
        setFileExtensions(CheckConstants.JAVA_EXTENSION);
        internalTypes = new ArrayList<>();
        fullTypeNamesToImports = new Hashtable<>();
        fullTypeNamesToMethods = new Hashtable<>();
    }

    public void finishProcessing() {
        fullTypeNamesToImports.entrySet().forEach(entry -> {
            Collection<String> importsForFile = entry.getValue();
            Collection<String> internalTypesForFile = getInternalTypesForFile(importsForFile);
            Map<Collection<String>, String> methodTypesToNames = fullTypeNamesToMethods.get(entry.getKey());
            checkExternalClasses(entry.getKey(), internalTypesForFile, methodTypesToNames);
        });
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        // According to our conventions all packages that are not exported should have
        // "internal" in their name.
        // We have a check for that - PackageExportsNameCheck
        String fullTypeName = getFullTypeName(fileText);
        if (fullTypeName.contains(CheckConstants.INTERNAL)) {
            internalTypes.add(fullTypeName);
        } else {
            DetailAST fileTree = getFileTree(fileText);
            Collection<DetailAST> importAndMethodDefNodes = SatCheckUtils.getAllNodesOfType(fileTree, TokenTypes.IMPORT,
                    TokenTypes.METHOD_DEF);
            collectImportsForFile(importAndMethodDefNodes, fullTypeName);
            collectMethodTypesForFile(importAndMethodDefNodes, fullTypeName);
        }
    }

    private void collectMethodTypesForFile(Collection<DetailAST> differentNodeTypes, String fullTypeName) {
        Optional<Map<Collection<String>, String>> methods = differentNodeTypes.stream()
                .filter(x -> x.getType() == TokenTypes.METHOD_DEF)
                .map(this::getMethodTypes)
                .reduce((x, y) -> {
                    x.putAll(y);
                    return x;
                });

        fullTypeNamesToMethods.put(fullTypeName, methods.orElse(Collections.emptyMap()));
    }

    private DetailAST getFileTree(FileText fileText) {
        FileContents fileContents = new FileContents(fileText);
        DetailAST fileTree = null;
        try {
            fileTree = TreeWalker.parse(fileContents);
        } catch (RecognitionException | TokenStreamException e) {
            logger.error("Error in parsing the file " + fileContents.getFileName(), e);
        }

        return fileTree;
    }

    private void collectImportsForFile(Collection<DetailAST> differentNodeTypes, String fullTypeName) {
        Collection<String> imports = differentNodeTypes.stream()
                .filter(x -> x.getType() == TokenTypes.IMPORT)
                .map(SatCheckUtils::createFullyQualifiedName)
                .collect(Collectors.toList());

        fullTypeNamesToImports.put(fullTypeName, imports);
    }

    private String getFullTypeName(FileText fileText) {
        String packageName = Stream.of(fileText.toLinesArray())
                .filter(x -> x.startsWith("package "))
                .map(x -> x.replaceAll("(package |;)", ""))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seems like the file has no package declaration"));

        String fileName = fileText.getFile().getName();
        String typeName = fileName.substring(0, fileName.indexOf('.'));
        return packageName + "." + typeName;
    }

    private Map<Collection<String>, String> getMethodTypes(DetailAST node) {
        Map<Collection<String>, String> methodTypesToNames = new Hashtable<>();
        DetailAST modifiersNode = node.findFirstToken(TokenTypes.MODIFIERS);
        boolean hasPublicLiteral = modifiersNode.branchContains(TokenTypes.LITERAL_PUBLIC);
        boolean hasProtectedLiteral = modifiersNode.branchContains(TokenTypes.LITERAL_PROTECTED);
        if (hasPublicLiteral || hasProtectedLiteral) {
            methodTypesToNames.putAll(getMethodParameterTypes(node));
        }

        return methodTypesToNames;
    }

    private Map<Collection<String>, String> getMethodParameterTypes(DetailAST currentNode) {
        Collection<String> methodTypes = new ArrayList<>();
        String methodReturnType = currentNode.findFirstToken(TokenTypes.TYPE).getFirstChild().getText();
        String methodName = currentNode.findFirstToken(TokenTypes.IDENT).getText();
        methodTypes.add(methodReturnType);

        if (currentNode.findFirstToken(TokenTypes.PARAMETERS).branchContains(TokenTypes.PARAMETER_DEF)) {
            DetailAST parametersNode = currentNode.findFirstToken(TokenTypes.PARAMETERS);
            DetailAST parameter = parametersNode.getFirstChild();
            while (parameter != null) {
                if (parameter.findFirstToken(TokenTypes.TYPE) != null) {
                    methodTypes.add(parameter.findFirstToken(TokenTypes.TYPE).getFirstChild().getText());
                }

                parameter = parameter.getNextSibling();
            }
        }

        Map<Collection<String>, String> methodTypesToNames = new Hashtable<>();
        methodTypesToNames.put(methodTypes, methodName);
        return methodTypesToNames;
    }

    /**
     * Checks the external classes for internal types usage.
     * 
     * @param filePath
     *            - The path to the file
     * @param internalTypes
     *            - the internal types used in the file
     * @param methodTypes
     *            - the used types in the public/protected methods of the type
     */
    private void checkExternalClasses(String filePath, Collection<String> internalTypes,
            Map<Collection<String>, String> methodTypes) {
        methodTypes.keySet().forEach(methodTypesCollection -> {
            methodTypesCollection.stream()
            .filter(internalTypes::contains)
            .forEach(type -> {
                logMessage("", 0, "",
                        String.format(LOG_MESSAGE, filePath, type, methodTypes.get(methodTypesCollection)));
            });
        });
    }

    private Collection<String> getInternalTypesForFile(Collection<String> importsForFile) {
        return internalTypes.stream()
                .filter(importsForFile::contains)
                .map(x -> x.substring(x.lastIndexOf('.') + 1))
                .collect(Collectors.toList());
    }
}
