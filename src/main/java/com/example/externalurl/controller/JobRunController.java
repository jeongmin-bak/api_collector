package com.example.externalurl.controller;

import com.example.externalurl.service.JobRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("jobs")
@RequiredArgsConstructor
public class JobRunController {
    @Autowired
    JobRunService jobRunService;

    @PostMapping("/run")
    public void jonRunRequest(@RequestBody Map<String, Object> metaInfo){

    }

}
