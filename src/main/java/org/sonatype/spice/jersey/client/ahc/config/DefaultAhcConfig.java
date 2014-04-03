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
package org.sonatype.spice.jersey.client.ahc.config;

import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class DefaultAhcConfig extends DefaultClientConfig implements AhcConfig{

    private AsyncHttpClientConfig.Builder config;

    @Override
    public AsyncHttpClientConfig.Builder getAsyncHttpClientConfigBuilder() {

        if (config == null) {
            config = new AsyncHttpClientConfig.Builder();
        }

        return config;
    }
}