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
package org.openhab.tools.analysis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A simple caching HttpClient
 *
 * A {@link ContentReceivedCallback} is used to convert the downloaded data.
 * The cached entry expires after {@link #RETRY_TIME}
 *
 * @author Svilen Valkanov - Initial contribution
 * @param <T> the type of the object being returned from the client
 */
public class CachingHttpClient<T> {
    /**
     * Retry time in minutes
     */
    private static final int RETRY_TIME = 10;
    private static Cache<URL, Optional<byte[]>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(RETRY_TIME, TimeUnit.MINUTES).build();

    private ContentReceivedCallback<T> callback;

    public CachingHttpClient(ContentReceivedCallback<T> callback) {
        this.callback = callback;
    }

    /**
     * Makes an attempt to download a file only once,
     * subsequent calls will return the result of the first attempt
     * until the entry expires
     *
     * @param url the resource URL
     * @throws IOException if an I/O exception occurs
     * @throws IllegalArgumentException if the URL is null
     * @return the requested object or null, if the first download attempt is unsuccessful
     */
    public synchronized T get(URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null");
        }

        Optional<byte[]> content;
        try {
            content = cache.get(url, () -> Optional.of(getContent(url)));
        } catch (ExecutionException e) {
            cache.put(url, Optional.empty());
            throw new IOException("Unable to get " + url, e.getCause());
        }
        return content.map(bytes -> callback.transform(bytes)).orElse(null);
    }

    private byte[] getContent(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        try (InputStream input = connection.getInputStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copy(input, output);
            return output.toByteArray();
        }
    }
}
