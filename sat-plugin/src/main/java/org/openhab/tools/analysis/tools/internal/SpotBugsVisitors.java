/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @author Svilen Valkanov
 *
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