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
package org.openhab.tools.analysis.pmd;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.types.InvocationMatcher;

/**
 * Checks if one of the JDK provided methods which implicitly use the default time zone is used.
 * <p>
 * Not all methods are covered, only public methods that can be called directly. Complex
 * call hierarchies that end up calling a private or protected method won't be caught. Classes
 * belonging to "irrelevant" things like AWT or Swing are excluded.
 *
 * @author Ravi Nadahar - Initial contribution
 */
public class ImplicitDefaultTimeZoneRule extends AbstractJavaRulechainRule {

    private static final List<InvocationMatcher> METHODS = List.of( //
            InvocationMatcher.parse("java.util.Calendar#new()"), //
            InvocationMatcher.parse("java.util.Calendar#getInstance()"), //
            InvocationMatcher.parse("java.util.Calendar#getInstance(java.util.Locale)"), //
            InvocationMatcher.parse("javax.xml.datatype.XMLGregorianCalendar#toGregorianCalendar()"), //
            InvocationMatcher.parse("java.time.chrono.Chronology#dateNow()"), //
            InvocationMatcher.parse("java.time.chrono.HijrahChronology#dateNow()"), //
            InvocationMatcher.parse("java.time.chrono.IsoChronology#dateNow()"), //
            InvocationMatcher.parse("java.time.chrono.JapaneseChronology#dateNow()"), //
            InvocationMatcher.parse("java.time.chrono.MinguoChronology#dateNow()"), //
            InvocationMatcher.parse("java.time.chrono.ThaiBuddhistChronology#dateNow()"), //
            InvocationMatcher.parse("java.time.chrono.HijrahDate#now()"), //
            InvocationMatcher.parse("java.time.chrono.JapaneseDate#now()"), //
            InvocationMatcher.parse("java.time.chrono.MinguoDate#now()"), //
            InvocationMatcher.parse("java.time.chrono.ThaiBuddhistDate#now()"), //
            InvocationMatcher.parse("java.time.LocalDate#now()"), //
            InvocationMatcher.parse("java.time.LocalDateTime#now()"), //
            InvocationMatcher.parse("java.time.LocalTime#now()"), //
            InvocationMatcher.parse("java.time.MonthDay#now()"), //
            InvocationMatcher.parse("java.time.OffsetDateTime#now()"), //
            InvocationMatcher.parse("java.time.OffsetTime#now()"), //
            InvocationMatcher.parse("java.time.Year#now()"), //
            InvocationMatcher.parse("java.time.YearMonth#now()"), //
            InvocationMatcher.parse("java.time.ZonedDateTime#now()") //
    );

    public ImplicitDefaultTimeZoneRule() {
        super(ASTConstructorCall.class, ASTMethodCall.class);
    }

    @Override
    public String getDescription() {
        return "Methods that implicitly use the default time zone can lead to bugs. Use an overloaded version with an explicit time zone.";
    }

    @Override
    public Object visit(ASTConstructorCall node, Object data) {
        checkInvocation(node, data);
        return data;
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        checkInvocation(node, data);
        return data;
    }

    private void checkInvocation(JavaNode node, Object data) {
        for (InvocationMatcher matcher : METHODS) {
            if (matcher.matchesCall(node)) {
                asCtx(data).addViolationWithPosition(node, node.getBeginLine(), node.getEndLine(),
                        "Avoid method with implicit default time zone: {0}", node.getText());
            }
        }
    }
}
