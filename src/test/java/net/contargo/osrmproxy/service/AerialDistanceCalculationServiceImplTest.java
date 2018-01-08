package net.contargo.osrmproxy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link net.contargo.osrmproxy.service.AerialDistanceCalculationServiceImpl}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
class AerialDistanceCalculationServiceImplTest {

    private AerialDistanceCalculationService sut;

    @BeforeEach
    void setUp() {

        sut = new AerialDistanceCalculationServiceImpl();
    }


    @Test
    void calculateAerialDistanceInMeters() {

        double expected = 132713.63654633463;
        double latA = 49.015;
        double lonA = 8.42;
        double latB = 50.015;
        double lonB = 9.42;

        assertThat(sut.calculateAerialDistanceInMeters(lonA, latA, lonB, latB)).isEqualTo(expected);
    }
}
