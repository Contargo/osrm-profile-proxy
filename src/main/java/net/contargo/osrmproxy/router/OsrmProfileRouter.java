package net.contargo.osrmproxy.router;

import java.net.URL;


/**
 * @author  Ben Antony - antony@synyx.de
 */
public interface OsrmProfileRouter {

    /**
     * Returns the host with port and protocol to which a request should be redirected.
     *
     * @param  requestUri  the complete url
     * @param  isRoutingRequest  true if the request is a routing request
     *
     * @return  The host with port and protocol
     */
    URL getDestination(String requestUri, boolean isRoutingRequest);
}
