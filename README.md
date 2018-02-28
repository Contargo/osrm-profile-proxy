# OSRM Profile Proxy

[![Build Status](https://travis-ci.org/Contargo/osrm-profile-proxy.svg?branch=master)](https://travis-ci.org/Contargo/osrm-profile-proxy)

OSRM profile proxy is a router which proxies [OSRM](http://project-osrm.org/)
routing requests to different OSRM instances depending on the profile parameter.
Additionally profiles can be proxied with different mechanisms, e.g. by
calculating the aerial distance between start and end of the route.

## How to configure proxies

This example will configure three profiles: `rail`, `water` and `driving`.
`rail` and `water` are simple proxies (see
[How a simple proxy works](#how-a-simple-proxy-works)) and `driving` is a limit
proxy (see [How a limit proxy works](#how-a-limit-proxy-works)).

Notice that `rail`, `water` and `driving` are just names, you can change them to
your needs and add or remove other profiles in the proxy categories (`simple`
and `limit`). In case no proxy of a certain type is needed, omit that category.

In order to perform a health check over all OSRM instances, a routing request
based on the coordinates under `healthCoordinates` is performed for each 
instance. For more details see [Health check](#health-check).

```yaml
proxy:
  profiles:
    fallback: driving
    simple:
      rail:
        destination: http://localhost:50003
      water:
        destination: http://localhost:50004
      # define as many simple proxies as you need
    limit:
      driving:
        limitInMeters: 75000
        destinations:
          underLimit: http://localhost:50001
          overLimit: http://localhost:50002
          fallback: http://localhost:50001
      # define as many limit proxies as you need
  healthCoordinates:
    start: #Berlin
      latitude: 52.517037
      longitude: 13.388860
    end: #Karlsruhe
      latitude: 49.014068
      longitude: 8.404437
```

## How a simple proxy works
This is an example OSRM routing request to an OSRM instance on port 8080 with
profile `rail`:
http://maps.example.com:8080/osrm/route/v1/rail/10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true

Notice at the beginning of the url path there is `/osrm`, this is needed
for the proxy to recognize requests for OSRM. This will be cut out when proxied.

Start an OSRM instance on port 5003. When the `profile`-parameter in the url
matches the proxy-name (in this case `rail`), then the request will be proxied
to the given destination (in this case `http://localhost:50003`).


## How a limit proxy works

This is an example OSRM routing request to an OSRM instance on port 8080 with
profile `driving`:
http://maps.example.com:8080/osrm/route/v1/driving/10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true

Notice at the beginning of the url path there is `/osrm`, this is needed
for the proxy to recognize requests for OSRM. This will be cut out when proxied.

Start two OSRM instances on different ports e.g. 5001 and 5002. The
coordinates will be extracted from the url and the aerial distance will be
calculated. Depending on the result, the request will be proxied to the
corresponding OSRM instance e.g. http://localhost:5001 or http://localhost:5002
(which can be configured with the properties
`profiles.proxy.limit.driving.destinations.underLimit` and
`profiles.proxy.limit.driving.destinations.overLimit`). The limit itself is
configured with `profiles.proxy.limit.driving.limitInMeters`.

Requests which are not routing requests (e.g. table requests) will be proxied
to a fallback destination which can be configured with the property
`profiles.proxy.limit.driving.destinations.fallback`. This destination should
be the same as one of the two OSRM instances. In case there was an error while
processing the routing request (e.g. coordinates which this implementation can't
read) the request will be proxied to the fallback destination.

In case of a multistop routing request (more than just two pairs of coordinates)
the aerial distance of the first and the last coordinate pair will be used to
decide which instance should be used.

## Health check

The application provides a health check at `/health`. That check performs a
routing with all OSRM instances which are used by the proxies.It will
report `UP` if all OSRM instances routed successfully, and `DOWN` otherwise. The
routing can be influenced with the `start` and `end` parameters under
`proxy.healthCoordinates`.

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
functionality by accessing the server via browser with a URL for OSRM
instances.
e.g.
[aerial distance under the limit](http://localhost:8080/osrm/route/v1/driving/8.745117,52.268157;8.828888,52.174774?overview=false&alternatives=true&steps=true&hints=;),
[aerial distance over the limit](http://localhost:8080/osrm/route/v1/driving/10.415039,50.148746;8.525391,52.829321?overview=false&alternatives=true&steps=true)
or
[not a route request](http://localhost:8080/osrm/table/v1/driving/10.415039,50.148746)
for the driving profile with the limit proxy.
[Profile rail](http://localhost:8080/osrm/route/v1/rail/8.745117,52.268157;8.828888,52.174774?overview=false&alternatives=true&steps=true&hints=;)
for the rail profile with the simple proxy.

The response will not look like a response from an OSRM instance, but it will
provide some useful information.
