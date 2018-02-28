package net.contargo.osrmproxy.router;

import net.contargo.osrmproxy.config.LimitProfileProperties;
import net.contargo.osrmproxy.config.LimitProfileProperties.Destinations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link net.contargo.osrmproxy.router.OsrmLimitRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
class OsrmLimitRouterTest {

    private static final String PATH_PREFIX_OSRM = "/osrm";
    private static final String PATH_PREFIX_ROUTE = "/route/v1/driving/";
    private static final String HOST_STUB = "http://localhost:8080";

    private OsrmLimitRouter sut;

    @BeforeEach
    void setUp() throws MalformedURLException {

        Destinations destinations = new Destinations();
        destinations.setUnderLimit(new URL("http://localhost:50002/mapsUnderLimit"));
        destinations.setOverLimit(new URL("http://localhost:50002/mapsOverLimit"));
        destinations.setFallback(new URL("http://localhost:50002/mapsFallback"));

        LimitProfileProperties properties = new LimitProfileProperties();
        properties.setLimitInMeters(75000);
        properties.setDestinations(destinations);

        sut = new OsrmLimitRouter(properties);
    }


    @Test
    void testForRegexCoordinates() throws MalformedURLException {

        assertNoError("1.2,32.4;4.23,5.98");
        assertNoError("20.126953,-33.578015;24.257813,70.229744");
        assertNoError("138.8671875,53.9560855309879;27.949219,67.94165");
        assertNoError("-79.27734374999999,43.96119063892024;27.949219,67.94165");
        assertError("90.0000000001,90.0000000001;27.949219,67.94165");
        assertError("90.0000000001,-90.0000000001;27.949219,67.94165");
        assertError("27.949219,67.94165;90.0000000001,-90.0000000001");
        assertError("27.949219,67.94165;90.0000000001,90.0000000001");
        assertError("27.949219,67.94165;90.0000000001,900000000001");
        assertError("27949219,67.94165;90.0000000001,90.0000000001");
    }


    private void assertNoError(String coordinates) throws MalformedURLException {

        assertThat(sut.getDestination(generateUrl(coordinates), true)).isEqualTo(new URL(
                "http://localhost:50002/mapsOverLimit"));
    }


    private void assertError(String coordinates) throws MalformedURLException {

        assertThat(sut.getDestination(generateUrl(coordinates), true)).isEqualTo(new URL(
                "http://localhost:50002/mapsFallback"));
    }


    private static String generateUrl(String coordinates) {

        return HOST_STUB + PATH_PREFIX_OSRM + PATH_PREFIX_ROUTE + coordinates;
    }
}
