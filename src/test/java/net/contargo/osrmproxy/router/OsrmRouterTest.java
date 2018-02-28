package net.contargo.osrmproxy.router;

import java.util.regex.Pattern;


/**
 * Unit test for {@link net.contargo.osrmproxy.router.OsrmRouter}.
 *
 * @author  Ben Antony - antony@synyx.de
 */
public class OsrmRouterTest {

    private static final Pattern PROFILE_PATTERN = Pattern.compile(
            "^/osrm/(?<service>.*)/(?<version>.*)/(?<profile>.*)/.*$");
}
