/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.sonatype.spice.jersey.client.ahc.tests.tests;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;

/**
 * @author Paul.Sandoz@Sun.Com
 */
public class CookieTest extends AbstractGrizzlyServerTester {
    @Path("/")
    public static class CookieResource {
        @GET
        public Response get(@Context HttpHeaders h) {
            Cookie c = h.getCookies().get("name");
            String e = (c == null) ? "NO-COOKIE" : c.getValue();
            return Response.ok(e).
                    cookie(new NewCookie("name", "value")).build();
        }

        @POST
        public Response get() {
            // return response without cookie
            return Response.ok("wo-cookie").build();
        }
    }

    public CookieTest(String testName) {
        super(testName);
    }

    public void testCookie() {
        ResourceConfig rc = new DefaultResourceConfig(CookieResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().build());

        assertEquals("NO-COOKIE", r.get(String.class));
        assertEquals("value", r.get(String.class));
    }

    public void testCookieWithState() {
        ResourceConfig rc = new DefaultResourceConfig(CookieResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().build());

        assertEquals("NO-COOKIE", r.get(String.class));
        assertEquals("value", r.get(String.class));

    }

    public void testSessionCookie() {
        ResourceConfig rc = new DefaultResourceConfig(CookieResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultAhcConfig config = new DefaultAhcConfig();
        AhcHttpClient c = AhcHttpClient.create(config);

        WebResource r = c.resource(getUri().build());

        assertEquals("NO-COOKIE", r.get(String.class));
        assertEquals("wo-cookie", r.post(String.class));
        assertEquals("value", r.get(String.class));
    }
}