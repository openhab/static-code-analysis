/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck;

/**
 * @author Lyubomir Papazov - Initial contribution
 *
 */
public class TrailingWhitespacesCheck extends RegexpSinglelineCheck {
    public static final String TRAILING_WHITESPACE_MESSAGE = "Code lines should not end with trailing whitespace";
    private static final String REGEXP_TRAILING_WHITESPACES = "\\s+$";

    public TrailingWhitespacesCheck() {
        setMessage(TRAILING_WHITESPACE_MESSAGE);
        setFormat(REGEXP_TRAILING_WHITESPACES);
    }
}
