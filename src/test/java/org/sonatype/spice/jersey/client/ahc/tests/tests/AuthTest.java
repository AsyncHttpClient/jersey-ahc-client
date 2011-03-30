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

import com.ning.http.client.Realm;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.resource.Singleton;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AuthTest extends AbstractGrizzlyServerTester {

    public AuthTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    public static class PreemptiveAuthResource {
        @GET
        public String get(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return e;
        }
    }
        
    public void testPreemptiveAuth() {
        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setScheme(Realm.AuthScheme.BASIC).setUsePreemptiveAuth(true).setPrincipal("name").setPassword("password").build());
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().build());
        assertEquals("GET", r.get(String.class));
    }

    public void testPreemptiveAuthPost() {
        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setScheme(Realm.AuthScheme.BASIC).setUsePreemptiveAuth(true).setPrincipal("name").setPassword("password").build());
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

    @Path("/test")
    @Singleton
    public static class AuthResource {
        int requestCount = 0;
        @GET
        public String get(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return "GET";
        }

        @GET
        @Path("filter")
        public String getFilter(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return e;
        }

        @POST
        @Path("filter")
        public String postFilter(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return e;
        }

        @DELETE
        public void delete(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }
        }

        @DELETE
        @Path("filter")
        public void deleteFilter(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }
        }

        @DELETE
        @Path("filter/withEntity")
        public String deleteFilterWithEntity(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return e;
        }


        
    }

    public void testAuthGet() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setUsePreemptiveAuth(false).setPrincipal("name").setPassword("password").build());
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }

    public void testAuthGetWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        AhcHttpClient c = AhcHttpClient.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        assertEquals("GET", r.get(String.class));
    }

    public void testAuthPost() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setUsePreemptiveAuth(false).setPrincipal("name").setPassword("password").build());
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

    public void testAuthPostWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        AhcHttpClient c = AhcHttpClient.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

    public void testAuthDelete() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setUsePreemptiveAuth(false).setPrincipal("name").setPassword("password").build());
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        ClientResponse response = r.delete(ClientResponse.class);
        assertEquals(response.getStatus(), 204);
    }

    public void testAuthDeleteWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        AhcHttpClient c = AhcHttpClient.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        ClientResponse response = r.delete(ClientResponse.class);
        assertEquals(204, response.getStatus());
    }

    public void testAuthDeleteWithEntityUsingClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        AhcHttpClient c = AhcHttpClient.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter/withEntity").build());
        ClientResponse response = r.delete(ClientResponse.class, "DELETE");
        assertEquals(200, response.getStatus());
        assertEquals("DELETE", response.getEntity(String.class));
    }

    public void testAuthInteractiveGet() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setUsePreemptiveAuth(false).setPrincipal("name").setPassword("password").build());
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }

    public void testAuthInteractivePost() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getAsyncHttpClientConfigBuilder().setRealm(new Realm.RealmBuilder().setUsePreemptiveAuth(false).setPrincipal("name").setPassword("password").build());

        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }
}
