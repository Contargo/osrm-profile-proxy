package net.contargo.osrmproxy.config;

import java.net.URL;

import java.util.List;

import static java.util.Arrays.asList;


/**
 * Properties used as nested properties in {@link net.contargo.osrmproxy.config.ProxyProperties} to configure a
 * {@link net.contargo.osrmproxy.router.OsrmLimitRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public class LimitProfileProperties {

    private double limitInMeters;
    private Destinations destinations;

    public double getLimitInMeters() {

        return limitInMeters;
    }


    public void setLimitInMeters(double limitInMeters) {

        this.limitInMeters = limitInMeters;
    }


    public Destinations getDestinations() {

        return destinations;
    }


    public void setDestinations(Destinations destinations) {

        this.destinations = destinations;
    }


    public List<URL> getAllDestinations() {

        return asList(destinations.underLimit, destinations.overLimit, destinations.fallback);
    }

    public static class Destinations {

        private URL underLimit;
        private URL overLimit;
        private URL fallback;

        public URL getUnderLimit() {

            return underLimit;
        }


        public void setUnderLimit(URL underLimit) {

            this.underLimit = underLimit;
        }


        public URL getOverLimit() {

            return overLimit;
        }


        public void setOverLimit(URL overLimit) {

            this.overLimit = overLimit;
        }


        public URL getFallback() {

            return fallback;
        }


        public void setFallback(URL fallback) {

            this.fallback = fallback;
        }
    }
}
