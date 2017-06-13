/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.report.test;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import org.openhab.tools.analysis.report.ReportUtility;

/**
 * Tests for the {@link ReportUtility}
 *
 * @author Svilen Valkanov
 */

public class ReportUtilityTest {

    public static final String TARGET_RELATIVE_DIR = "target" + File.separator + "test-classes" + File.separator
            + "report";
    public static final String TARGET_ABSOLUTE_DIR = System.getProperty("user.dir") + File.separator
            + TARGET_RELATIVE_DIR;
    public static final String RESULT_FILE_PATH = TARGET_ABSOLUTE_DIR + File.separator + ReportUtility.RESULT_FILE_NAME;

    @Test(expected = MojoFailureException.class)
    public void assertReportIsCreatedAndBuildFails() throws Exception {
        File file = new File(RESULT_FILE_PATH);

        if (file.exists()) {
            file.delete();
        }

        assertFalse(file.exists());

        ReportUtility utility = new ReportUtility();
        utility.setFailOnError(true);
        utility.setSummaryReport(null);
        utility.setTargetDirectory(new File(TARGET_ABSOLUTE_DIR));
        utility.execute();

        assertTrue(file.exists());
    }

}
