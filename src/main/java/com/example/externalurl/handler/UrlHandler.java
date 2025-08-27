package com.example.externalurl.handler;

import java.util.Map;

public interface UrlHandler {
    void handle(Map<String, Object> urlParam, Map<String, Object> jobInfo);
    
}