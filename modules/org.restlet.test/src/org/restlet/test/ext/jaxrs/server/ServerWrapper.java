/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * This interface wraps a server for the tests. The default implementation is
 * the {@link RestletServerWrapper}, but there are other implementations
 * possible, for example for the JSR-311 reference implementation Jersey.
 * 
 * @see org.restlet.test.ext.jaxrs.services.tests.JaxRsTestCase#setServerWrapper(ServerWrapper)
 * @see ServerWrapperFactory
 * 
 * @author Stephan Koops
 */
public interface ServerWrapper {
    /**
     * Returns the connector to access the application.
     * 
     * @return the connector to access the application.
     */
    public Restlet getClientConnector();

    /**
     * Returns the port the server is running on. throws an
     * {@link IllegalStateException}, if direct access is used.
     */
    public int getServerPort();

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param protocol
     *            the protocol to use
     * @param appConfig
     * @throws Exception
     */
    public void startServer(Application application, Protocol protocol)
            throws Exception;

    /**
     * Stops the component. The method {@link #tearDown()} do this after every
     * test.
     * 
     * @param component
     * @throws Exception
     */
    public void stopServer() throws Exception;
}
