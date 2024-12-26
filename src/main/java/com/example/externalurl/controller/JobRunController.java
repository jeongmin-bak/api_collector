package com.example.externalurl.controller;

import com.example.externalurl.service.JobRunService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("jobs")
@RequiredArgsConstructor
public class JobRunController {
    @Autowired
    JobRunService jobRunService;

    @PostMapping("/run")
    public void jonRunRequest(@RequestBody Map<String, Object> metaInfo){
        log.info("start api url request processing start! ");

        // 화면에서 넘어 온 파라미터들은 json으로 저장된다.




    }

}
