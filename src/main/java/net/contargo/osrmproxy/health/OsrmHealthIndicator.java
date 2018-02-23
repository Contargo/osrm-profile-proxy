package net.contargo.osrmproxy.health;

import net.contargo.osrmproxy.config.LimitProfileProperties;
import net.contargo.osrmproxy.config.ProxyProperties;
import net.contargo.osrmproxy.config.SimpleProfileProperties;

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

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;


/**
 * @author  Sandra Thieme - thieme@synyx.de
 * @author  Ben Antony - antony@synyx.de
 */
@Component
public class OsrmHealthIndicator implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<URL> osrmInstances;
    private final RestTemplate restTemplate;
    private final ProxyProperties.HealthCoordinates coordinates;

    public OsrmHealthIndicator(ProxyProperties properties, RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
        this.coordinates = properties.getHealthCoordinates();

        ProxyProperties.Profiles profiles = properties.getProfiles();

        List<URL> instances = profiles.getSimple()
                .values()
                .stream()
                .map(SimpleProfileProperties::getDestination)
                .collect(toList());

        instances.addAll(profiles.getLimit()
            .values()
            .stream()
            .map(LimitProfileProperties::getAllDestinations)
            .flatMap(List::stream)
            .collect(toList()));

        this.osrmInstances = instances.stream().distinct().collect(toList());
    }

    @Override
    public Health health() {

        Health.Builder builder = new Health.Builder(Status.UP);

        osrmInstances.stream().filter(this::isDown).forEach(u -> builder.down());

        return builder.build();
    }


    private boolean isDown(URL url) {

        String request = String.format(Locale.ENGLISH, "%s/route/v1/driving/%f,%f;%f,%f", url,
                coordinates.getStartLongitude(), coordinates.getStartLatitude(), coordinates.getEndLongitude(),
                coordinates.getEndLatitude());

        LOG.debug("Health checking {}", request);

        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(request, String.class);

            return entity.getStatusCode() != HttpStatus.OK;
        } catch (RestClientException e) {
            return true;
        }
    }
}
