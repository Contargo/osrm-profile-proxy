package net.contargo.osrmproxy.router;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import net.contargo.osrmproxy.config.ProfilesProperties;
import net.contargo.osrmproxy.service.AerialDistanceCalculationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

import static java.lang.Double.parseDouble;


/**
 * Router to route requests to a configured OSRM instance depending on aerial distance.
 *
 * @author  Ben Antony - antony@synyx.de
 */
@Component
public class OsrmProfileRouter extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            ".*/(-?\\d{1,3}\\.\\d*),(-?\\d{1,3}\\.\\d*);(?:-?\\d{1,3}\\.\\d*,-?\\d{1,3}\\.\\d*;)*(-?\\d{1,3}\\.\\d*),"
            + "(-?\\d{1,3}\\.\\d*)");

    private static final int INDEX_LON_A = 1;
    private static final int INDEX_LAT_A = 2;
    private static final int INDEX_LON_B = 3;
    private static final int INDEX_LAT_B = 4;

    private static final String ROUTING_URL_PREFIX = "/osrm/route/";

    private final AerialDistanceCalculationService aerialDistanceCalculationService;

    private final URL destinationUnderLimit;
    private final URL destinationOverLimit;
    private final URL fallbackDestination;
    private final double limitInMeters;

    @Autowired
    public OsrmProfileRouter(ProfilesProperties profilesProperties,
        AerialDistanceCalculationService aerialDistanceCalculationService) {

        this.limitInMeters = profilesProperties.getLimitInMeters();
        this.destinationOverLimit = profilesProperties.getDestinationOverLimit();
        this.destinationUnderLimit = profilesProperties.getDestinationUnderLimit();
        this.fallbackDestination = profilesProperties.getFallbackDestination();
        this.aerialDistanceCalculationService = aerialDistanceCalculationService;
    }

    @Override
    public String filterType() {

        return ROUTE_TYPE;
    }


    @Override
    public int filterOrder() {

        return 0;
    }


    @Override
    public boolean shouldFilter() {

        return true;
    }


    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();

        Boolean isRoutingRequest = ctx.getRequest().getRequestURI().startsWith(ROUTING_URL_PREFIX);
        URL destination = isRoutingRequest ? getDestinationForRouting(ctx) : fallbackDestination;
        ctx.setRouteHost(destination);

        LOG.debug("Proxy destination {}", destination);

        return null;
    }


    private URL getDestinationForRouting(RequestContext ctx) {

        URL destination = null;

        try {
            double distance = calculateAerialDistanceFromUrl(ctx.getRequest().getRequestURI());
            destination = distance > limitInMeters ? destinationOverLimit : destinationUnderLimit;
        } catch (NoCoordinatesException e) {
            LOG.error("No coordinates were found: {}", e.getRequestURI(), e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            LOG.error("Calculation of aerial distance {} had errors", ctx.getRequest().getRequestURI(), e);
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

            return aerialDistanceCalculationService.calculateAerialDistanceInMeters(lonA, latA, lonB, latB);
        } else {
            throw new NoCoordinatesException(requestURI);
        }
    }
}
