package net.contargo.osrmproxy.service;

/**
 * Provides aerial distance calculation.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public interface AerialDistanceCalculationService {

    /**
     * Calculates the aerial distance between two geographical points in meters.
     *
     * @param  longitudeA  the longitude part of the origin point
     * @param  latitudeA  the latitude part of the origin point
     * @param  longitudeB  the longitude part of the destination point
     * @param  latitudeB  the latitude part of the destination point
     *
     * @return  The distance between origin and destination, in meters
     */
    double calculateAerialDistanceInMeters(double longitudeA, double latitudeA, double longitudeB, double latitudeB);
}
