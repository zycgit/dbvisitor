package com.example.demo;

import net.hasor.db.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@MapperScan(basePackages = "com.example.demo.dao", mapperLocations = "classpath:hasordb/mapper/*.xml")
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
