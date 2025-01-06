package com.example.externalurl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import util.ShellCommandUtil;

@SpringBootApplication
public class ExternalUrlApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalUrlApplication.class, args);
    }


}
