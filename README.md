# OSRM Profile Proxy

[![Build Status](https://travis-ci.org/Contargo/osrm-profile-proxy.svg?branch=master)](https://travis-ci.org/Contargo/osrm-profile-proxy)

OSRM profile proxy is a router which proxies [OSRM](http://project-osrm.org/)
routing requests to different OSRM instances depending on the aerial distance
between start and end of the route.

## How it works

This is an example OSRM routing request to an OSRM instance on port 5000:
http://maps.example.com:5000/osrm/route/v1/driving/10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true

Notice at the beginning of the url path there is `/osrm`, this is needed
for the proxy to recognize requests for OSRM. This will be cut out when proxied.

Start two OSRM instances on different ports e.g. 5001 and 5002. Start the
OSRM-profile-proxy on port 5000, so that it will process the requests. The
coordinates will be extracted from the url and the aerial distance will be
calculated. Depending on the result, the request will be proxied to the
corresponding OSRM instance e.g. http://localhost:5001 or http://localhost:5002
(which can be configured with the properties `profiles.destinations.underLimit`
and `profiles.destinations.overLimit`). The limit itself is configured with
`profiles.limitInMeters`.

Requests which are not routing requests (e.g. table requests) will be proxied
to a fallback destination which can be configured with the property
`profiles.destinations.fallback`. This destination should be the same as one of
the two OSRM-instances. In case there was an error while processing the routing
request (e.g. coordinates which this implementation can't read) the request
will be proxied to the fallback destination.

In case of a multistop routing request (more than just two pairs of coordinates)
the aerial distance of the first and the last coordinate pair will be used to
decide which instance should be used.

Here is an example configuration. This configuration leads to all routing
requests with a distance of 75 km or less to be proxied to
`http://localhost:5001`, those with more than 75 km to be proxied to
`http://localhost:5002`. Non routing requests will be proxied to
`http://localhost:5001`

```yaml
profiles:
  limitInMeters: 75000
  destinations:
      underLimit: http://localhost:5001
      overLimit: http://localhost:5002
      fallback: http://localhost:5001
```

## Development

To project is built with maven:

### Build

```bash
./mvnw clean verify
```

### Run locally

```bash
./mvnw spring-boot:run
```

This will start the proxy locally on port 8080. When the application is started
in `dev`-profile, a wiremock server will be started as well. You can test the
functionality by accessing the server via the browser with an url for OSRM
instances.
e.g. [aerial distance under the limit](http://localhost:8080/osrm/route/v1/driving/8.745117,52.268157;8.828888,52.174774?overview=false&alternatives=true&steps=true&hints=;),
[aerial distance over the limit](http://localhost:8080/osrm/route/v1/driving/10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true)
or
[not a route request](http://localhost:8080/osrm/table)

The response will not look like a response from an OSRM-instance, but it will
provide some useful information.
