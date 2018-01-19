package net.contargo.osrmproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

import org.springframework.context.annotation.Bean;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import org.springframework.web.client.RestTemplate;


/**
 * @author  Ben Antony - antony@synyx.de
 * @author  Sandra Thieme - thieme@synyx.de
 */
@SpringBootApplication
@EnableZuulProxy
public class OsrmProfileProxyApplication {

    @Bean
    public RestTemplate restTemplate() {

        HttpComponentsClientHttpRequestFactory clientRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientRequestFactory.setReadTimeout(200);

        return new RestTemplate(clientRequestFactory);
    }


    public static void main(String[] args) {

        SpringApplication.run(OsrmProfileProxyApplication.class, args);
    }
}
