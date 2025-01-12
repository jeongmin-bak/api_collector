package com.example.externalurl.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExternalRepository {
    List<Map<String, Object>> getAllUrlList();
    List<Map<String, Object>> getCodeGroup();
    String checkDuplicationUrl(String apiUrl);
    int insertApiJdbcInfo(Map<String, Object> jdbcInfo);
    int insertApiUrlInfo(Map<String, Object> apiUrlInfo);
    Map<String, Object> selectApiDetailInfo(Map<String, Object> selectMap);
    int updateJobId(Map<String, Object> userParamMap);
}
