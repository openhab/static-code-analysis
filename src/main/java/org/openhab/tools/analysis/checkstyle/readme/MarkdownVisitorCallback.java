/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.readme;

import java.util.function.Function;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.openhab.tools.analysis.checkstyle.api.NoResultException;
import org.openhab.tools.analysis.utils.LineFormatterFunction;

import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * This Interface is used to make a callback in {@link MarkdownCheck}.
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 */
public interface MarkdownVisitorCallback {
    /**
     * This method is implemented in The {@link MarkdownCheck} class calling the protected findLineNumber() of
     * {@link AbstractStaticCheck}.
     *
     * @param fileContent - the file content represented in a list
     * @param searchedText - the searched text in the source
     * @param startLineNumber - the line number to start the search from
     * @param lineFormatterFunction - a formatting function to apply to every line
     * @throws NoResultException when no match was found
     * @return - returns the line number in the source file
     */
    public int findLineNumber(FileText fileContent, String searchedText, int startLineNumber, LineFormatterFunction lineFormatterFunction) throws NoResultException;

    /**
     * This method is implemented in The {@link MarkdownCheck} class calling the protected log() of
     * {@link AbstractStaticCheck}.
     *
     * @param line - line number of the logged message
     * @param message - the message that is logged for the error
     */
    public void log(int line, String message);
}
