package net.contargo.osrmproxy.health;

import net.contargo.osrmproxy.config.ProfilesProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import org.springframework.test.web.client.MockRestServiceServer;

import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

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
 */
class OsrmHealthIndicatorTest {

    private OsrmHealthIndicator sut;
    private MockRestServiceServer serverMock;

    @BeforeEach
    void setUp() throws MalformedURLException {

        ProfilesProperties properties = new ProfilesProperties();

        ProfilesProperties.Destinations destinations = new ProfilesProperties.Destinations();
        destinations.setOverLimit(new URL("http://example.com:5000"));
        destinations.setUnderLimit(new URL("http://example.com:5001"));
        destinations.setFallback(new URL("http://example.com:5002"));
        properties.setDestinations(destinations);

        ProfilesProperties.HealthCoordinates healthCoordinates = new ProfilesProperties.HealthCoordinates();

        ProfilesProperties.Coordinates start = new ProfilesProperties.Coordinates();
        start.setLatitude(52.517037);
        start.setLongitude(13.388860);

        healthCoordinates.setStart(start);

        ProfilesProperties.Coordinates end = new ProfilesProperties.Coordinates();
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

        serverMock.expect(requestTo("http://example.com:5000/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5001/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5002/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));

        Health health = sut.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }


    @Test
    void badHealth() {

        serverMock.expect(requestTo("http://example.com:5000/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5001/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withSuccess("{}", APPLICATION_JSON));
        serverMock.expect(requestTo("http://example.com:5002/route/v1/driving/13.388860,52.517037;8.404437,49.014068"))
            .andExpect(method(GET))
            .andRespond(withServerError());

        Health health = sut.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
