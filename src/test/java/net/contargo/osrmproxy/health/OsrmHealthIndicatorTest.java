package net.contargo.osrmproxy.health;

import net.contargo.osrmproxy.config.LimitProfileProperties;
import net.contargo.osrmproxy.config.LimitProfileProperties.Destinations;
import net.contargo.osrmproxy.config.ProxyProperties;
import net.contargo.osrmproxy.config.ProxyProperties.Coordinates;
import net.contargo.osrmproxy.config.ProxyProperties.HealthCoordinates;
import net.contargo.osrmproxy.config.ProxyProperties.Profiles;
import net.contargo.osrmproxy.config.SimpleProfileProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import org.springframework.test.web.client.MockRestServiceServer;

import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.client.MockRestServiceServer.createServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


/**
 * @author  Sandra Thieme - thieme@synyx.de
 * @author  Ben Antony - antony@synyx.de
 */
class OsrmHealthIndicatorTest {

    private OsrmHealthIndicator sut;
    private MockRestServiceServer serverMock;

    @BeforeEach
    void setUp() throws MalformedURLException {

        Destinations destinations = new Destinations();
        destinations.setOverLimit(new URL("http://example.com:5000"));
        destinations.setUnderLimit(new URL("http://example.com:5001"));
        destinations.setFallback(new URL("http://example.com:5000"));

        LimitProfileProperties limitProfileProperties = new LimitProfileProperties();
        limitProfileProperties.setDestinations(destinations);
        limitProfileProperties.setLimitInMeters(75000);

        SimpleProfileProperties simpleProfileProperties = new SimpleProfileProperties();
        simpleProfileProperties.setDestination(new URL("http://example.com:5003"));

        Profiles profiles = new Profiles();

        Map<String, LimitProfileProperties> limitProfiles = new HashMap<>();
        limitProfiles.put("driving", limitProfileProperties);
        profiles.setLimit(limitProfiles);

        Map<String, SimpleProfileProperties> simpleProfiles = new HashMap<>();
        simpleProfiles.put("rail", simpleProfileProperties);
        profiles.setSimple(simpleProfiles);

        ProxyProperties properties = new ProxyProperties();
        properties.setProfiles(profiles);

        HealthCoordinates healthCoordinates = new HealthCoordinates();

        Coordinates start = new Coordinates();
        start.setLatitude(52.517037);
        start.setLongitude(13.388860);

        healthCoordinates.setStart(start);

        Coordinates end = new Coordinates();
        end.setLatitude(49.014068);
        end.setLongitude(8.404437);
        healthCoordinates.setEnd(end);

        properties.setHealthCoordinates(healthCoordinates);

        RestTemplate restTemplate = new RestTemplate();
        serverMock = createServer(restTemplate);

        sut = new OsrmHealthIndicator(properties, restTemplate);
    }


    @Test
    void goodHealth() {

        serverMock.expect(requestTo("http://example.com:5003/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5001/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5000/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));

        Health health = sut.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }


    @Test
    void badHealth() {

        serverMock.expect(requestTo("http://example.com:5003/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withServerError());
        serverMock.expect(requestTo("http://example.com:5001/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5000/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));

        Health health = sut.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
