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
 * Checks if one of the JDK provided methods which implicitly use the default locale is used.
 * <p>
 * Not all methods are covered, only public methods that can be called directly. Complex
 * call hierarchies that end up calling a private or protected method won't be caught. Classes
 * belonging to "irrelevant things" like AWT or Swing are excluded.
 *
 * @author Ravi Nadahar - Initial contribution
 */
public class ImplicitDefaultLocaleRule extends AbstractJavaRulechainRule {

    private static final List<InvocationMatcher> METHODS = List.of( //
            InvocationMatcher.parse("java.io.OutputStreamWriter#new(java.io.OutputStream)"), //
            InvocationMatcher.parse("java.io.PrintStream#format(java.lang.String,java.lang.Object[])"), //
            InvocationMatcher.parse("java.io.PrintWriter#format(java.lang.String,java.lang.Object[])"), //
            InvocationMatcher.parse("java.lang.String#toLowerCase()"), //
            InvocationMatcher.parse("java.lang.String#toUpperCase()"), //
            InvocationMatcher.parse("java.text.BreakIterator#getWordInstance()"), //
            InvocationMatcher.parse("java.text.BreakIterator#getCharacterInstance()"), //
            InvocationMatcher.parse("java.text.BreakIterator#getSentenceInstance()"), //
            InvocationMatcher.parse("java.text.BreakIterator#getLineInstance()"), //
            InvocationMatcher.parse("java.text.Collator#getInstance()"), //
            InvocationMatcher.parse("java.text.DateFormat#getTimeInstance()"), //
            InvocationMatcher.parse("java.text.DateFormat#getTimeInstance(int)"), //
            InvocationMatcher.parse("java.text.DateFormatSymbols#new()"), //
            InvocationMatcher.parse("java.text.DateFormatSymbols#getInstance()"), //
            InvocationMatcher.parse("java.text.DecimalFormat#new()"), //
            InvocationMatcher.parse("java.text.DecimalFormat#new(java.lang.String)"), //
            InvocationMatcher.parse("java.text.DecimalFormatSymbols#new()"), //
            InvocationMatcher.parse("java.text.DecimalFormatSymbols#getInstance()"), //
            InvocationMatcher.parse("java.text.MessageFormat#new(java.lang.String)"), //
            InvocationMatcher.parse("java.text.NumberFormat#getInstance()"), //
            InvocationMatcher.parse("java.text.NumberFormat#getNumberInstance()"), //
            InvocationMatcher.parse("java.text.NumberFormat#getIntegerInstance()"), //
            InvocationMatcher.parse("java.text.NumberFormat#getCurrencyInstance()"), //
            InvocationMatcher.parse("java.text.NumberFormat#getPercentInstance()"), //
            InvocationMatcher.parse("java.text.NumberFormat#getScientificInstance()"), //
            InvocationMatcher.parse("java.text.SimpleDateFormat#new()"), //
            InvocationMatcher.parse("java.text.SimpleDateFormat#new(java.lang.String)"), //
            InvocationMatcher.parse("java.text.SimpleDateFormat#new(java.lang.String,java.text.DateFormatSymbols)"), //
            InvocationMatcher.parse("java.time.format.DateTimeFormatterBuilder#toFormatter()"), //
            InvocationMatcher.parse("java.util.Calendar#new()"), //
            InvocationMatcher.parse("java.util.Calendar#getInstance()"), //
            InvocationMatcher.parse("java.util.Calendar#getInstance(java.util.TimeZone)"), //
            InvocationMatcher.parse("java.util.Currency#getSymbol()"), //
            InvocationMatcher.parse("java.util.Currency#getDisplayName()"), //
            InvocationMatcher.parse("java.util.Formatter#new()"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.lang.Appendable)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.lang.String)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.lang.String,java.lang.String)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.io.File)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.io.File,java.lang.String)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.io.PrintStream)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.io.OutputStream)"), //
            InvocationMatcher.parse("java.util.Formatter#new(java.io.OutputStream,java.lang.String)"), //
            InvocationMatcher.parse("java.util.GregorianCalendar#new()"), //
            InvocationMatcher.parse("java.util.GregorianCalendar#new(java.util.TimeZone)"), //
            InvocationMatcher.parse("java.util.ResourceBundle#getBundle(java.lang.String)"), //
            InvocationMatcher
                    .parse("java.util.ResourceBundle#getBundle(java.lang.String,java.util.ResourceBundle.Control)"), //
            InvocationMatcher.parse("java.util.ResourceBundle#getBundle(java.lang.String,java.lang.Module)"), //
            InvocationMatcher.parse("java.util.TimeZone#getDisplayName()"), //
            InvocationMatcher.parse("java.util.TimeZone#getDisplayName(boolean,int)"), //
            InvocationMatcher.parse("javax.accessibility.AccessibleBundle#toDisplayString()"), //
            InvocationMatcher.parse("javax.xml.datatype.XMLGregorianCalendar#toGregorianCalendar()") //
    );

    public ImplicitDefaultLocaleRule() {
        super(ASTConstructorCall.class, ASTMethodCall.class);
    }

    @Override
    public String getDescription() {
        return "Methods that implicitly use the default Locale can lead to bugs. Use an overloaded version with an explicit Locale, and specify 'Locale.getDefault()' if the default Locale is desired.";
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
                        "Avoid method with implicit default Locale: {0}", node.getText());
            }
        }
    }
}
