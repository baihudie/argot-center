package com.baihudie.web;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.baihudie"})
@MapperScan(basePackages = "com.baihudie")
public class WebApplication {

    public static void main(String[] args) {

        SpringApplication.run(WebApplication.class, args);
    }

}
