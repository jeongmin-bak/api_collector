
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractUrlHandler implements UrlHandler {
    private final RawApiDataLoader apiDataLoader;
    protected final int MAX_API_BATCH_SIZE = 1000;
    protected abstract Map<String, Object> executeJob(Map<String, Object> urlParam, Map<String, Object> metaInfo);

    @Override
    @Transactional
    public void handle(Map<String, Object> urlParam, Map<String, Object> metaInfo) {
        String dataFormat = (String) metaInfo.get("DATA_FORMAT");
        String jbDt = (String) metaInfo.get("JB_DT");
        String dataPvGp = (String) metaInfo.get("DATA_PV_GP");
        String apiUrl = (String) metaInfo.get("API_URL");
        String apiSvc = (String) metaInfo.get("API_SVC");
        String dataProvider = (String) metaInfo.get("DATA_PROVIDER");
        String apiExpl = (String) metaInfo.get("API_EXPL");
        String bsDtKey = (String) metaInfo.get("BSDT_KEY");
        String bsDt = metaInfo.get("BSDT") == null ? "" : (String) metaInfo.get("BSDT");
        String bsDtParamOrder = metaInfo.get("BSDT_ORDER") == null ? null : metaInfo.get("BSDT_ORDER").toString();

        Map<String, Object> bsInfoMap = new HashMap<>();
        bsInfoMap.put("BSDT_KEY", bsDtKey);
        bsInfoMap.put("BSDT", bsDt);
        bsInfoMap.put("BSDT_ORDER", bsDtParamOrder);
        log.info("bsInfoMap : {}", bsInfoMap);

        boolean isPath = Boolean.parseBoolean((String) metaInfo.get("IS_PATH"));
        String reqUrlType = metaInfo.get("URL_REQ_TYPE").toString();
        String baseUrl = DynamicUrlBuilder.builderUrl(apiUrl, urlParam, isPath, reqUrlType, bsInfoMap);
        log.info("baseUrl : {}", baseUrl);

        Map<String, Obejct> apiData = executeJob(urlParam, metaInfo);

        if(apiData == null || apiData.isEmpty()) {
            log.info("fetchApiData is null");
            return;
        }
        boolean isPage = (boolean) apiData.get("IS_PAGE");
        String jbDt_formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String seq = UUID.randomUUID().toString();
        String dataSeqId = apiSvc + "_" + seq;

        if(apiData.get("ERROR_CODE") != null) {
            if(apiData.get("ERROR_CODE").toString().equals("TRUE")) {
                String errorContent = apiData.get("HB_RESP").toString();
                log.info("ERROR_CODE, content : {} {}", apiData.get("ERROR_CODE"), errorContent);
                apiDataLoader.deleteRawApiData(baseUrl, bsDt);
                apiDataLoader.insertRawApiData(dataPvGp, apiSvc, baseUrl, dataSeqId, dataProvider, apiExpl, errorContent, dataFormat, bsDt, jbDt_formatted);
                return;
            }
        }
        String hbResp = (String) apiData.get("HB_RESP");

        // 서비스
    }
}