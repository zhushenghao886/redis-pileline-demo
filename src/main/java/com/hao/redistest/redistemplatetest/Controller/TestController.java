package com.hao.redistest.redistemplatetest.Controller;

import com.hao.redistest.redistemplatetest.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hao
 * @date 2020/4/27
 */
@RestController
@RequestMapping("/api")
public class TestController {

    private static final String SUCCESS = "write the path file to redis success!";

    @Autowired
    private TestService testService;

    @GetMapping("/write-redis")
    public Object writeToRedis(String path){

        testService.writeToRedis(path);

        return SUCCESS;

    }

}
