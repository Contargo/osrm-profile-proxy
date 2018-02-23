package net.contargo.osrmproxy.router;

import net.contargo.osrmproxy.config.LimitProfileProperties;
import net.contargo.osrmproxy.service.AerialDistanceCalculation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;


/**
 * Router to route requests to a configured OSRM instance depending on aerial distance.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public class OsrmLimitRouter implements OsrmProfileRouter {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            ".*/(-?\\d{1,3}\\.\\d*),(-?\\d{1,3}\\.\\d*);(?:-?\\d{1,3}\\.\\d*,-?\\d{1,3}\\.\\d*;)*(-?\\d{1,3}\\.\\d*),"
            + "(-?\\d{1,3}\\.\\d*)");

    private static final int INDEX_LON_A = 1;
    private static final int INDEX_LAT_A = 2;
    private static final int INDEX_LON_B = 3;
    private static final int INDEX_LAT_B = 4;

    private final URL destinationUnderLimit;
    private final URL destinationOverLimit;
    private final URL fallbackDestination;
    private final double limitInMeters;

    OsrmLimitRouter(LimitProfileProperties properties) {

        this.limitInMeters = properties.getLimitInMeters();
        this.destinationOverLimit = properties.getDestinations().getOverLimit();
        this.destinationUnderLimit = properties.getDestinations().getUnderLimit();
        this.fallbackDestination = properties.getDestinations().getFallback();
    }

    @Override
    public URL getDestination(String requestUri, boolean isRoutingRequest) {

        return isRoutingRequest ? getDestinationForRouting(requestUri) : fallbackDestination;
    }


    private URL getDestinationForRouting(String url) {

        URL destination = null;

        try {
            double distance = calculateAerialDistanceFromUrl(url);
            destination = distance > limitInMeters ? destinationOverLimit : destinationUnderLimit;
        } catch (NoCoordinatesException e) {
            LOG.error("No coordinates were found: {}", e.getRequestURI(), e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            LOG.error("Calculation of aerial distance {} had errors", url, e);
        }

        // In case of an error (e.g. NoCoordinatesException, IllegalStateException or IllegalArgumentException )
        // the request should be proxied to the default destination
        return destination == null ? fallbackDestination : destination;
    }


    private double calculateAerialDistanceFromUrl(String requestURI) {

        Matcher matcher = COORDINATE_PATTERN.matcher(requestURI);

        if (matcher.matches()) {
            double lonA = parseDouble(matcher.group(INDEX_LON_A));
            double latA = parseDouble(matcher.group(INDEX_LAT_A));
            double lonB = parseDouble(matcher.group(INDEX_LON_B));
            double latB = parseDouble(matcher.group(INDEX_LAT_B));

            return AerialDistanceCalculation.calculateAerialDistanceInMeters(lonA, latA, lonB, latB);
        } else {
            throw new NoCoordinatesException(requestURI);
        }
    }
}
