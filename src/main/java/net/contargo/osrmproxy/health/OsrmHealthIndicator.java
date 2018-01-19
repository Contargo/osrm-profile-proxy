package net.contargo.osrmproxy.health;

import net.contargo.osrmproxy.config.ProfilesProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;

import java.net.URL;

import java.util.Locale;
import java.util.stream.Stream;


/**
 * @author  Sandra Thieme - thieme@synyx.de
 */
@Component
public class OsrmHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProfilesProperties profilesProperties;
    private final RestTemplate restTemplate;

    public OsrmHealthIndicator(ProfilesProperties profilesProperties, RestTemplate restTemplate) {

        this.profilesProperties = profilesProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {

        Health.Builder builder = new Health.Builder(Status.UP);

        Stream.of(profilesProperties.getDestinationOverLimit(), profilesProperties.getDestinationUnderLimit(),
                profilesProperties.getFallbackDestination())
            .filter(this::isDown)
            .forEach(u -> builder.down());

        return builder.build();
    }


    private boolean isDown(URL url) {

        ProfilesProperties.HealthCoordinates c = profilesProperties.getHealthCoordinates();

        String request = String.format(Locale.ENGLISH, "%s/route/v1/driving/%f,%f;%f,%f", url, c.getStartLongitude(),
                c.getStartLatitude(), c.getEndLongitude(), c.getEndLatitude());

        LOG.debug("Health checking {}", request);

        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(request, String.class);

            return entity.getStatusCode() != HttpStatus.OK;
        } catch (RestClientException e) {
            return true;
        }
    }
}
