/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.api;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * 
 * @author Velin Yordanov - initial contribution
 *
 */
public abstract class MeasuredAbstractCheck extends AbstractCheck {
    private long startTime;
    private long methodTime;
    private Log logger = LogFactory.getLog(MeasuredAbstractCheck.class);

    public void beginTree(DetailAST rootAST) {
        startTime = System.nanoTime();
    }

    public void finishTree(DetailAST rootAST) {
        long endTime = System.nanoTime();
        methodTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        try (FileWriter writer = new FileWriter("measurements.txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.append(this.getClass().getSimpleName() + " : " + methodTime);
            bufferedWriter.newLine();
        } catch (IOException e) {
            logger.error("Error in writing to or creating measurements.txt", e);
        }
    }
}
