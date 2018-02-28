package net.contargo.osrmproxy.router;

import net.contargo.osrmproxy.config.SimpleProfileProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link net.contargo.osrmproxy.router.OsrmSimpleRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
class OsrmSimpleRouterTest {

    private OsrmSimpleRouter sut;

    @BeforeEach
    void setUp() throws MalformedURLException {

        SimpleProfileProperties properties = new SimpleProfileProperties();
        properties.setDestination(new URL("http://localhost:5003"));
        sut = new OsrmSimpleRouter(properties);
    }


    @Test
    void getDestinationIsRoutingRequest() throws MalformedURLException {

        URL result = sut.getDestination("http://example.com/osrm/route/v1/rail/50.234234,8.4324234", true);
        assertThat(result).isEqualTo(new URL("http://localhost:5003"));
    }


    @Test
    void getDestinationIsNotRoutingRequest() throws MalformedURLException {

        URL result = sut.getDestination("http://example.com/osrm/route/v1/rail/50.234234,8.4324234", false);
        assertThat(result).isEqualTo(new URL("http://localhost:5003"));
    }
}
