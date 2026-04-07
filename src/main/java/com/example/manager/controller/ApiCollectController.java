//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//
//@Slf4j
//@RestController
//@RequestMapping(value = "/dp/external")
//@RequiredArgsConstructor
//public class ApiCollectController {
////    private final DpHandler dpHandler;
////    private final RestApiClient restApiClient;
//    private final ScheduleRunner scheduleRunner;
//    private final NamedParameterJdbcOperations jdbc;
//
//    @SneakyThrows
//    @PostMapping("/run")
//    public void apiCollectRun(@RequestBody List<Map<String, Object>> param) {
//        log.info("외부데이터 작업 요청 파라미터 :{}", param);
//        AtomicReference<Map<String, Object>> submitParams = new AtomicReference<>();
//        /* 단위작업을 통해서 작업이 제출될 수 있는 개수는 N개 */
//        param.forEach(apiJobInfo -> {
//            log.info("외부데이터 기본 작업 정보 : {}", apiJobInfo);
//            String jbId = "JOB" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + UUID.randomUUID().toString().replaceAll("-", "").substring(0,9);
//            Map<String, Object> apiDetailInfo = (Map<String, Object>) apiJobInfo.get("API_SELECT");
//            apiDetailInfo.put("JB_ID", jbId);
//            apiDetailInfo.put("JB_CAL_DSC", "01"); // 작업구분코드 (01: 단위작업 / 02: 스케줄 / 03: 기타)
//            apiDetailInfo.put("EXE_DT", new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime())); // 싫행시간
//            apiDetailInfo.put("INIT_LOAD_YN", apiDetailInfo.containsKey("INIT_LOAD_YN") ? apiDetailInfo.get("INIT_LOAD_YN").toString() : "N"); // 초기적재 구분코드
//            apiDetailInfo.put("LOAD_SYS_CTGR", "01");
//            apiDetailInfo.put("SCHD_DTLS_HST_ID", "9999999999999999");
//            apiDetailInfo.put("EXE_JB_RNG", "02");
//            apiDetailInfo.put("EXT_CNDTN", "01");
//            apiDetailInfo.put("SRC_TYPE", "05");
//            apiDetailInfo.put("COL_BSDT", apiDetailInfo.get("BSDT").toString());
//
//            jdbc.update("""
//                INSERT
//                INTO TB_DP_BI_API_JB_HST (
//                    JB_ID
//                    , API_MEATA_ID
//                    , EXE_JB_RNG
//                    , SCHD_DTLS_HST_ID
//                    , EXE_JB_STSC
//                    , INIT_JB_RNG
//                    , JB_CAL_DSC
//                    , EXE_DT
//                    , EXT_CNDTN
//                    , INIT_LOAD_YN
//                    , EXE_CNT
//                    , COL_BSDT
//                ) VALUES (
//                    :JB_ID
//                    , :API_META_ID
//                    , :EXE_JB_RNG
//                    , :SCHD_DTLS_HST_ID
//                    , '00'
//                    , :INIT_JB_RNG
//                    , :JB_CAL_DSC
//                    , :EXE_DT
//                    , :EXT_CNDTN
//                    , :INIT_LOAD_YN
//                    , 0
//                    , :COL_BSDT
//                )
//            """, apiDetailInfo);
//            submitParams.set(apiDetailInfo);
//
//            try {
//                scheduleRunner.submit(submitParams.get().get("API_META_ID").toString(), submitParams.get().get("JB_ID").toString(), submitParams.get(), ApiDpJob.class, null);
//            } catch (SchedulerException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    @SneakyThrows
//    @PostMapping("/ezRun")
//    public Map<String, Object> ezApiCollectRun(@RequestBody Map<String, Object> param) {
//
//    }
//}