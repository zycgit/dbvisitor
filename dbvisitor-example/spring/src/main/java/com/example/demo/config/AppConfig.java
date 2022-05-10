package com.example.demo.config;

import net.hasor.dbvisitor.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(value = "com.example.demo.dao")
public class AppConfig {
}
