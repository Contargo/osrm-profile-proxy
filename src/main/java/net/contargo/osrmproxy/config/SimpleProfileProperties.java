package net.contargo.osrmproxy.config;

import java.net.URL;


/**
 * Properties used as nested properties in {@link net.contargo.osrmproxy.config.ProxyProperties} to configure a
 * {@link net.contargo.osrmproxy.router.OsrmSimpleRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public class SimpleProfileProperties {

    private URL destination;

    public URL getDestination() {

        return destination;
    }


    public void setDestination(URL destination) {

        this.destination = destination;
    }
}
