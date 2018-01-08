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
    public MockConfiguration createTwoMapsServerMocks(ProfilesProperties profilesProperties) {

        Integer port = profilesProperties.getDestinationOverLimit().getPort();
        serverMock = new ServerMock(port);

        double limitInMeters = profilesProperties.getLimitInMeters();

        serverMock.configMock(getServerName(profilesProperties.getDestinationUnderLimit()),
            "You arrived here because the aerial distance of your request is under the limit of " + limitInMeters);
        serverMock.configMock(getServerName(profilesProperties.getDestinationOverLimit()),
            "You arrived here because the aerial distance of your request is over the limit of " + limitInMeters);
        serverMock.configMock(getServerName(profilesProperties.getFallbackDestination()),
            "You arrived here because the request wasn't a routing request or "
            + "there was an error while determine the aerial distance for the routing request");

        return this;
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
