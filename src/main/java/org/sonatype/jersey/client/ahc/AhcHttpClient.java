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
package org.sonatype.jersey.client.ahc;

import com.ning.http.client.AsyncHttpClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import org.sonatype.jersey.client.ahc.config.AhcConfig;
import org.sonatype.jersey.client.ahc.config.DefaultAhcConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * A {@link Client} that utilizes the AsyncHttpClient to send and receive
 * HTTP request and responses.
 * <p>
 * If an {@link AhcClientHandler} is not explicitly passed as a
 * constructor or method parameter then by default an instance is created with
 * an {@link AsyncHttpClient} constructed
 * <p>
 * <p>
 * If a response entity is obtained that is an instance of
 * {@link java.io.Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * <p>
 * If a {@link com.sun.jersey.api.client.ClientResponse} is obtained and an
 * entity is not read from the response then
 * {@link com.sun.jersey.api.client.ClientResponse#close() } MUST be called
 * after processing the response to release connection-based resources.
 *
 * @author Jeanfrancois Arcand
 */
public class AhcHttpClient extends Client {

    private AhcClientHandler clientHandler;
    
    /**
     * Create a new client instance.
     *
     */
    public AhcHttpClient() {
        this(createDefaultClientHander(new DefaultAhcConfig()));
    }

    /**
     * Create a new client instance.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     */
    public AhcHttpClient(AhcClientHandler root) {
        this(root, null);
    }

    /**
     * Create a new instance with a client configuration and a
     * component provider.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     * @param provider the IoC component provider factory.
     * @deprecated the config parameter is no longer utilized and instead
     *             the config obtained from the {@link AhcClientHandler#getConfig() }
     *             is utilized instead.
     */
    @Deprecated
    public AhcHttpClient(AhcClientHandler root, ClientConfig config,
            IoCComponentProviderFactory provider) {
        this(root, provider);
    }

    /**
     * Create a new instance with a client configuration and a
     * component provider.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param provider the IoC component provider factory.
     */
    public AhcHttpClient(AhcClientHandler root,
            IoCComponentProviderFactory provider) {
        super(root, root.getConfig(), provider);

        this.clientHandler = root;
        inject(this.clientHandler.getAhcRequestWriter());
    }

    /**
     * Get the AsyncHttpClient client handler.
     * 
     * @return the AsyncHttpClient client handler.
     */
    public AhcClientHandler getClientHandler() {
        return clientHandler;
    }

    /**
     * Create a default client.
     *
     * @return a default client.
     */
    public static AhcHttpClient create() {
        return create(new DefaultAhcConfig());
    }

    /**
     * Create a default client with client configuration.
     *
     * @param cc the client configuration.
     * @return a default client.
     */
    public static AhcHttpClient create(ClientConfig cc) {
        return create(cc, null);
    }

    /**
     * Create a default client with client configuration and component provider.
     *
     * @param cc the client configuration.
     * @param provider the IoC component provider factory.
     * @return a default client.
     */
    public static AhcHttpClient create(ClientConfig cc, IoCComponentProviderFactory provider) {
        return new AhcHttpClient(createDefaultClientHander(cc), provider);
    }

    @Override
    public void destroy() {
        clientHandler.getHttpClient().close();    
    }

    /**
     * Create a default AsyncHttpClient client handler.
     *
     * @return a default AsyncHttpClient client handler.
     */
    private static AhcClientHandler createDefaultClientHander(ClientConfig cc) {

        if (AhcConfig.class.isAssignableFrom(cc.getClass()) || DefaultAhcConfig.class.isAssignableFrom(cc.getClass())) {
            AhcConfig c = AhcConfig.class.cast(cc);
            return new AhcClientHandler(new AsyncHttpClient(c.getAsyncHttpClientConfigBuilder().build()), c);
        } else {
            throw new IllegalStateException("Client Config Type not supported");
        }
    }

    @Override
    public void setFollowRedirects(Boolean redirect) {
        clientHandler.getConfig().getAsyncHttpClientConfigBuilder().setFollowRedirects(redirect);
    }

    @Override
    public void setReadTimeout(Integer interval) {
        clientHandler.getConfig().getAsyncHttpClientConfigBuilder().setRequestTimeoutInMs(interval);
    }

    @Override
    public void setConnectTimeout(Integer interval) {
        clientHandler.getConfig().getAsyncHttpClientConfigBuilder().setConnectionTimeoutInMs(interval);
    }
}