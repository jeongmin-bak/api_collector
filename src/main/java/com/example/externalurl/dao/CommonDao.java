//package com.example.externalurl.dao;
//
//import java.util.List;
//import java.util.Map;
//
//@Mapper
//public interface CommonDao {
//
//    int createJobHistory(Map<String, Object> inputMap);
//
//    int startJobHistory(Map<String, Object> inputMap);
//
//    int updateJobDetailTotCntHistory(Map<String, Object> inputMap);
//
//    int updateJobDetailRunCntHistory(Map<String, Object> inputMap);
//
//    int endJobHistory(Map<String, Object> inputMap);
//
//    int errorJobHistory(Map<String, Object> inputMap);
//
//    int startJobDetailHistory(Map<String, Object> inputMap);
//
//    int endJobDetailHistory(Map<String, Object> inputMap);
//
//    int endJobDetailAllCntHistory(Map<String, Object> inputMap);
//
//    int updateFileDataType(Map<String, Object> inputMap);
//
//    int errorJobDetailHistory(Map<String, Object> inputMap);
//
//    int stopJobDetailHistory(Map<String, Object> inputMap);
//
//    Map<String, Object> selectJobByFileId(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectJobIdByStatus(List<String> status);
//
//    String selectConfigValueByKey(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectAllConfigValue();
//
//    int deleteVectorByFileId(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectImageList(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectGridList(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectSimilarVectorList(Map<String, Object> inputMap);
//
//    List<String> selectQuestionListByFileId(Map<String, Object> inputMap);
//
//    List<String> selectQuestionListByVectorId(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectMetaDataByVectorId(Map<String, Object> params);
//
//    int insertVideoMeta(Map<String, Object> inputMap);
//
//    List<Map<String, Object>> selectVideoMetaList(Map<String, Object> inputMap);
//
//    int insertAuxGrid(Map<String, Object> inputMap);
//
//    int deleteImageByFileId(Map<String, Object> inputMap);
//
//    int deleteGridByFileId(Map<String, Object> inputMap);
//
//    int resetProcTypeByFileId(Map<String, Object> inputMap);
//
//    int deleteFileQuestionByFileId(Map<String, Object> inputMap);
//
//    int deleteParagraphQuestionByFileId(Map<String, Object> inputMap);
//}
//
