package net.contargo.osrmproxy.config;

import net.contargo.osrmproxy.mock.ServerMock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URL;


/**
 * Configuration an instances of {@link net.contargo.osrmproxy.mock.ServerMock} for local and integration testing.
 *
 * @author  Ben Antony - antony@synyx.de
 */
@Configuration
public class MockConfiguration {

    private ServerMock serverMock;

    @Profile("dev")
    @Bean(destroyMethod = "destroy")
    public MockConfiguration createTwoMapsServerMocks(ProxyProperties proxyProperties) {

        Integer port = proxyProperties.getProfiles().getLimit().get("driving").getAllDestinations().get(0).getPort();
        serverMock = new ServerMock(port);

        proxyProperties.getProfiles().getLimit().values().forEach(this::configLimitMock);
        proxyProperties.getProfiles().getSimple().values().forEach(this::configSimpleMock);

        return this;
    }


    private void configSimpleMock(SimpleProfileProperties properties) {

        String serverName = getServerName(properties.getDestination());
        serverMock.configMock(serverName, "You arrived here because of the specified profile");
    }


    private void configLimitMock(LimitProfileProperties properties) {

        double limitInMeters = properties.getLimitInMeters();
        LimitProfileProperties.Destinations destinations = properties.getDestinations();

        serverMock.configMock(getServerName(destinations.getUnderLimit()),
            "You arrived here because the aerial distance of your request is under the limit of "
            + limitInMeters);

        serverMock.configMock(getServerName(destinations.getOverLimit()),
            "You arrived here because the aerial distance of your request is over the limit of "
            + limitInMeters);

        serverMock.configMock(getServerName(destinations.getFallback()),
            "You arrived here because the request wasn't a routing request or "
            + "there was an error while determine the aerial distance for the routing request");
    }


    private static String getServerName(URL destinationUnderLimit) {

        return destinationUnderLimit.getPath();
    }


    public void destroy() {

        serverMock.destroy();
    }


    public ServerMock getServerMock() {

        return serverMock;
    }
}
