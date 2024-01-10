/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.checks.header.AbstractHeaderCheck;

/**
 * Checks the header of the source against a header file that contains a
 * {@link Pattern regular expression} and parameters
 * (e.g. years included in the copyright notice, names, etc)
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class ParameterizedRegexpHeaderCheck extends AbstractHeaderCheck {
    // Default header formats
    public static final String DEFAULT_JAVADOC_START_COMMENT = "/**";
    public static final String DEFAULT_XML_START_COMMENT = "<!--";

    private static final String SEPARATOR = "\n";
    private static final String MSG_MISMATCH = "Header line doesn''t match pattern {0}";
    private static final String MSG_MISSING = "Header is missing";

    /**
     * Values of the parameters used in the pattern, in the pattern.
     * They are defined with number between a curly bracket - e.g. {0}
     */
    private String[] values;

    /**
     * Format of the header
     */
    private String headerFormat;

    /** The compiled regular expressions. */
    private final List<Pattern> headerRegexps = new ArrayList<>();

    public void setHeaderFormat(String headerFormat) {
        this.headerFormat = headerFormat;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    /**
     * Post processing of the reference header lines.
     * Includes replacing the placeholders in the header with actual values and
     * pattern compilation
     */
    @Override
    protected void postProcessHeaderLines() {
        final List<String> headerLines = getHeaderLines();
        String header = String.join(SEPARATOR, headerLines);
        String formattedHeader = MessageFormat.format(header, values);

        for (String line : formattedHeader.split(SEPARATOR)) {
            try {
                headerRegexps.add(Pattern.compile(line));
            } catch (final PatternSyntaxException ex) {
                throw new IllegalArgumentException("line " + (headerRegexps.size() + 1) + " in header specification"
                        + " is not a regular expression", ex);
            }
        }
    }

    @Override
    protected void processFiltered(File file, FileText fileText) {
        if (headerFormat == null) {
            headerFormat = getDefaultHeaderFormat(file);
        }

        List<String> referenceHeaderLines = getHeaderLines();

        if (referenceHeaderLines.size() > fileText.size()) {
            log(1, MSG_MISSING);
        } else {
            if (!isComment(headerFormat, fileText.get(0))) {
                log(1, MSG_MISSING);
                return;
            }

            for (int i = 0; i < referenceHeaderLines.size(); i++) {
                if (!isMatch(fileText.get(i), i)) {
                    log(i + 1, MSG_MISMATCH, headerRegexps.get(i).pattern());
                    break;
                }
            }
        }
    }

    private String getDefaultHeaderFormat(File file) {
        switch (FilenameUtils.getExtension(file.getName())) {
            case JAVA_EXTENSION:
                return DEFAULT_JAVADOC_START_COMMENT;
            case XML_EXTENSION:
                return DEFAULT_XML_START_COMMENT;
            default:
                // will simply skip this check
                return StringUtils.EMPTY;
        }
    }

    private boolean isMatch(String line, int headerLineNo) {
        return headerRegexps.get(headerLineNo).matcher(line).find();
    }

    private boolean isComment(String type, String line) {
        return line.trim().startsWith(type.trim());
    }
}
