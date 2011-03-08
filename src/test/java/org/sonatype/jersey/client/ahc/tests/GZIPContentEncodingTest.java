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
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.sonatype.jersey.client.ahc.AhcHttpClient;
import org.sonatype.jersey.client.ahc.config.AhcConfig;
import org.sonatype.jersey.client.ahc.config.DefaultAhcConfig;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Arrays;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class GZIPContentEncodingTest extends AbstractGrizzlyServerTester {

    @Path("/")
    public static class Resource {
        @POST
        public byte[] post(byte[] content) { return content; }
    }
    
    public GZIPContentEncodingTest(String testName) {
        super(testName);
    }


    public void testPost() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                GZIPContentEncodingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                GZIPContentEncodingFilter.class.getName());
        startServer(rc);

        AhcHttpClient c = AhcHttpClient.create();
        c.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());

        WebResource r = c.resource(getUri().path("/").build());
        byte[] content = new byte[1024 * 1024];
        assertTrue(Arrays.equals(content, r.post(byte[].class, content)));

        ClientResponse cr = r.post(ClientResponse.class, content);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPostChunked() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                GZIPContentEncodingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                GZIPContentEncodingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        config.getProperties().put(AhcConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 1024);
        AhcHttpClient c = AhcHttpClient.create(config);
        c.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());

        WebResource r = c.resource(getUri().path("/").build());
        byte[] content = new byte[1024 * 1024];
        assertTrue(Arrays.equals(content, r.post(byte[].class, content)));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }

}