/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Handles {@link MavenSession} events so the SAT extension can generate HTML report summaries.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(role = AbstractMavenLifecycleParticipant.class)
public class SummaryReportLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    @Requirement
    private SummaryReportExecutionListener summaryReportExecutionListener;

    @Requirement
    private SummaryReportHtmlGenerator summaryReportHtmlGenerator;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        // The context class loader of the current thread needs to be used with the
        // SummaryReportHtmlGenerator or it will not find the XSLT files on the class path
        summaryReportHtmlGenerator.initialize();
        summaryReportExecutionListener.chainListener(session);
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        summaryReportExecutionListener.generateFinalSummaryReports();
    }
}
