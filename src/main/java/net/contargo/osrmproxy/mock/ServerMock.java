package net.contargo.osrmproxy.mock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static wiremock.org.apache.http.HttpStatus.SC_OK;


/**
 * A mocked server for local testing.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public class ServerMock {

    private final WireMockServer wireMockServer;

    public ServerMock(Integer port) {

        wireMockServer = new WireMockServer(wireMockConfig().port(port));

        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
    }

    public void configMock(String serverName, String reason) {

        wireMockServer.stubFor(get(urlMatching(serverName + "/.*")).willReturn(
                aResponse().withStatus(SC_OK)
                    .withBody(generateMockedServerResponse(serverName, reason))
                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)));
    }


    private static String generateMockedServerResponse(String serverName, String reason) {

        return "{"
            + "\"message\": \"this is the response from a mocked server named '" + serverName + "'\","
            + "\"reason\": \"" + reason + "\""
            + "}";
    }


    public WireMockServer getWireMockServer() {

        return this.wireMockServer;
    }


    public void destroy() {

        wireMockServer.stop();
    }
}
