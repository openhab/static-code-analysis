/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.utils;

import org.openhab.tools.analysis.checkstyle.readme.MarkdownCheck;

/**
 * A callback interface for {@link MarkdownCheck}
 *
 * @author Lyubomir Papazov - initial contribution
 *
 */
@FunctionalInterface
public interface LineFormatterFunction {
    /**
     * Used in AbstractStaticCheck's findLineNumber method to apply rules
     * about escaping special characters to a line in the markdown file
     *
     * @param line - an unparsed line from the markdown file
     * @return the line after formatting has been applied to it
     */
    public String formatLine(String line);
}
