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

package org.sonatype.spice.jersey.client.ahc.tests.tests;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.AhcConfig;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpMethodTest extends AbstractGrizzlyServerTester {
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    public @interface PATCH {
    }

    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            return "GET";
        }

        @POST
        public String post(String entity) {
            return entity;
        }

        @PUT
        public String put(String entity) {
            return entity;
        }

        @DELETE
        public String delete() {
            return "DELETE";
        }

        @DELETE
        @Path("withentity")
        public String delete(String entity) {
            return entity;
        }

        @POST
        @Path("noproduce")
        public void postNoProduce(String entity) {
        }

        @POST
        @Path("noconsumeproduce")
        public void postNoConsumeProduce() {
        }

        @PATCH
        public String patch(String entity) {
            return entity;
        }
    }

    public HttpMethodTest(String testName) {
        super(testName);
    }

    protected AhcHttpClient createClient() {
        return AhcHttpClient.create();
    }

    protected AhcHttpClient createClient(AhcConfig cc) {
        return AhcHttpClient.create(cc);
    }

    public void testHead() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        ClientResponse cr = r.head();
        assertFalse(cr.hasEntity());
    }

    public void testOptions() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        ClientResponse cr = r.options(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));

        ClientResponse cr = r.get(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPostChunked() {
        ResourceConfig rc = new DefaultResourceConfig(HttpMethodResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getProperties().put(AhcConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 1024);
        AhcHttpClient c = createClient(config);

        WebResource r = c.resource(getUri().path("test").build());        
        assertEquals("POST", r.post(String.class, "POST"));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPostVoid() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            r.post("POST");
        }
    }

    public void testPostNoProduce() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals(204, r.path("noproduce").post(ClientResponse.class, "POST").getStatus());

        ClientResponse cr = r.path("noproduce").post(ClientResponse.class, "POST");
        assertFalse(cr.hasEntity());
        cr.close();
    }

    public void testPostNoConsumeProduce() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals(204, r.path("noconsumeproduce").post(ClientResponse.class).getStatus());

        ClientResponse cr = r.path("noconsumeproduce").post(ClientResponse.class, "POST");
        assertFalse(cr.hasEntity());
        cr.close();
    }

    public void testPut() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT"));

        ClientResponse cr = r.put(ClientResponse.class, "PUT");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testDelete() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class));

        ClientResponse cr = r.delete(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testDeleteWithEntity() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test/withentity").build());
        r.addFilter(new com.sun.jersey.api.client.filter.LoggingFilter());
        assertEquals("DELETE with entity", r.delete(String.class, "DELETE with entity"));

        ClientResponse cr = r.delete(ClientResponse.class, "DELETE with entity");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPatch() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        r.addFilter(new com.sun.jersey.api.client.filter.LoggingFilter());
        assertEquals("PATCH", r.method("PATCH", String.class, "PATCH"));

        ClientResponse cr = r.method("PATCH", ClientResponse.class, "PATCH");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testAll() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        assertEquals("GET", r.get(String.class));

        assertEquals("POST", r.post(String.class, "POST"));

        assertEquals(204, r.path("noproduce").post(ClientResponse.class, "POST").getStatus());

        assertEquals(204, r.path("noconsumeproduce").post(ClientResponse.class).getStatus());

        assertEquals("PUT", r.post(String.class, "PUT"));

        assertEquals("DELETE", r.delete(String.class));
    }


    @Path("/test")
    public static class ErrorResource {
        @POST
        public Response post(String entity) {
            return Response.serverError().build();
        }

        @Path("entity")
        @POST
        public Response postWithEntity(String entity) {
            return Response.serverError().entity("error").build();
        }
    }

    public void testPostError() {
        startServer(ErrorResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            try {
                r.post("POST");
            } catch (UniformInterfaceException ex) {
            }
        }
    }

    public void testPostErrorWithEntity() {
        startServer(ErrorResource.class);
        WebResource r = createClient().resource(getUri().path("test/entity").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            try {
                r.post("POST");
            } catch (UniformInterfaceException ex) {
                String s = ex.getResponse().getEntity(String.class);
                assertEquals("error", s);
            }
        }
    }
}