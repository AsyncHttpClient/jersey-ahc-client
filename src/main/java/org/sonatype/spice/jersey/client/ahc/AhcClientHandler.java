/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.spice.jersey.client.ahc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Context;

import org.sonatype.spice.jersey.client.ahc.config.AhcConfig;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;

/**
 * A root handler with Sonatype AsyncHttpClient acting as a backend.
 * <p/>
 * Client operations are thread safe, the HTTP connection may
 * be shared between different threads.
 * <p/>
 * If a response entity is obtained that is an instance of {@link java.io.Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * <p/>
 * If a {@link ClientResponse} is obtained and an entity is not read from the
 * response then {@link ClientResponse#close() } MUST be called after processing
 * the response to release connection-based resources.
 * <p/>
 * The following methods are currently supported: HEAD, GET, POST, PUT, DELETE, TRACE
 * OPTIONS as well as custom methods.
 * <p/>
 *
 * @author Jeanfrancois Arcand
 */
public final class AhcClientHandler implements ClientHandler {

    private final AsyncHttpClient client;

    private final AhcConfig config;

    private final AhcRequestWriter requestWriter = new AhcRequestWriter();

    private final List<Cookie> cookies = new ArrayList<Cookie>();

    @Context
    private MessageBodyWorkers workers;

    /**
     * Create a new root handler with an {@link AsyncHttpClient}.
     *
     * @param client the {@link AsyncHttpClient}.
     */
    public AhcClientHandler(final AsyncHttpClient client) {
        this(client, new DefaultAhcConfig());
    }

    /**
     * Create a new root handler with an {@link AsyncHttpClient}.
     *
     * @param client the {@link AsyncHttpClient}.
     * @param config the client configuration.
     */
    public AhcClientHandler(final AsyncHttpClient client, final AhcConfig config) {
        this.client = client;
        this.config = config;
    }

    /**
     * Get the client config.
     *
     * @return the client config.
     */
    public AhcConfig getConfig() {
        return config;
    }

    /**
     * Get the {@link AsyncHttpClient}.
     *
     * @return the {@link AsyncHttpClient}.
     */
    public AsyncHttpClient getHttpClient() {
        return client;
    }

    /**
     * Translate the {@link ClientRequest} into a AsyncHttpClient request, and execute it.
     *
     * @param cr the HTTP request.
     * @return the {@link ClientResponse}
     * @throws ClientHandlerException
     */
    @Override
    public ClientResponse handle(final ClientRequest cr)
            throws ClientHandlerException {

        try {
            final RequestBuilder requestBuilder = getRequestBuilder(cr);
            handleCookie(requestBuilder);
            requestWriter.configureRequest(requestBuilder, cr, allowBody(cr.getMethod()));

            final Response response = client.executeRequest(requestBuilder.build()).get();

            applyResponseCookies(response.getCookies());

            final ClientResponse r = new ClientResponse(response.getStatusCode(),
                    getInBoundHeaders(response),
                    response.getResponseBodyAsStream(),
                    workers);
            if (!r.hasEntity()) {
                r.bufferEntity();
                r.close();
            }
            return r;
        } catch (final Exception e) {
            throw new ClientHandlerException(e);
        }
    }

    /**
     * append request cookies and override existing cookies
     *
     * @param responseCookies list of cookies from response
     */
    private void applyResponseCookies(final List<Cookie> responseCookies) {
        if (responseCookies != null) {
            for (final Cookie rc : responseCookies) {
                // remove existing cookie
                final Iterator<Cookie> it = cookies.iterator();
                while (it.hasNext()) {
                    final Cookie c = it.next();
                    if (isSame(rc, c)) {
                        it.remove();
                        break;
                    }
                }
                // add new cookie
                cookies.add(rc);
            }
        }
    }

    private boolean isSame(final Cookie c, final Cookie o) {
        return isEquals(c.getDomain(), o.getDomain()) &&
                isEquals(c.getPath(), o.getPath()) &&
                isEquals(c.getName(), o.getName());
    }

    private boolean isEquals(final Object o, final Object o2) {
        return (o == null && o2 == null) || o != null && o.equals(o2);
    }

    /**
     * Check if a body needs to be constructed based on a method's name.
     *
     * @param method An HTTP method
     * @return true if s body can be allowed.
     */
    private boolean allowBody(final String method) {
        if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("OPTIONS")
                && method.equalsIgnoreCase("TRACE")
                && method.equalsIgnoreCase("HEAD")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Return the {@link RequestBuilder} based on a method
     *
     * @param cr the HTTP request.
     * @return {@link RequestBuilder}
     */
    private RequestBuilder getRequestBuilder(final ClientRequest cr) {
        final String strMethod = cr.getMethod();
        final String uri = cr.getURI().toString();

        if (strMethod.equals("GET")) {
            return new RequestBuilder("GET").setUrl(uri);
        } else if (strMethod.equals("POST")) {
            return new RequestBuilder("POST").setUrl(uri);
        } else if (strMethod.equals("PUT")) {
            return new RequestBuilder("PUT").setUrl(uri);
        } else if (strMethod.equals("DELETE")) {
            return new RequestBuilder("DELETE").setUrl(uri);
        } else if (strMethod.equals("HEAD")) {
            return new RequestBuilder("HEAD").setUrl(uri);
        } else if (strMethod.equals("OPTIONS")) {
            return new RequestBuilder("OPTIONS").setUrl(uri);
        } else {
            return new RequestBuilder(strMethod).setUrl(uri);
        }
    }

    private InBoundHeaders getInBoundHeaders(final Response response) {
        final InBoundHeaders headers = new InBoundHeaders();
        final FluentCaseInsensitiveStringsMap respHeaders = response.getHeaders();
        for (final Map.Entry<String, List<String>> header : respHeaders) {
            headers.put(header.getKey(), header.getValue());
        }
        return headers;
    }

    /**
     * Return the instance of {@link com.sun.jersey.api.client.RequestWriter}. This instance will be injected
     * within Jersey so it cannot be null.
     *
     * @return the instance of {@link com.sun.jersey.api.client.RequestWriter}.
     */
    public AhcRequestWriter getAhcRequestWriter() {
        return requestWriter;
    }

    private void handleCookie(final RequestBuilder requestBuilder) {
        for (final Cookie c : cookies) {
            requestBuilder.addCookie(c);
        }
    }

}
