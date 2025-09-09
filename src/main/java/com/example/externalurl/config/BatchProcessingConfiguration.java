package com.example.externalurl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.xml.parsers.DocumentBuilderFactory;

@Configuration
public class BatchProcessingConfiguration {
    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }

    @Bean
    public DocumentBuilderFactory documentBuilderFactory() { return DocumentBuilderFactory.newInstance(); }
    
}