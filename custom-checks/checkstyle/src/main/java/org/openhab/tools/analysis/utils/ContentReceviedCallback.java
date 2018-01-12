/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
