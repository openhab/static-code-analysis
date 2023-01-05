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
package org.openhab.tools.analysis.pmd.test;

import org.junit.jupiter.api.BeforeEach;

import net.sourceforge.pmd.testframework.SimpleAggregatorTst;

/**
 * Test class that tests all custom PMD rules.
 *
 * @author Lyubomir Papazov - Initial contribution
 */
public class CustomRulesTest extends SimpleAggregatorTst {
    @Override
    @BeforeEach
    public void setUp() {
        addRule("pmd/ruleset/customrules.xml", "UseSLF4JLoggerRule");
    }
}
