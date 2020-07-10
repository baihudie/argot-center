package com.baihudie.web.controller;

import com.baihudie.api.common.CommonRes;
import com.baihudie.core.mapper.UsersMapper;
import com.baihudie.core.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private UsersMapper usersMapper;

    @GetMapping(path = "/hello")
    public CommonRes hello(String name) {

        System.out.println("hello " + name);

//        Users users = new Users();
//        users.setUserName("kitty");
//        users.setPasswd("kitty");
//        users.setState("0");
//        users.setMobile("13512345678");
//
//        usersMapper.insertUsers(users);

        Users users = usersMapper.getUsersById(1);

        return CommonRes.success("hello " + name + users);
    }

}
