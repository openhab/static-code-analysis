/**
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
package org.openhab.tools.analysis.checkstyle;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.tools.analysis.checkstyle.api.AbstractOhInfXmlCheck;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Checks if all words in a label start with an uppercase character and if labels are not to long.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class OhInfXmlLabelCheck extends AbstractOhInfXmlCheck {

    private static final String CONFIG_ATTR_KEY = "name";
    private static final String THING_ATTR_KEY = "id";

    private static final String I18N_PREFIX = "@text/";

    private static final String PARAMETER_LABEL_EXPRESSION = "//parameter/label/text()";
    private static final String PARAMETER_GROUP_LABEL_EXPRESSION = "//parameter-group/label/text()";

    private static final String CHANNEL_LABEL_EXPRESSION = "//channel/label/text()";
    private static final String CHANNEL_GROUP_LABEL_EXPRESSION = "//channel-group/label/text()";
    private static final String CHANNEL_TYPE_LABEL_EXPRESSION = "//channel-type/label/text()";
    private static final String CHANNEL_GROUP_TYPE_LABEL_EXPRESSION = "//channel-group-type/label/text()";
    private static final String THING_TYPE_LABEL_EXPRESSION = "//thing-type/label/text()";

    /**
     * If any of these characters appear in the word when the first character is lowercase the first character should
     * not be reported to be changed to uppercase. This is to handle exceptional cases, like brand names, words with
     * numbers or units between brackets, e.g. (ms).
     */
    private static final Pattern SKIP_PATTERN = Pattern.compile("[A-Z\\d+\\.\\(\\)]");

    private static final List<String> ALL_CONFIG_EXPRESSIONS = Arrays.asList(PARAMETER_LABEL_EXPRESSION,
            PARAMETER_GROUP_LABEL_EXPRESSION);
    private static final List<String> ALL_THING_EXPRESSIONS = Arrays.asList(THING_TYPE_LABEL_EXPRESSION,
            CHANNEL_LABEL_EXPRESSION, CHANNEL_GROUP_LABEL_EXPRESSION, CHANNEL_TYPE_LABEL_EXPRESSION,
            CHANNEL_GROUP_TYPE_LABEL_EXPRESSION);
    private static final List<String> ALL_THING_CONFIG_EXPRESSIONS = Collections
            .singletonList(PARAMETER_LABEL_EXPRESSION);

    private static final Pattern TYPE_PATTERN = Pattern.compile("//([^\\/]+)");
    private static final Pattern LABEL_PATTERN = Pattern.compile("<label>([^<]+)</label>");

    public static final String MESSAGE_LABEL_UPPERCASE = "Label of {0} with {1} ''''{2}'''' does not have uppercase first character for each word: ''''{3}''''";
    public static final String MESSAGE_MAX_LABEL_LENGTH = "Label of {0} with {1} ''''{2}'''' exceeds maximum length of %d characters with length {4}: ''''{3}''''";

    public static final Set<String> LOWER_CASE_WORDS = Stream
            .of("a", "an", "the", "and", "as", "but", "by", "for", "from", "in", "into", "like", "near", "nor", "of",
                    "onto", "or", "out", "over", "past", "so", "till", "to", "up", "upon", "with", "yet")
            .collect(Collectors.toSet());

    private int maxLabelLength = Integer.MAX_VALUE;
    private int maxLabelLengthError = Integer.MAX_VALUE;
    private boolean doCheckWordCasing;
    private String dynamicMessageMaxLabelLength = "";
    private String dynamicMessageMaxLabelLengthError = "";

    /**
     * Sets the configuration property for the max label length.
     *
     * @param maxLabelLength max length a label may have
     */
    public void setMaxLabelLength(final String maxLabelLength) {
        this.maxLabelLength = maxLabelLength == null ? Integer.MAX_VALUE : Integer.parseInt(maxLabelLength);
        dynamicMessageMaxLabelLength = String.format(MESSAGE_MAX_LABEL_LENGTH, this.maxLabelLength);
    }

    /**
     * Sets the configuration property for the max label length.
     *
     * @param maxLabelLength max length a label may have
     */
    public void setMaxLabelLengthError(final String maxLabelLength) {
        this.maxLabelLengthError = maxLabelLength == null ? Integer.MAX_VALUE : Integer.parseInt(maxLabelLength);
        dynamicMessageMaxLabelLengthError = String.format(MESSAGE_MAX_LABEL_LENGTH, this.maxLabelLengthError);
    }

    public void setCheckWordCasing(final String check) {
        this.doCheckWordCasing = Boolean.parseBoolean(check);
    }

    @Override
    protected void checkConfigFile(final FileText xmlFileText) throws CheckstyleException {
        for (final String expression : ALL_CONFIG_EXPRESSIONS) {
            evaluateExpressionOnFile(xmlFileText, expression, CONFIG_ATTR_KEY);
        }
    }

    @Override
    protected void checkAddonFile(final FileText xmlFileText) throws CheckstyleException {
        // No labels in binding files.
    }

    @Override
    protected void checkThingTypeFile(final FileText xmlFileText) throws CheckstyleException {
        for (final String expression : ALL_THING_EXPRESSIONS) {
            evaluateExpressionOnFile(xmlFileText, expression, THING_ATTR_KEY);
        }
        for (final String expression : ALL_THING_CONFIG_EXPRESSIONS) {
            evaluateExpressionOnFile(xmlFileText, expression, CONFIG_ATTR_KEY);
        }
    }

    private void evaluateExpressionOnFile(final FileText xmlFileText, final String xPathExpression, final String key)
            throws CheckstyleException {
        final String type = filterType(xPathExpression);
        final Map<String, Integer> lineNumberMap = new HashMap<>();
        final NodeList nodes = getNodes(xmlFileText, xPathExpression);
        final File file = xmlFileText.getFile();

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                final String labelText = nodes.item(i).getNodeValue();
                if (noI18NLabel(labelText)) {
                    checkWordCasing(xmlFileText, lineNumberMap, key, type, nodes.item(i), file, labelText);
                    checkLabelLength(xmlFileText, lineNumberMap, key, type, nodes.item(i), file, labelText);
                }
            }
        }
    }

    /**
     * Check if the label is an i18n key. If such it should not be checked for length.
     *
     * @param labelText label text to check
     * @return true if no i18n label
     */
    private boolean noI18NLabel(String labelText) {
        return !labelText.startsWith(I18N_PREFIX);
    }

    private void checkWordCasing(final FileText xmlFileText, final Map<String, Integer> lineNumberMap, final String key,
            final String type, final Node node, final File file, final String labelText) {
        if (!doCheckWordCasing) {
            return;
        }
        final String[] words = labelText.split("\\s+");

        for (int i = 0; i < words.length; i++) {
            final String word = words[i];

            final int firstCharType = Character.getType(word.charAt(0));
            final boolean lowerCase = firstCharType == Character.LOWERCASE_LETTER;
            final boolean firstOrLastWord = i == 0 || i == words.length - 1;
            boolean log = false;

            if (lowerCase) {
                if ((firstOrLastWord || !LOWER_CASE_WORDS.contains(word)) && !SKIP_PATTERN.matcher(word).find()) {
                    log = true;
                }
            } else if (!firstOrLastWord && LOWER_CASE_WORDS.contains(word)
                    && firstCharType == Character.UPPERCASE_LETTER) {
                log = true;
            }
            if (log) {
                log(xmlFileText, lineNumberMap, MESSAGE_LABEL_UPPERCASE, key, type, node, file, labelText);
                return;
            }
        }
    }

    private void checkLabelLength(final FileText xmlFileText, final Map<String, Integer> lineNumberMap,
            final String key, final String type, final Node node, final File file, final String labelText) {
        if (labelText.length() > maxLabelLengthError) {
            final SeverityLevel configuredSeverityLevel = getSeverityLevel();
            setSeverity(SeverityLevel.ERROR.name());
            log(xmlFileText, lineNumberMap, dynamicMessageMaxLabelLengthError, key, type, node, file, labelText);
            setSeverity(configuredSeverityLevel.name());
        } else if (labelText.length() > maxLabelLength) {
            log(xmlFileText, lineNumberMap, dynamicMessageMaxLabelLength, key, type, node, file, labelText);
        }
    }

    private void log(final FileText xmlFileText, final Map<String, Integer> lineNumberMap, final String message,
            final String key, final String type, final Node node, final File file, final String labelText) {
        lazyLoadMap(xmlFileText, lineNumberMap);
        final Integer lineNr = lineNumberMap.get(labelText);

        logMessage(file.getPath(), lineNr == null ? 0 : lineNr, file.getName(),
                MessageFormat.format(message, type, key, getReferenceId(key, node), labelText, labelText.length()));
    }

    private void lazyLoadMap(final FileText xmlFileText, final Map<String, Integer> lineNumberMap) {
        if (lineNumberMap.isEmpty()) {
            final String[] lines = xmlFileText.getFullText().toString().split(System.lineSeparator());
            for (int i = 0; i < lines.length; i++) {
                final Matcher matcher = LABEL_PATTERN.matcher(lines[i]);

                if (matcher.find()) {
                    lineNumberMap.put(matcher.group(1), i + 1);
                }
            }
        }
    }

    private String getReferenceId(final String key, final Node node) {
        return node.getParentNode().getParentNode().getAttributes().getNamedItem(key).getNodeValue();
    }

    private String filterType(final String expression) {
        final Matcher matcher = TYPE_PATTERN.matcher(expression);
        return matcher.find() ? matcher.group(1) : "";
    }
}
