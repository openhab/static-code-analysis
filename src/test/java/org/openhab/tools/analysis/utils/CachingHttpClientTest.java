/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link CachingHttpClient}
 *
 * @author Svilen Valkanov - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingHttpClientTest {

    private static final String PATH_TO_RESOURCE = "/found";
    private static final String PATH_TO_MISSING_RESOURCE = "/notFound";
    private static final int TEST_PORT = 9090;
    private static final String TEST_HOST = "localhost";
    private static final int TEST_TIMEOUT = 1000;
    private static final String SERVER_RESPONSE = "content";

    private static final Logger logger = LoggerFactory.getLogger(CachingHttpClientTest.class);

    private static Server server;

    private ContentReceviedCallback<String> testCallback = s -> new String(s);
    private CachingHttpClient<String> testClient = new CachingHttpClient<>(testCallback);

    @BeforeClass
    public static void setUp() throws Exception {
        server = new Server();

        ServerConnector http = new ServerConnector(server);
        http.setHost(TEST_HOST);
        http.setPort(TEST_PORT);
        http.setIdleTimeout(TEST_TIMEOUT);
        server.addConnector(http);

        Handler handler = Mockito.mock(Handler.class);
        server.setHandler(handler);

        try {
            server.start();
        } catch (Exception e) {
            fail(MessageFormat.format("Unable to start test server on host {} and port {} : {}", TEST_HOST, TEST_PORT,
                    e));
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            if (server.isStarted() || server.isStarting()) {
                server.stop();
            }
        } catch (Exception e) {
            logger.error("Unable to stop test server {} : {}", e.getMessage(), e);
        }
    }

    @Before
    public void clearInteractionsWithServer() throws Exception {
        Handler handler = server.getHandler();
        Mockito.reset(handler);
        mockHandler(handler);
    }

    private void mockHandler(Handler handler) throws IOException, ServletException {
        Mockito.doAnswer(new Answer<Handler>() {
            @Override
            public Handler answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();

                Request baseRequest = (Request) arguments[2];
                HttpServletRequest request = (HttpServletRequest) arguments[2];
                HttpServletResponse response = (HttpServletResponse) arguments[3];

                baseRequest.setHandled(true);

                assertThat(request.getMethod(), is(HttpMethod.GET.asString()));

                if (request.getPathInfo().startsWith(PATH_TO_RESOURCE)) {
                    response.setStatus(HttpStatus.SC_OK);
                    response.setContentType("text/html");
                    response.getWriter().println(SERVER_RESPONSE);
                } else {
                    response.setStatus(HttpStatus.SC_NOT_FOUND);
                }
                return null;
            }
        }).when(handler).handle(any(), any(), any(), any());
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
            assertThat(e.getMessage(), equalTo("Unable to get " + unreachableURL.toString()));
            assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
        }
        // Next requests do not throw exception
        assertNull(testClient.get(unreachableURL));
        assertNull(testClient.get(unreachableURL));

        verify(server.getHandler(), times(1)).handle(any(), any(), any(), any());
    }

    @Test
    public void testMultipleSuccessfulRequests() throws Exception {
        URL reachableURL = getUniqueURL(PATH_TO_RESOURCE);

        assertNotNull(testClient.get(reachableURL));
        assertNotNull(testClient.get(reachableURL));
        assertNotNull(testClient.get(reachableURL));

        verify(server.getHandler(), times(1)).handle(any(), any(), any(), any());
    }

    @Test
    public void testSuccessAndFailure() throws Exception {
        URL url = getUniqueURL(PATH_TO_RESOURCE);
        assertNotNull(testClient.get(url));

        // The server stops responding
        Mockito.doNothing().when(server.getHandler()).handle(any(), any(), any(), any());

        assertNotNull(testClient.get(url));
        verify(server.getHandler(), times(1)).handle(any(), any(), any(), any());
    }

    @Test
    public void testFailureAndSuccess() throws Exception {
        URL url = getUniqueURL(PATH_TO_RESOURCE);

        // The server stops responding
        Mockito.doNothing().when(server.getHandler()).handle(any(), any(), any(), any());

        // First request throws exception, because a get request is sent
        try {
            testClient.get(url);
        } catch (IOException e) {
            assertThat(e, instanceOf(IOException.class));
            assertThat(e.getMessage(), equalTo("Unable to get " + url.toString()));
            assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
        }

        mockHandler(server.getHandler());

        // Next requests do not throw exception
        assertNull(testClient.get(url));
        assertNull(testClient.get(url));

        verify(server.getHandler(), times(1)).handle(any(), any(), any(), any());
    }

    private URL getUniqueURL(String path) throws MalformedURLException {
        String fileName = new SimpleDateFormat("mmssSSS'.txt'").format(new Date());
        return new URL("http", TEST_HOST, TEST_PORT, path + "/" + fileName);
    }
}
