package net.contargo.osrmproxy.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link net.contargo.osrmproxy.service.AerialDistanceCalculation}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
class AerialDistanceCalculationTest {

    @Test
    void calculateAerialDistanceInMeters() {

        double expected = 132713.63654633463;
        double latA = 49.015;
        double lonA = 8.42;
        double latB = 50.015;
        double lonB = 9.42;

        assertThat(AerialDistanceCalculation.calculateAerialDistanceInMeters(lonA, latA, lonB, latB)).isEqualTo(
            expected);
    }
}
