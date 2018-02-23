package net.contargo.osrmproxy.router;

import net.contargo.osrmproxy.config.SimpleProfileProperties;

import java.net.URL;


/**
 * Router to route requests to the configured OSRM instance.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public class OsrmSimpleRouter implements OsrmProfileRouter {

    private final URL destination;

    OsrmSimpleRouter(SimpleProfileProperties properties) {

        destination = properties.getDestination();
    }

    @Override
    public URL getDestination(String requestUri, boolean isRoutingRequest) {

        return this.destination;
    }
}
