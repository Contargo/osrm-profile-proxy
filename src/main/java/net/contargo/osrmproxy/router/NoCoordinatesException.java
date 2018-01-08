package net.contargo.osrmproxy.router;

/**
 * Is thrown when the calculation of the aerial distance based on the url fails.
 *
 * @author  Ben Antony - antony@synyx.de
 */
class NoCoordinatesException extends RuntimeException {

    private static final long serialVersionUID = -1841340576072789771L;

    private final String requestURI;

    NoCoordinatesException(String requestURI) {

        super("The requested URL: " + requestURI);
        this.requestURI = requestURI;
    }

    String getRequestURI() {

        return requestURI;
    }
}
