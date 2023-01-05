/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.tools.analysis.report;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.plugin.MojoExecution;

/**
 * Provides constants and utility methods used for generating reports.
 *
 * @author Wouter Born - Initial contribution
 */
final class ReportUtil {

    // SAT plugin GAV parameters
    static final String SAT_PLUGIN_GROUP_ID = "org.openhab.tools.sat";
    static final String SAT_PLUGIN_ARTIFACT_ID = "sat-plugin";
    static final String SAT_PLUGIN_REPORT_GOAL = "report";

    // SAT plugin configuration parameter names
    static final String DIRECTORY_PARAMETER = "summaryReportDirectory";
    static final String HTML_GENERATION_PARAMETER = "summaryHtmlGeneration";
    static final String HTML_GENERATION_PERIOD_PARAMETER = "summaryHtmlGenerationPeriod";

    // XSLT files that are used to create the merged report, located in the resources folder
    static final String REPORT_SUBDIR = "report";
    static final String CREATE_HTML_XSLT = REPORT_SUBDIR + "/create_html.xslt";
    static final String MERGE_XSLT = REPORT_SUBDIR + "/merge.xslt";
    static final String PREPARE_PMD_XSLT = REPORT_SUBDIR + "/prepare_pmd.xslt";
    static final String PREPARE_CHECKSTYLE_XSLT = REPORT_SUBDIR + "/prepare_checkstyle.xslt";
    static final String PREPARE_FINDBUGS_XSLT = REPORT_SUBDIR + "/prepare_findbugs.xslt";

    static final String SUMMARY_TEMPLATE_FILE_NAME = "summary.html";

    // Input files that contain the reports of the different tools
    static final String PMD_INPUT_FILE_NAME = "pmd.xml";
    static final String CHECKSTYLE_INPUT_FILE_NAME = "checkstyle-result.xml";
    static final String FINDBUGS_INPUT_FILE_NAME = "spotbugsXml.xml";

    // Name of the file that contains the merged report
    static final String RESULT_FILE_NAME = "report.html";
    static final String SUMMARY_REPORT_FILE_NAME = "summary_report.html";
    static final String SUMMARY_BUNDLES_FILE_NAME = "summary_bundles.html";
    static final String SUMMARY_XML_FILE_NAME = "summary.xml";
    static final String EMPTY = "";

    // Files used for merging the individual reports into the summary reports
    static final String MERGE_XML_FILE_NAME = "merge.xml";
    static final String MERGE_XML_TMP_FILE_NAME = "merge.xml.tmp";

    // The lock used for updating merge files
    private static final ReentrantLock MERGE_LOCK;
    private static final String MERGE_LOCK_KEY_NAME = ReportUtil.class.getCanonicalName() + ".MERGE_LOCK";

    // The lock used for updating summary files
    private static final ReentrantLock SUMMARY_LOCK;
    private static final String SUMMARY_LOCK_KEY_NAME = ReportUtil.class.getCanonicalName() + ".SUMMARY_LOCK";

    private ReportUtil() {
        // Hidden utility class constructor
    }

    static {
        synchronized (ClassLoader.getSystemClassLoader()) {
            MERGE_LOCK = getOrCreateJvmSingletonLock(MERGE_LOCK_KEY_NAME);
            SUMMARY_LOCK = getOrCreateJvmSingletonLock(SUMMARY_LOCK_KEY_NAME);
        }
    }

    private static ReentrantLock getOrCreateJvmSingletonLock(String keyName) {
        ReentrantLock lock = (ReentrantLock) System.getProperties().get(keyName);
        if (lock == null) {
            lock = new ReentrantLock();
            System.getProperties().put(keyName, lock);
        }
        return lock;
    }

    static void acquireMergeLock() {
        MERGE_LOCK.lock();
    }

    static void releaseMergeLock() {
        if (MERGE_LOCK.isHeldByCurrentThread()) {
            MERGE_LOCK.unlock();
        }
    }

    static void acquireSummaryLock() {
        SUMMARY_LOCK.lock();
    }

    static void releaseSummaryLock() {
        if (SUMMARY_LOCK.isHeldByCurrentThread()) {
            SUMMARY_LOCK.unlock();
        }
    }

    static boolean isReportExecution(ExecutionEvent event) {
        MojoExecution execution = event.getMojoExecution();
        return SAT_PLUGIN_GROUP_ID.equals(execution.getGroupId())
                && SAT_PLUGIN_ARTIFACT_ID.equals(execution.getArtifactId())
                && SAT_PLUGIN_REPORT_GOAL.equals(execution.getGoal());
    }
}
