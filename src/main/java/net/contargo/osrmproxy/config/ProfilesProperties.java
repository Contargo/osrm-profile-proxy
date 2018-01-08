package net.contargo.osrmproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import java.net.URL;


/**
 * Properties for everything that is configured under "profiles" in the application.yml.
 *
 * @author  Ben Antony - antony@synyx.de
 * @author  Sandra Thieme - thieme@synyx.de
 */
@Component
@ConfigurationProperties("profiles")
public class ProfilesProperties {

    private double limitInMeters;
    private Destinations destinations;

    public double getLimitInMeters() {

        return limitInMeters;
    }


    public void setLimitInMeters(int limitInMeters) {

        this.limitInMeters = limitInMeters;
    }


    public Destinations getDestinations() {

        return destinations;
    }


    public URL getDestinationUnderLimit() {

        return destinations.getUnderLimit();
    }


    public URL getDestinationOverLimit() {

        return destinations.getOverLimit();
    }


    public URL getFallbackDestination() {

        return destinations.getFallback();
    }


    public void setDestinations(Destinations destinations) {

        this.destinations = destinations;
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
