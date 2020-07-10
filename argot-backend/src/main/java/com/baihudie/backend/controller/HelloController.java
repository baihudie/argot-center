package com.baihudie.backend.controller;

import com.baihudie.api.common.WebRes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(path = "/hello")
    public WebRes hello(String name) {

        System.out.println("hello " + name);

        return WebRes.success("hello " + name);
    }

}
