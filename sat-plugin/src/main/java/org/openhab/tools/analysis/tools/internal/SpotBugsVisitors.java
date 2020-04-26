/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.tools.analysis.tools.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * A JAXB model of the SpotBugs visitors XML file
 *
 * @author Svilen Valkanov - Initial contribution
 */
@XmlRootElement(name = "visitors")
public class SpotBugsVisitors {

    @XmlElement
    List<String> visitor = new ArrayList<String>();

    @Override
    public String toString() {
        return StringUtils.join(visitor, ",");
    }
}
