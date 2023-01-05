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
package org.openhab.tools.analysis.checkstyle.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * An extension that stores all log messages appended by a logger during the execution of a test.
 *
 * @author Wouter Born - Initial contribution
 */
public class LoggedMessagesExtension implements BeforeEachCallback, AfterEachCallback {

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final Logger logger;

    public LoggedMessagesExtension(Class<?> loggerClass) {
        logger = (Logger) LoggerFactory.getLogger(loggerClass);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        logger.addAppender(listAppender);
        listAppender.start();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        listAppender.stop();
        listAppender.list.clear();
        logger.detachAppender(listAppender);
    }

    public List<String> getMessages() {
        return listAppender.list.stream().map(event -> event.getMessage()).collect(Collectors.toList());
    }

    public List<String> getFormattedMessages() {
        return listAppender.list.stream().map(event -> event.getFormattedMessage()).collect(Collectors.toList());
    }
}
