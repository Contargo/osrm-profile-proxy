package net.contargo.osrmproxy.router;

import com.github.tomakehurst.wiremock.WireMockServer;

import net.contargo.osrmproxy.OsrmProfileProxyApplication;
import net.contargo.osrmproxy.config.MockConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Integration test for {@link net.contargo.osrmproxy.router.OsrmRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OsrmProfileProxyApplication.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles({ "integration", "dev" })
class OsrmRouterIT {

    private static final String PATH_PREFIX_OSRM = "/osrm";
    private static final String PATH_PREFIX_ROUTE = "/route/v1/";
    private static final String PATH_PREFIX_OTHER = "/table/v1/driving/";
    private static final String PATH_OVER_LIMIT =
        "10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true";
    private static final String PATH_UNDER_LIMIT =
        "8.745117,52.268157;8.828888,52.174774?overview=false&hints=;&alternatives=true&steps=true";
    private static final String HOST_STUB = "http://localhost:";
    private static final String PATH_MULTI_STOP =
        "13.31543,50.485474;8.723145,51.433464;8.404437,49.014068?overview=false&hints=;;&alternatives=true&steps=true";
    private static final String PATH_COORDS_ONLY = "13.31543,50.485474;8.723145,51.433464;8.404437,49.014068";
    private static final String DRIVING = "driving/";

    @LocalServerPort
    private Integer port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private MockConfiguration mockConfiguration;
    private WireMockServer wireMockServer;
    private String rootUrl;

    @BeforeEach
    void setUp() {

        rootUrl = HOST_STUB + port + PATH_PREFIX_OSRM;
        wireMockServer = mockConfiguration.getServerMock().getWireMockServer();
    }


    @Test
    void testRouter() {

        assertPath(PATH_PREFIX_ROUTE + DRIVING + PATH_OVER_LIMIT, "/mapsOverLimit");
        assertPath(PATH_PREFIX_ROUTE + DRIVING + PATH_MULTI_STOP, "/mapsOverLimit");
        assertPath(PATH_PREFIX_ROUTE + DRIVING + PATH_COORDS_ONLY, "/mapsOverLimit");
        assertPath(PATH_PREFIX_ROUTE + DRIVING + PATH_UNDER_LIMIT, "/mapsUnderLimit");
        assertPath(PATH_PREFIX_ROUTE + "rail/" + PATH_UNDER_LIMIT, "/mapsRail");
        assertPath(PATH_PREFIX_ROUTE + "water/" + PATH_UNDER_LIMIT, "/mapsWater");

        // driving is defined ass fallback profile
        assertPath(PATH_PREFIX_ROUTE + "different/" + PATH_UNDER_LIMIT, "/mapsFallbackProxy");

        assertPath(PATH_PREFIX_OTHER, "/mapsFallback");
    }


    private ResponseEntity<String> executeRequest(String coordinates) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = HOST_STUB + port + PATH_PREFIX_OSRM + PATH_PREFIX_ROUTE + coordinates;

        return restTemplate.exchange(url, GET, entity, String.class);
    }


    private void assertPath(String path, String serverName) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = rootUrl + path;
        ResponseEntity<String> exchange = restTemplate.exchange(url, GET, entity, String.class);

        assertThat(exchange.getBody()).contains(serverName);

        wireMockServer.verify(getRequestedFor(urlEqualTo(serverName + path)));
    }
}
