@Slf4j
@Repository
@RequiredArgsConstructor
public class RawApiDataLoader {
    private final JdbcTemplate jdbcTemplate;

    public void deleteRawApiData(String apiUrl, String jdDt) {
        Sting deleteStmt = "DELETE FROM SWCDITBFAD001 WHERE OTSD_LINK_URL = ? AND BSDT = ?";
        jdbcTemplate.update(deleteStmt, apiUrl, jbDt);
    
    }

    public void deleteRawApiDataDetail(String apiUrl, String jbDt) {
        String deleteStmt = "DELETE FROM SWCDITBFAD002 WHERE OTSD_LINK_URL = ? AND BSDT = ?";
        jdbcTemplate.update(deleteStmt, apiUrl, jbDt);
    }

    public void insertRawApiData(String dataPvGp, String apiSvc, String apiUrl, String apiSn, String dataProvider, String apiExpl, String responseData, String dataFormat, String bsDt, String jbDt) {
        String sql = "INSERT INTO SWCDITBFAD001 (FRG_BUR_C, SV_NM, OTST_LINK_URL, LNK_DTA_DRM_ID, FRG_IF_BUR_NM, API_DFNTN_CN, API_RSP_CN, COL_DTA_TP_NM, BSDT, LDNG_TS)" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    new Object[]{dataPvGp, apiSvc, apiUrl, apiSn, dataProvider, apiExpl, responseData, dataFormat, bsDt, jbDt},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CLOB, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP}
            );
        } catch (Exception e) {
            log.error("외부데이터 수집작업에 실패했습니다. : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void insertRawApiDataDetail(String dataPvGp, String apiSvc, String apiUrl, String apiSn, String dataProvider, String apiExpl, String responseData, String dataFormat, String bsDt, String jbDt) {
        String sql = "INSERT INTO SWCDITBFAD002 (FRG_BUR_C, SV_NM, OTST_LINK_URL, LNK_DTA_DRM_ID, FRG_IF_BUR_NM, API_DFNTN_CN, API_RSP_CN, COL_DTA_TP_NM, BSDT, LDNG_TS)" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        bodyData = (bodyData == null) ? "" : bodyData;
        try {
            jdbcTemplate.update(sql,
                    new Object[]{dataPvGp, apiSvc, apiUrl, apiSn, dataProvider, apiExpl, responseData, dataFormat, bsDt, jbDt},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CLOB, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP}
            );
        } catch (Exception e) {
            log.error("외부데이터 수집작업에 실패했습니다. : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}