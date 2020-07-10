package com.baihudie.backend;

import com.baihudie.backend.server.MsgServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.baihudie"})
public class BackApplication implements CommandLineRunner {

    @Autowired
    private MsgServer msgServer;

    public static void main(String[] args) {

        SpringApplication.run(BackApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Thread thread2 = new Thread() {

            @Override
            public void run() {

                msgServer.startServer();
            }
        };
        thread2.start();

    }
}
