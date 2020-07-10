package com.baihudie.web.controller;

import com.baihudie.api.common.WebRes;
import com.baihudie.web.mapper.ArgotUsersMapper;
import com.baihudie.web.model.ArgotUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private ArgotUsersMapper argotUsersMapper;

    @GetMapping(path = "/hello")
    public WebRes hello(String name) {

        System.out.println("hello " + name);

//        Users users = new Users();
//        users.setUserName("kitty");
//        users.setPasswd("kitty");
//        users.setState("0");
//        users.setMobile("13512345678");
//
//        usersMapper.insertUsers(users);

        ArgotUsers users = argotUsersMapper.getArgotUsersById(1);

        return WebRes.success("hello " + name + users);
    }

}
