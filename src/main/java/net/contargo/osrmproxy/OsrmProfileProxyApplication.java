package net.contargo.osrmproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.netflix.zuul.EnableZuulProxy;


@SpringBootApplication
@EnableZuulProxy
public class OsrmProfileProxyApplication {

    public static void main(String[] args) {

        SpringApplication.run(OsrmProfileProxyApplication.class, args);
    }
}
