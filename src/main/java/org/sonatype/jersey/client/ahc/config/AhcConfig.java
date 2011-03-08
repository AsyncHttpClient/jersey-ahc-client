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
package org.sonatype.jersey.client.ahc.config;

import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.jersey.api.client.config.ClientConfig;

public interface AhcConfig extends ClientConfig {

    /**
     * Get the {@link com.ning.http.client.AsyncHttpClientConfig.Builder} config object. Credentials may be set on the it.
     * <p>
     * @return the {@link com.ning.http.client.AsyncHttpClientConfig.Builder}
     */
    public AsyncHttpClientConfig.Builder getAsyncHttpClientConfigBuilder();
}
