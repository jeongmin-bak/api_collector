package com.example.externalurl.config;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@MapperScan(value="", sqlSessionFactoryRef = "subSqlSessionFactory")
public class SubDatabaseConfig {

}
