package com.example.externalurl.repository;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface MySqlMapper {
    String getCodeGroupId(Map<String, Object> inputParam);
}
