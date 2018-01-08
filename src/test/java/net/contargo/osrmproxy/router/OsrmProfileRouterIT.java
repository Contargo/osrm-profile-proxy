package net.contargo.osrmproxy.router;

import com.github.tomakehurst.wiremock.WireMockServer;

import net.contargo.osrmproxy.OsrmProfileProxyApplication;
import net.contargo.osrmproxy.config.MockConfiguration;
import net.contargo.osrmproxy.config.ProfilesProperties;

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
 * Integration test for {@link net.contargo.osrmproxy.router.OsrmProfileRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OsrmProfileProxyApplication.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles({ "integration", "dev" })
class OsrmProfileRouterIT {

    private static final String PATH_PREFIX_OSRM = "/osrm";
    private static final String PATH_PREFIX_ROUTE = "/route/v1/driving/";
    private static final String PATH_PREFIX_OTHER = "/table/foo";
    private static final String PATH_OVER_LIMIT =
        "10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true";
    private static final String PATH_UNDER_LIMIT =
        "8.745117,52.268157;8.828888,52.174774?overview=false&hints=;&alternatives=true&steps=true";
    private static final String HOST_STUB = "http://localhost:";
    private static final String PATH_MULTI_STOP =
        "13.31543,50.485474;8.723145,51.433464;8.404437,49.014068?overview=false&hints=;;&alternatives=true&steps=true";
    private static final String PATH_COORDS_ONLY = "13.31543,50.485474;8.723145,51.433464;8.404437,49.014068";

    @LocalServerPort
    private Integer port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private ProfilesProperties profilesProperties;

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

        assertPath(PATH_PREFIX_ROUTE + PATH_OVER_LIMIT, "/mapsOverLimit");
        assertPath(PATH_PREFIX_ROUTE + PATH_MULTI_STOP, "/mapsOverLimit");
        assertPath(PATH_PREFIX_ROUTE + PATH_COORDS_ONLY, "/mapsOverLimit");
        assertPath(PATH_PREFIX_ROUTE + PATH_UNDER_LIMIT, "/mapsUnderLimit");
        assertPath(PATH_PREFIX_OTHER, "/mapsFallback");
    }


    @Test
    void testForRegexCoordinates() {

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


    private ResponseEntity<String> executeRequest(String coordinates) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", APPLICATION_JSON_VALUE);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = HOST_STUB + port + PATH_PREFIX_OSRM + PATH_PREFIX_ROUTE + coordinates;

        return restTemplate.exchange(url, GET, entity, String.class);
    }


    private void assertError(String coordinates) {

        String body = executeRequest(coordinates).getBody();
        assertThat(body).contains(
            "You arrived here because the request wasn't a routing request or there was an error while determine the aerial distance for the routing request");
        assertThat(body).contains("this is the response from a mocked server named '/mapsFallback'");
    }


    private void assertNoError(String coordinates) {

        String body = executeRequest(coordinates).getBody();
        assertThat(body).contains("this is the response from a mocked server named ");
        assertThat(body).doesNotContain("this is the response from a mocked server running on port "
            + profilesProperties.getFallbackDestination().getPort());
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
