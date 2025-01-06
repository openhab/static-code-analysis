/*
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
package org.openhab.tools.analysis.report;

import static org.openhab.tools.analysis.report.ReportUtil.*;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to Maven executions, delegates events to the default {@link ExecutionListener} and updates HTML summaries
 * based on SAT plugin configuration values.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(role = SummaryReportExecutionListener.class)
public class SummaryReportExecutionListener extends AbstractExecutionListener {

    private Logger logger = LoggerFactory.getLogger(SummaryReportExecutionListener.class);

    @Requirement
    private SummaryReportHtmlGenerator summaryReportHtmlGenerator;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * The default ExecutionListener
     */
    private ExecutionListener listener;

    /**
     * Maps each directory for which a summary report needs to be generated to a {@link SummaryUpdater}.
     */
    private final Map<String, SummaryUpdater> summaryUpdaters = Collections.synchronizedMap(new HashMap<>());

    /**
     * Updates HTML summaries in a directory based on SAT plugin configuration values.
     */
    private class SummaryUpdater {
        private final String directory;
        private final SummaryHtmlGeneration htmlGeneration;
        private final int htmlGenerationPeriod;
        private Instant lastUpdate = Instant.MIN;

        public SummaryUpdater(String directory, SummaryHtmlGeneration htmlGeneration, int htmlGenerationPeriod) {
            this.directory = directory;
            this.htmlGeneration = htmlGeneration;
            this.htmlGenerationPeriod = htmlGenerationPeriod;
        }

        private File update() {
            File latestSummaryReport = summaryReportHtmlGenerator.generateHtmlSummaryByRules(directory);
            lastUpdate = Instant.now();
            return latestSummaryReport;
        }

        private boolean shouldUpdateIncrementally() {
            return htmlGeneration == SummaryHtmlGeneration.CONTINUOUS
                    || (htmlGeneration == SummaryHtmlGeneration.PERIODIC
                            && Instant.now().isAfter(lastUpdate.plusSeconds(htmlGenerationPeriod)));
        }

        public void incrementalUpdate() {
            // Only queue tasks that are likely to be executed by the single thread executor
            if (shouldUpdateIncrementally()) {
                executor.submit(() -> {
                    // Check again because the lastUpdate value may have been updated while waiting in the task queue
                    if (shouldUpdateIncrementally()) {
                        File latestSummaryReport = update();

                        logger.debug("Updated static code analysis summary report in:");
                        logger.debug("{}", latestSummaryReport.toURI());
                    }
                });
            }
        }

        public void finalUpdate() {
            if (htmlGeneration != SummaryHtmlGeneration.NEVER) {
                File latestSummaryReport = update();

                if (latestSummaryReport != null) {
                    logger.info("Static code analysis summary report is available in:");
                    logger.info("{}", latestSummaryReport.toURI());
                }
            }
        }
    }

    public void chainListener(MavenSession session) {
        listener = session.getRequest().getExecutionListener();
        session.getRequest().setExecutionListener(this);
    }

    private void generateSummaryReportForExecution(ExecutionEvent event) {
        try {
            getOrCreateSummaryUpdater(event).incrementalUpdate();
        } catch (ExpressionEvaluationException e) {
            logger.error("Exception while evaluating '{}' plugin parameter", DIRECTORY_PARAMETER, e);
        }
    }

    private SummaryUpdater getOrCreateSummaryUpdater(ExecutionEvent event) throws ExpressionEvaluationException {
        String directory = getPluginParameterValue(event, DIRECTORY_PARAMETER);
        SummaryUpdater summaryUpdater;
        synchronized (summaryUpdaters) {
            summaryUpdater = summaryUpdaters.get(directory);
            if (summaryUpdater == null) {
                SummaryHtmlGeneration htmlGeneration = SummaryHtmlGeneration
                        .valueOf(getPluginParameterValue(event, HTML_GENERATION_PARAMETER).toUpperCase());
                int htmlGenerationPeriod = Integer
                        .parseInt(getPluginParameterValue(event, HTML_GENERATION_PERIOD_PARAMETER));

                summaryUpdater = new SummaryUpdater(directory, htmlGeneration, htmlGenerationPeriod);
                summaryUpdaters.put(directory, summaryUpdater);
            }
        }
        return summaryUpdater;
    }

    private String getPluginParameterValue(ExecutionEvent event, String parameterName)
            throws ExpressionEvaluationException {
        Parameter parameter = event.getMojoExecution().getMojoDescriptor().getParameterMap().get(parameterName);

        PluginParameterExpressionEvaluator evaluator = getEvaluator(event);
        String parameterValue = (String) evaluator.evaluate(parameter.getExpression());
        if (parameterValue == null) {
            parameterValue = (String) evaluator.evaluate(parameter.getDefaultValue());
        }
        return parameterValue;
    }

    public void generateFinalSummaryReports() {
        synchronized (summaryUpdaters) {
            summaryUpdaters.values().forEach(SummaryUpdater::finalUpdate);
        }
    }

    private PluginParameterExpressionEvaluator getEvaluator(ExecutionEvent event) {
        PluginParameterExpressionEvaluator evaluator;
        MavenSession session = event.getSession();
        MavenProject currentProject = session.getCurrentProject();
        // Maven 3: PluginParameterExpressionEvaluator gets the current project from the session:
        // synchronize in case another thread wants to fetch the real current project in between
        synchronized (session) {
            session.setCurrentProject(event.getProject());
            evaluator = new PluginParameterExpressionEvaluator(session, event.getMojoExecution());
            session.setCurrentProject(currentProject);
        }
        return evaluator;
    }

    // These overrides make sure the original listener still receives all events

    @Override
    public void projectDiscoveryStarted(ExecutionEvent event) {
        listener.projectDiscoveryStarted(event);
    }

    @Override
    public void sessionStarted(ExecutionEvent event) {
        listener.sessionStarted(event);
    }

    @Override
    public void sessionEnded(ExecutionEvent event) {
        listener.sessionEnded(event);
    }

    @Override
    public void projectSkipped(ExecutionEvent event) {
        listener.projectSkipped(event);
    }

    @Override
    public void projectStarted(ExecutionEvent event) {
        listener.projectStarted(event);
    }

    @Override
    public void projectSucceeded(ExecutionEvent event) {
        listener.projectSucceeded(event);
    }

    @Override
    public void projectFailed(ExecutionEvent event) {
        listener.projectFailed(event);
    }

    @Override
    public void forkStarted(ExecutionEvent event) {
        listener.forkStarted(event);
    }

    @Override
    public void forkSucceeded(ExecutionEvent event) {
        listener.forkSucceeded(event);
    }

    @Override
    public void forkFailed(ExecutionEvent event) {
        listener.forkFailed(event);
    }

    @Override
    public void mojoSkipped(ExecutionEvent event) {
        listener.mojoSkipped(event);
    }

    @Override
    public void mojoStarted(ExecutionEvent event) {
        listener.mojoStarted(event);
    }

    @Override
    public void mojoSucceeded(ExecutionEvent event) {
        listener.mojoSucceeded(event);
        if (isReportExecution(event)) {
            generateSummaryReportForExecution(event);
        }
    }

    @Override
    public void mojoFailed(ExecutionEvent event) {
        listener.mojoFailed(event);
    }

    @Override
    public void forkedProjectStarted(ExecutionEvent event) {
        listener.forkedProjectStarted(event);
    }

    @Override
    public void forkedProjectSucceeded(ExecutionEvent event) {
        listener.forkedProjectSucceeded(event);
    }

    @Override
    public void forkedProjectFailed(ExecutionEvent event) {
        listener.forkedProjectFailed(event);
    }
}
