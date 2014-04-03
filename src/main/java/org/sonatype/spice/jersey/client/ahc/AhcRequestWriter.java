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

import static com.sun.jersey.api.client.ClientRequest.getHeaderValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.sonatype.spice.jersey.client.ahc.config.AhcConfig;

import com.ning.http.client.PerRequestConfig;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.CommittingOutputStream;
import com.sun.jersey.api.client.RequestWriter;

/**
 * An implementation of {@link RequestWriter} that also configure the AHC {@link RequestBuilder}
 *
 * @author Jeanfrancois Arcand
 */
public class AhcRequestWriter extends RequestWriter {

    public void configureRequest(final RequestBuilder requestBuilder, final ClientRequest cr, final boolean needsBody) {
        final Map<String, Object> props = cr.getProperties();

        // Set the read timeout
        final Integer readTimeout = (Integer) props.get(AhcConfig.PROPERTY_READ_TIMEOUT);
        if (readTimeout != null) {
            final PerRequestConfig c = new PerRequestConfig();
            c.setRequestTimeoutInMs(readTimeout);
            requestBuilder.setPerRequestConfig(c);
        }
        if (cr.getEntity() != null && needsBody) {
            final RequestEntityWriter re = getRequestEntityWriter(cr);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                re.writeRequestEntity(new CommittingOutputStream(baos) {
                    @Override
                    protected void commit() throws IOException {
                        configureHeaders(cr.getHeaders(), requestBuilder);
                    }
                });
            } catch (final IOException ex) {
                throw new ClientHandlerException(ex);
            }

            final byte[] content = baos.toByteArray();
            requestBuilder.setBody(new Request.EntityWriter() {
                @Override
                public void writeEntity(final OutputStream out) throws IOException {
                    out.write(content);
                }
            });
        } else {
            configureHeaders(cr.getHeaders(), requestBuilder);
        }
    }

    private void configureHeaders(final MultivaluedMap<String, Object> metadata, final RequestBuilder requestBuilder) {
        for (final Map.Entry<String, List<Object>> e : metadata.entrySet()) {
            final List<Object> vs = e.getValue();
            for (final Object o : vs) {
                if (String.class.isAssignableFrom( o.getClass() )) {
                    requestBuilder.addHeader(e.getKey(), o.toString());
                } else {
                    requestBuilder.addHeader(e.getKey(), getHeaderValue(o));
                }
            }
        }
    }
}
