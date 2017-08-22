/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.readme;

import java.util.List;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

/**
 * This Interface is used to make a callback in {@link MarkdownCheck}.
 * 
 * @author Erdoan Hadzhiyusein - Initial contribution
 */
public interface MarkdownVisitorCallback {
    /**
     * This method is implemented in The {@link MarkdownCheck} class calling the protected findLineNumber() of
     * {@link AbstractStaticCheck}.
     */
    public int findLineNumber(List<String> fileContent, String searchedText, int startLineNumber);

    /**
     * This method is implemented in The {@link MarkdownCheck} class calling the protected log() of
     * {@link AbstractStaticCheck}.
     */
    public void log(int line, String message);
}
