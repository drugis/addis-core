package org.drugis.addis.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)}, basePackages = {
        "org.drugis.addis.trialverse.service"
})
public class TrialverseServiceIntegrationTestConfig {

        @Bean
        public RestTemplate restTemplate() {
                RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient()));
                return restTemplate;
        }

        @Bean
        public HttpClient httpClient() {
                return HttpClientBuilder
                        .create()
                        .setMaxConnTotal(20)
                        .setMaxConnPerRoute(2)
                        .build();
        }
}

