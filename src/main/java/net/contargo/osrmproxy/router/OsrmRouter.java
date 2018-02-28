package net.contargo.osrmproxy.router;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import net.contargo.osrmproxy.config.ProxyProperties;

import org.springframework.stereotype.Component;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;


/**
 * The root router. It knows all the limit and simple routers with their names. It chooses the router based on the
 * profile in the url and redirects to the host specified by the router.
 *
 * @author  Ben Antony - antony@synyx.de
 */
@Component
public class OsrmRouter extends ZuulFilter {

    private static final String ROUTING_URL_PREFIX = "/osrm/route/";
    private static final Pattern PROFILE_PATTERN = Pattern.compile(
            "^/osrm/(?<service>.*)/(?<version>.*)/(?<profile>.*)/.*$");
    private final OsrmProfileRouter fallback;

    private Map<String, OsrmProfileRouter> routers;

    public OsrmRouter(ProxyProperties proxyProperties) {

        routers = new HashMap<>();

        ProxyProperties.Profiles profiles = proxyProperties.getProfiles();
        profiles.getSimple().forEach((key, value) -> {
            OsrmProfileRouter router = new OsrmSimpleRouter(value);
            routers.put(key, router);
        });
        profiles.getLimit().forEach((key, value) -> {
            OsrmProfileRouter router = new OsrmLimitRouter(value);
            routers.put(key, router);
        });

        this.fallback = routers.get(profiles.getFallback());
    }

    @Override
    public String filterType() {

        return ROUTE_TYPE;
    }


    @Override
    public int filterOrder() {

        return 0;
    }


    @Override
    public boolean shouldFilter() {

        return true;
    }


    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();

        String requestUri = ctx.getRequest().getRequestURI();
        Boolean isRoutingRequest = requestUri.startsWith(ROUTING_URL_PREFIX);
        Matcher matcher = PROFILE_PATTERN.matcher(requestUri);

        if (matcher.matches()) {
            String profile = matcher.group("profile");

            URL destination = routers.getOrDefault(profile, fallback).getDestination(requestUri, isRoutingRequest);
            ctx.setRouteHost(destination);
        }

        return null;
    }
}
