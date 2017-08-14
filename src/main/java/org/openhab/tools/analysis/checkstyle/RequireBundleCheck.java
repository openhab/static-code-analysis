/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle;

import static org.openhab.tools.analysis.checkstyle.api.CheckConstants.MANIFEST_EXTENSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.openhab.tools.analysis.checkstyle.api.AbstractStaticCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.FileText;

/**
 * Checks if the MANIFEST.MF file contains any "Require-Bundle" entries.
 *
 * @author Petar Valchev
 *
 */
public class RequireBundleCheck extends AbstractStaticCheck {
    private final Logger logger = LoggerFactory.getLogger(RequireBundleCheck.class);

    public RequireBundleCheck() {
        setFileExtensions(MANIFEST_EXTENSION);
    }

    @Override
    protected void processFiltered(File file, FileText fileText) {
        try {
            // We use Manifest class here instead of ManifestParser,
            // because it is easier to get the content of the headers
            // in the MANIFEST.MF
            Manifest manifest = new Manifest(new FileInputStream(file));
            Attributes attributes = manifest.getMainAttributes();

            String requireBundleHeaderName = "Require-Bundle";
            String requireBundleHeaderValue = attributes.getValue(requireBundleHeaderName);
            if (requireBundleHeaderValue != null) {
                log(findLineNumber(fileText.toLinesArray(), requireBundleHeaderValue, 0),
                        "The MANIFEST.MF file must not contain any Require-Bundle entries. "
                                + "Instead, Import-Package must be used.");
            }
        } catch (FileNotFoundException e) {
            logger.error("An exception was thrown while trying to open the file {}", file.getPath(), e);
        } catch (IOException e) {
            logger.error("An exception was thrown while trying to read the file {}", file.getPath(), e);
        }
    }
}
