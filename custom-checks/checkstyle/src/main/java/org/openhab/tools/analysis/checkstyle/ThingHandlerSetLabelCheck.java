/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.JAVA_EXTENSION;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

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
 * Checks the code and generates a warning if a class extends BaseThingHandler
 * and the instance method setLabel of the Thing interface is used.
 * The label have to be set only by the user.
 *
 * @author Tanya Georgieva - Initial Contribution
 */
public class ThingHandlerSetLabelCheck extends AbstractStaticCheck {

    private static final String WARNING_MESSAGE = "Do not use the setLabel of the thing in the ThingHandler";
    private static final String BASE_THING_HANDLER = "BaseThingHandler";
    private static final String SEARCHED_PACKAGE = "org.eclipse.smarthome.core.thing.binding." + BASE_THING_HANDLER;
    private static final String SET_LABEL = "setLabel";
    private static final String THING = "thing";
    private static final String UNKNOWN = "UNKNOWN";

    private final Log logger = LogFactory.getLog(ThingHandlerSetLabelCheck.class);

    private String fullJavaExtension = "\\." + JAVA_EXTENSION;

    private HashMap<File, DetailAST> files;
    private HashMap<File, String> derivedAndBaseClasses;
    private HashMap<File, DetailAST> classesExtendingBaseThingHandler;

    public ThingHandlerSetLabelCheck() {
        setFileExtensions(JAVA_EXTENSION);
    }

    @Override
    public void init() {
        files = new HashMap<>();
        derivedAndBaseClasses = new HashMap<>();
        classesExtendingBaseThingHandler = new HashMap<>();
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {
        FileContents fileContents = new FileContents(fileText);
        DetailAST rootAST = null;

        try {
            rootAST = TreeWalker.parse(fileContents);
        } catch (RecognitionException | TokenStreamException e) {
            logger.error("Parsing file not successful");
            e.printStackTrace();
        }
        files.put(file, rootAST);
    }

    @Override
    public void finishProcessing() {
        findDerivedAndBaseClasses();
        findClassesExtendingBaseThingHandler();

        classesExtendingBaseThingHandler.forEach((file, rootAST) -> {
            String fileName = file.getName();
            Path filePath = file.toPath();
            checkSetLabelMethodCall(rootAST, fileName, filePath);
        });
    }

    // Pre-order traversal - starting from the root first
    private void checkSetLabelMethodCall(DetailAST nodeAST, String fileName, Path filePath) {
        if (nodeAST == null) {
            return;
        }

        if (THING.equals(nodeAST.getText())) {
            // thing.setLabel(...)
            boolean isThingFieldSetLabelMethodCalled = nodeAST.getNextSibling() != null
                    && SET_LABEL.equals(nodeAST.getNextSibling().getText());
            // when literal THIS is used the setLabel method is sibling of the parent of the node - thing
            // this.thing.setLabel(...)
            boolean isReferredThingFieldSetLabelMethodCalled = nodeAST.getParent().getNextSibling() != null
                    && SET_LABEL.equals(nodeAST.getParent().getNextSibling().getText());
            boolean isSetLabelCalled = isThingFieldSetLabelMethodCalled || isReferredThingFieldSetLabelMethodCalled;

            if (isSetLabelCalled) {
                int nodeLineNumber = nodeAST.getLineNo();
                String fileStringPath = filePath.toString();
                logMessage(fileStringPath, nodeLineNumber, fileName, WARNING_MESSAGE);
            }
        }
        checkSetLabelMethodCall(nodeAST.getFirstChild(), fileName, filePath);
        checkSetLabelMethodCall(nodeAST.getNextSibling(), fileName, filePath);
    }

    public void findClassesExtendingBaseThingHandler() {
        Queue<File> visitedFiles = new LinkedList<>();
        File searchedPackageFile = new File(SEARCHED_PACKAGE);
        visitedFiles.add(searchedPackageFile);

        while (!visitedFiles.isEmpty()) {
            File currentFile = visitedFiles.poll();
            String currentFileParent = currentFile.getParent();
            String currentFileClassName = currentFile.getName().replaceFirst(fullJavaExtension, "");

            derivedAndBaseClasses.forEach((file, baseClassName) -> {
                String fileParent = file.getParent();
                // Check if the current class and it's base class are from the same package.
                // To find first the classes extending BaseThingHandler directly,
                // we set the first file to be the SEARCHED PACKAGE,
                // it's parent is null so the check is for the currentFileClassName
                boolean areFilesFromSamePackage = SEARCHED_PACKAGE.equals(currentFileClassName)
                        || currentFileParent.equals(fileParent);

                if (currentFileClassName.equals(baseClassName) && areFilesFromSamePackage) {
                    DetailAST fileRootAST = files.get(file);
                    classesExtendingBaseThingHandler.put(file, fileRootAST);
                    visitedFiles.add(file);
                }
            });
        }
    }

    private void findDerivedAndBaseClasses() {
        files.forEach((file, rootAST) -> {
            Optional<String> fullyQualifiedName = getFullyQualifiedName(rootAST);
            Optional<String> baseClassName = getBaseClassName(rootAST);
            String currentFullName = fullyQualifiedName.orElse(UNKNOWN);
            String currentBaseName = baseClassName.orElse(UNKNOWN);
            // if the currentFullName is not UNKNOWN our SEARCHED PACKAGE is found for the current class
            boolean isHandlerImportsBaseThingHanlderPackage = !UNKNOWN.equals(currentFullName);
            // check for case where the SEARCHED PACKAGE is imported but not extended
            boolean isHandlerExtendingBaseThingHandler = !UNKNOWN.equals(currentBaseName)
                    && BASE_THING_HANDLER.equals(currentBaseName);

            if (isHandlerImportsBaseThingHanlderPackage && isHandlerExtendingBaseThingHandler) {
                derivedAndBaseClasses.put(file, currentFullName);
            } else {
                derivedAndBaseClasses.put(file, currentBaseName);
            }
        });
    }

    private Optional<String> getFullyQualifiedName(DetailAST rootAST) {
        Queue<DetailAST> visitedNodes = new LinkedList<>();
        visitedNodes.add(rootAST);

        while (!visitedNodes.isEmpty()) {
            DetailAST currentNode = visitedNodes.poll();
            String currentFullName = CheckUtils.createFullType(currentNode).getText();

            if (SEARCHED_PACKAGE.equals(currentFullName)) {
                return Optional.of(SEARCHED_PACKAGE);
            } else {
                if (currentNode.getNextSibling() != null) {
                    visitedNodes.add(currentNode.getNextSibling());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> getBaseClassName(DetailAST rootAST) {
        Queue<DetailAST> visitedNodes = new LinkedList<>();
        visitedNodes.add(rootAST);

        while (!visitedNodes.isEmpty()) {
            DetailAST currentNode = visitedNodes.poll();

            if (currentNode.getType() == TokenTypes.CLASS_DEF) {
                DetailAST extendsClause = currentNode.findFirstToken(TokenTypes.EXTENDS_CLAUSE);

                if (extendsClause != null) {
                    String inheritedClassName = extendsClause.getFirstChild().getText();
                    // Only the simple name of the class can be retrieved from the extends clause
                    return Optional.of(inheritedClassName);
                }
            } else {
                if (currentNode.getNextSibling() != null) {
                    visitedNodes.add(currentNode.getNextSibling());
                }
            }
        }
        return Optional.empty();
    }
}
