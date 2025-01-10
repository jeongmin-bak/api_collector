//package com.example.externalurl.config;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//import java.io.IOException;
//
//@Configuration
//@RequiredArgsConstructor
//public class SubDatabaseConfig {
//    private final String mySqlDataSource = "MySqlDataSource";
//
//    @Value("${sub-db.url}")
//    private String jdbcUrl;
//
//    @Value("${sub-db.username}")
//    private String jdbcId;
//
//    @Value("${sub-db.password}")
//    private String jdbcPw;
//
//    @Value("${sub-db.driver}")
//    private String jdbcDriver;
//
//    @Value("${hikari.connection-timeout}")
//    private long connectionTimeout;
//
//    @Value("${hikari.max-lifetime}")
//    private long maxLifetime;
//
//    @Bean
//    public HikariDataSource dataSource() throws IOException {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(jdbcId);
//        config.setPassword(jdbcPw);
//        config.setDriverClassName(jdbcDriver);
//        config.setConnectionTimeout(connectionTimeout); // 유휴 커넥션 시간(밀리초)
//        config.setMaxLifetime(maxLifetime);
//
//        return new HikariDataSource(config);
//    }
//
//    @Bean
//    public JdbcTemplate jdbcTemplate(HikariDataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//
//}