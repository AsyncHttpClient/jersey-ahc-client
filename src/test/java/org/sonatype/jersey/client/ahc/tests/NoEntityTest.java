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

package org.sonatype.jersey.client.ahc.tests;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.sonatype.jersey.client.ahc.AhcHttpClient;
import org.sonatype.jersey.client.ahc.config.AhcConfig;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NoEntityTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public Response get() {
            return Response.status(Status.CONFLICT).build();
        }

        @POST
        public void post(String entity) {
        }
    }

    public NoEntityTest(String testName) {
        super(testName);
    }

    protected AhcHttpClient createClient() {
        return AhcHttpClient.create();
    }

    protected AhcHttpClient createClient(AhcConfig cc) {
        return AhcHttpClient.create(cc);
    }

    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.get(ClientResponse.class);
        }
    }

    public void testGetWithClose() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.get(ClientResponse.class);
            cr.close();
        }
    }

    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.post(ClientResponse.class);
        }
    }

    public void testPostWithClose() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.post(ClientResponse.class);
            cr.close();
        }
    }
}