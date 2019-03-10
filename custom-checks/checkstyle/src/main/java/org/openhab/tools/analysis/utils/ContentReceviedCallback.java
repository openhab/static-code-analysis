/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.tools.analysis.utils;

/**
 * A callback interface for the {@link CachingHttpClient}.
 *
 * @author Svilen Valkanov
 * @param <T> - the type of the object
 */
public interface ContentReceviedCallback<T> {
    /**
     * Called after a successful download attempt is made by the {@link CachingHttpClient}
     * and should transform the data into a object of type T
     *
     * @param content - HTTP request content, can`t be null
     * @return the transformed data
     */
    public T transform(byte[] content);
}
