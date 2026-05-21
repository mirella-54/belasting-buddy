package com.group8.usermanagement.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig {
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
