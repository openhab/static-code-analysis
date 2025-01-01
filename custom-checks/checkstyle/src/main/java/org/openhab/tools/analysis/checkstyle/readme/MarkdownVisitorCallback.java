/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.tools.analysis.checkstyle.readme;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;

/**
 * This Interface is used to make a callback in {@link MarkdownCheck}.
 *
 * @author Erdoan Hadzhiyusein - Initial contribution
 */
public interface MarkdownVisitorCallback {
    /**
     * This method is implemented in The {@link MarkdownCheck} class calling the protected log() of
     * {@link AbstractStaticCheck}.
     *
     * @param line line number of the logged message
     * @param message the message that is logged for the error
     */
    void log(int line, String message);
}
