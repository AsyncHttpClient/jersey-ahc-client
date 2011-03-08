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

import com.sun.jersey.api.client.filter.LoggingFilter;
import org.sonatype.jersey.client.ahc.AhcHttpClient;
import org.sonatype.jersey.client.ahc.config.AhcConfig;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpMethodWithClientFilterTest extends HttpMethodTest {
    public HttpMethodWithClientFilterTest(String testName) {
        super(testName);
    }

    @Override
    protected AhcHttpClient createClient() {
        AhcHttpClient ac = AhcHttpClient.create();
        ac.addFilter(new LoggingFilter());
        return ac;
    }

    @Override
    protected AhcHttpClient createClient(AhcConfig cc) {
        AhcHttpClient ac = AhcHttpClient.create(cc);
        ac.addFilter(new LoggingFilter());
        return ac;
    }
}