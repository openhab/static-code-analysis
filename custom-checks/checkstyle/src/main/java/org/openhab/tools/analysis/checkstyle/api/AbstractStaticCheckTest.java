/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.tools.analysis.checkstyle.api;

import java.io.File;
import java.util.ArrayList;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;

/**
 * Base test class for static code analysis checks
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Petar Valchev - Implement method to get the path in the expected format from checkstyle
 */
public abstract class AbstractStaticCheckTest extends AbstractModuleTestSupport {

    /**
     * Generates message that can be used in the
     * {@link AbstractModuleTestSupport} verify methods.
     *
     * @param arguments a set of line number and message pairs
     * @return String[] in the format used from checkstyle to verify the logged messages
     */
    protected String[] generateExpectedMessages(Object... arguments) {
        int messageNumber = arguments.length / 2;
        String[] messages = new String[messageNumber];

        for (int i = 0; i < messageNumber; i++) {
            Object lineNum = arguments[2 * i];
            Object message = arguments[2 * i + 1];
            messages[i] = lineNum + ": " + message;
        }
        return messages;
    }

    /**
     * Lists all the files from the current directory and its subdirectories.
     *
     * @param directory the directory which files will be returned
     * @param files list where the found files will be stored
     * @return File[] with the files in the directory and its subdirectories
     */
    protected File[] listFilesForDirectory(File directory, ArrayList<File> files) {
        for (File fileEntry : directory.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForDirectory(fileEntry, files);
            } else {
                files.add(fileEntry);
            }
        }
        return files.toArray(new File[] {});
    }
}
