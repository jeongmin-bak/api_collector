package com.example.externalurl.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExternalRepository {
    List<Map<String, Object>> getAllUrlList();
    String checkDuplicationUrl(String apiUrl);
    int insertApiJdbcInfo(Map<String, Object> jdbcInfo);
    int insertApiUrlInfo(Map<String, Object> apiUrlInfo);
}
