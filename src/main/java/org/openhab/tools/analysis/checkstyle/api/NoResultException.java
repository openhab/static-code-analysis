/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.api;

/**
 * Exception that indicates that a line number was not found in a file
 *
 * @author Svilen Valkanov
 */
public class NoResultException extends Exception {

    public NoResultException(String message) {
        super(message);
    }

    public NoResultException() {
        // super constructor call
    }
}
