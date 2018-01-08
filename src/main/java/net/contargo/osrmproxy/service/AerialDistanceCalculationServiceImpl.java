package net.contargo.osrmproxy.service;

import org.geotools.referencing.GeodeticCalculator;

import org.springframework.stereotype.Service;


/**
 * @author  Ben Antony - antony@synyx.de
 */
@Service
public class AerialDistanceCalculationServiceImpl implements AerialDistanceCalculationService {

    @Override
    public double calculateAerialDistanceInMeters(double longitudeA, double latitudeA, double longitudeB,
        double latitudeB) {

        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(longitudeA, latitudeA);
        calculator.setDestinationGeographicPoint(longitudeB, latitudeB);

        return calculator.getOrthodromicDistance();
    }
}
