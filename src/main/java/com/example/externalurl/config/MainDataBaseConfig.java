//package com.example.externalurl.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.io.Resource;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//@MapperScan(value = "com.example.externalurl.repository.ExternalRepository", sqlSessionFactoryRef = "MainSqlSessionFactory")
//public class MainDataBaseConfig {
//    private final String mainDataSource = "MainDataSource";
//
//    @Primary
//    @Bean(mainDataSource)
//    @ConfigurationProperties(prefix = "spring.oracle.datasource.hikari")
//    public DataSource MainDataSource(){
//        return DataSourceBuilder.create()
//                .type(HikariDataSource.class)
//                .build();
//    }
//
//    @Primary
//    @Bean
//    public SqlSessionFactory MainSqlSessionFactory(@Qualifier(mainDataSource) DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//        bean.setDataSource(dataSource);
//
//        // MyBatis Mapper Source
//        Resource[] res = new PathMatchingResourcePatternResolver().getResources("classpath:mappers/oracle/*Mapper.xml");
//        bean.setMapperLocations(res);
//
//        // MyBatis Config Setting
//        Resource myBatisConfig = new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml");
//        bean.setConfigLocation(myBatisConfig);
//
//        return bean.getObject();
//    }
//
//    @Primary
//    @Bean
//    public DataSourceTransactionManager MainTransactionManager(@Qualifier(mainDataSource) DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//}
