package net.contargo.osrmproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * Root properties to configure the osrm-profile-proxy.
 *
 * @author  Sandra Thieme - thieme@synyx.de
 * @author  Ben Antony - antony@synyx.de
 */
@Component
@ConfigurationProperties("proxy")
public class ProxyProperties {

    private Profiles profiles;
    private HealthCoordinates healthCoordinates;

    public HealthCoordinates getHealthCoordinates() {

        return healthCoordinates;
    }


    public void setHealthCoordinates(HealthCoordinates healthCoordinates) {

        this.healthCoordinates = healthCoordinates;
    }


    public Profiles getProfiles() {

        return profiles;
    }


    public void setProfiles(Profiles profiles) {

        this.profiles = profiles;
    }

    public static class Profiles {

        private Map<String, LimitProfileProperties> limit = new HashMap<>();
        private Map<String, SimpleProfileProperties> simple = new HashMap<>();
        private String fallback;

        public Map<String, LimitProfileProperties> getLimit() {

            return limit;
        }


        public void setLimit(Map<String, LimitProfileProperties> limit) {

            this.limit = limit;
        }


        public Map<String, SimpleProfileProperties> getSimple() {

            return simple;
        }


        public void setSimple(Map<String, SimpleProfileProperties> simple) {

            this.simple = simple;
        }


        public String getFallback() {

            return fallback;
        }


        public void setFallback(String fallback) {

            this.fallback = fallback;
        }
    }

    public static class HealthCoordinates {

        private Coordinates start;
        private Coordinates end;

        public Coordinates getStart() {

            return start;
        }


        public void setStart(Coordinates start) {

            this.start = start;
        }


        public double getStartLatitude() {

            return start.getLatitude();
        }


        public double getStartLongitude() {

            return start.getLongitude();
        }


        public Coordinates getEnd() {

            return end;
        }


        public void setEnd(Coordinates end) {

            this.end = end;
        }


        public double getEndLatitude() {

            return end.getLatitude();
        }


        public double getEndLongitude() {

            return end.getLongitude();
        }
    }

    public static class Coordinates {

        private double latitude;
        private double longitude;

        public double getLatitude() {

            return latitude;
        }


        public void setLatitude(double latitude) {

            this.latitude = latitude;
        }


        public double getLongitude() {

            return longitude;
        }


        public void setLongitude(double longitude) {

            this.longitude = longitude;
        }
    }
}
