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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.openhab.tools.analysis.report.ReportUtil.RESULT_FILE_NAME;

import java.io.File;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link ReportMojo}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Martin van Wingerden - added logging of all messages
 */
@ExtendWith(MockitoExtension.class)
public class ReportMojoTest {

    private static final String TARGET_RELATIVE_DIR = "target" + File.separator + "test-classes" + File.separator
            + "report";
    private static final String TARGET_ABSOLUTE_DIR = System.getProperty("user.dir") + File.separator
            + TARGET_RELATIVE_DIR;
    private static final String RESULT_FILE_PATH = TARGET_ABSOLUTE_DIR + File.separator + RESULT_FILE_NAME;

    private @Mock Log logger;

    private ReportMojo subject;

    private File resultFile = new File(RESULT_FILE_PATH);

    @BeforeEach
    public void setUp() throws Exception {
        subject = new ReportMojo();
        subject.setLog(logger);

        if (resultFile.exists()) {
            resultFile.delete();
        }
    }

    @ParameterizedTest
    @MethodSource("provideReportParameters")
    public void assertReportIsCreatedAndBuildFailsAsExpected(boolean failOnError, boolean failOnWarning,
            boolean failOnInfo, boolean buildFailExpected) {
        assertFalse(resultFile.exists());

        subject.setFailOnError(failOnError);
        subject.setFailOnWarning(failOnWarning);
        subject.setFailOnInfo(failOnInfo);
        subject.setSummaryReport(null);
        subject.setTargetDirectory(new File(TARGET_ABSOLUTE_DIR));

        if (buildFailExpected) {
            assertThrows(MojoFailureException.class, () -> subject.execute());
        } else {
            assertDoesNotThrow(() -> subject.execute());

        }
        assertTrue(resultFile.exists());
    }

    private static Stream<Arguments> provideReportParameters() {
        return Stream.of(arguments(false, false, false, false), arguments(false, false, true, true),
                arguments(false, true, false, true), arguments(false, true, true, true),
                arguments(true, false, false, true), arguments(true, false, true, true),
                arguments(true, true, false, true), arguments(true, true, true, true));
    }

    @Test
    public void assertWarningAreLoggedWhileExecuting() throws MojoFailureException {
        assertFalse(resultFile.exists());

        subject.setFailOnError(false);
        subject.setSummaryReport(null);
        subject.setTargetDirectory(new File(TARGET_ABSOLUTE_DIR));
        subject.setReportInMaven(true);

        subject.execute();

        String nl = System.lineSeparator();

        verify(logger).warn("org.sprunck.bee.Bee.java:[31]" + nl
                + "org.sprunck.bee.Bee defines clone() but doesn't implement Cloneable");
        verify(logger).warn("org.sprunck.bee.Bee.java:[31]" + nl + "org.sprunck.bee.Bee.clone() may return null");
        verify(logger).warn("org.sprunck.foo.Foo.java:[35]" + nl
                + "The method name org.sprunck.foo.Foo.Went() doesn't start with a lower case letter");
        verify(logger).error(
                "Code Analysis Tool has found: " + nl + " 2 error(s)! " + nl + " 3 warning(s) " + nl + " 3 info(s)");
        verify(logger).error("org.sprunck.bee.Bee.java:[19]" + nl
                + "org.sprunck.bee.Bee.toString() ignores return value of String.concat(String)");
        verify(logger).error("org.openhab.core.auth.jaas.internal.JaasAuthenticationProvider.java:[69]" + nl
                + "Comment matches to-do format '(TODO)|(FIXME)'.");
        verify(logger).debug("org.sprunck.bee.Bee.java:[19]" + nl
                + "An operation on an Immutable object (String, BigDecimal or BigInteger) won't change the object itself");
        verify(logger).debug(
                "org.sprunck.foo.Foo.java:[36]" + nl + "Do not use if statements that are always true or always false");
        verify(logger).debug(
                ".core.automation.module.core\\OH-INF\\automation\\moduletypes\\EventTriggersTypeDefinition.json:[0]"
                        + nl + "File does not end with a newline.");
        verify(logger).info("Detailed report can be found at: " + new File(RESULT_FILE_PATH).toURI());
    }
}
