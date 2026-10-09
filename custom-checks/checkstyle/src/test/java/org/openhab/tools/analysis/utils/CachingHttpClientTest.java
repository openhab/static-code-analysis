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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CachingHttpClient}
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class CachingHttpClientTest {

    private static final String PATH_TO_RESOURCE = "/found";
    private static final String PATH_TO_MISSING_RESOURCE = "/notFound";
    private static final int TEST_PORT = Integer.getInteger("http.port", 9090);
    private static final String TEST_HOST = "localhost";
    private static final int TEST_TIMEOUT = 1000;
    private static final String SERVER_RESPONSE = "content";

    private static Server server;
    private static TestHandler handler = new TestHandler();

    private ContentReceivedCallback<String> testCallback = String::new;
    private CachingHttpClient<String> testClient = new CachingHttpClient<>(testCallback);

    /**
     * Serves {@link #SERVER_RESPONSE} for {@link #PATH_TO_RESOURCE} and counts the received requests.
     * Requests that are not handled are answered with 404 by the server.
     */
    private static class TestHandler extends Handler.Abstract {
        private final AtomicInteger requestCount = new AtomicInteger();
        private volatile boolean responding = true;

        @Override
        public boolean handle(Request request, Response response, Callback callback) {
            requestCount.incrementAndGet();

            assertThat(request.getMethod(), is(HttpMethod.GET.asString()));

            if (!responding || !Request.getPathInContext(request).startsWith(PATH_TO_RESOURCE)) {
                return false;
            }
            response.setStatus(HttpStatus.OK_200);
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/html");
            Content.Sink.write(response, true, SERVER_RESPONSE + System.lineSeparator(), callback);
            return true;
        }
    }

    @BeforeAll
    public static void setUp() throws Exception {
        server = new Server();

        ServerConnector http = new ServerConnector(server);
        http.setHost(TEST_HOST);
        http.setPort(TEST_PORT);
        http.setIdleTimeout(TEST_TIMEOUT);
        server.addConnector(http);

        server.setHandler(handler);

        try {
            server.start();
        } catch (Exception e) {
            fail(MessageFormat.format("Unable to start test server on host {} and port {}", TEST_HOST, TEST_PORT, e));
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (server.isStarted() || server.isStarting()) {
            server.stop();
        }
    }

    @BeforeEach
    public void resetHandler() {
        handler.requestCount.set(0);
        handler.responding = true;
    }

    @Test
    public void testMultipleUnsuccessfulRequests() throws Exception {
        // Unique URL is needed for each test as once the URL is used it will be cached
        URL unreachableURL = getUniqueURL(PATH_TO_MISSING_RESOURCE);

        // First request throws exception, because a get request is sent
        try {
            testClient.get(unreachableURL);
        } catch (IOException e) {
            assertThat(e, instanceOf(IOException.class));
            assertThat(e.getMessage(), equalTo("Unable to get " + unreachableURL));
            assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
        }
        // Next requests do not throw exception
        assertNull(testClient.get(unreachableURL));
        assertNull(testClient.get(unreachableURL));

        assertEquals(1, handler.requestCount.get());
    }

    @Test
    public void testMultipleSuccessfulRequests() throws Exception {
        URL reachableURL = getUniqueURL(PATH_TO_RESOURCE);

        assertNotNull(testClient.get(reachableURL));
        assertNotNull(testClient.get(reachableURL));
        assertNotNull(testClient.get(reachableURL));

        assertEquals(1, handler.requestCount.get());
    }

    @Test
    public void testSuccessAndFailure() throws Exception {
        URL url = getUniqueURL(PATH_TO_RESOURCE);
        assertNotNull(testClient.get(url));

        // The server stops responding
        handler.responding = false;

        assertNotNull(testClient.get(url));
        assertEquals(1, handler.requestCount.get());
    }

    @Test
    public void testFailureAndSuccess() throws Exception {
        URL url = getUniqueURL(PATH_TO_RESOURCE);

        // The server stops responding
        handler.responding = false;

        // First request throws exception, because a get request is sent
        try {
            testClient.get(url);
        } catch (IOException e) {
            assertThat(e, instanceOf(IOException.class));
            assertThat(e.getMessage(), equalTo("Unable to get " + url));
            assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
        }

        handler.responding = true;

        // Next requests do not throw exception
        assertNull(testClient.get(url));
        assertNull(testClient.get(url));

        assertEquals(1, handler.requestCount.get());
    }

    private URL getUniqueURL(String path) throws MalformedURLException {
        String fileName = new SimpleDateFormat("mmssSSS'.txt'").format(new Date());
        return URI.create("http://" + TEST_HOST + ":" + TEST_PORT + path + "/" + fileName).toURL();
    }
}
