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

import org.junit.jupiter.api.Test;
import org.openhab.tools.analysis.checkstyle.DeclarativeServicesDependencyInjectionCheck;
import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheckTest;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Test for the {@link DeclarativeServicesDependencyInjectionCheck}
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class DeclarativeServicesDependencyInjectionCheckTest extends AbstractStaticCheckTest {

    private static final String SERVICE_TRACKER_USED = "Avoid using ServiceTracker for dependency injection, consider using Declarative Services";
    private static final String SERVICE_CUSTOMIZER_IMPLEMENTED = "Avoid using ServiceTrackerCustomizer for dependency injection, consider using Declarative Services";

    @Override
    protected String getPackageLocation() {
        return "checkstyle/declarativeServicesDependencyInjectionTest";
    }

    @Test
    public void testServiceTrackerInjection() throws Exception {
        String testFileName = "UseServiceTracker.java";
        int lineNumberFirst = 7;
        int lineNumberSecond = 8;

        String[] expectedMessages = generateExpectedMessages(lineNumberFirst, SERVICE_TRACKER_USED, lineNumberSecond,
                SERVICE_TRACKER_USED);

        verifyFile(testFileName, expectedMessages);
    }

    @Test
    public void testServiceTrackerCustomizer() throws Exception {
        String testFileName = "ImplementServiceTrackerCustomizer.java";
        int lineNumber = 3;

        String[] expectedMessages = generateExpectedMessages(lineNumber, SERVICE_CUSTOMIZER_IMPLEMENTED);

        verifyFile(testFileName, expectedMessages);
    }

    @Test
    public void testServiceTrackerCustomizerGenerics() throws Exception {
        String testFileName = "ImplementServiceTrackerCustomizerGenerics.java";
        int lineNumber = 4;

        String[] expectedMessages = generateExpectedMessages(lineNumber,
                DeclarativeServicesDependencyInjectionCheck.MESSAGE_SERVICE_CUSTOMIZER_IMPLEMENTED);

        verifyFile(testFileName, expectedMessages);
    }

    @Test
    public void testDeclarativeServiceUsage() throws Exception {
        String testFileName = "DeclarativeServicesUsage.java";
        verifyFile(testFileName, new String[0]);
    }

    private void verifyFile(String testFileName, String[] expectedMessages) throws Exception {
        DefaultConfiguration config = createModuleConfig(DeclarativeServicesDependencyInjectionCheck.class);

        String filePath = getPath(testFileName);

        verify(config, filePath, expectedMessages);
    }
}
