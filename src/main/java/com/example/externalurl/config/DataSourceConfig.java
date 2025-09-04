import com.example.externalurl.util.Decrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {
    @Value("${common.params.meta_info_path}")
    private String jsonFilePath;

    @Value("${common.params.job_id}")
    private String jobId;

    @Value("${hikari.connection-timeout}")
    private long connectionTimeout;

    @Value("${hikari.max-lifetime}")
    private long maxLifetime;

    private final ObjectMapper objectMapper;

    @Bean
    public HikariDataSource dataSource() throws IOException {
        String jobMetaFile = jsonFilePath + "/api_unload_" + jobId + ".json";
        HashMap<String, Object> metaInfo = objectMapper.readValue(new File(jobMetaFile), HashMap.class);

        String jdbcId = (String) metaInfo.get("JDBC_ID");
        String jdbcPw = (String) metaInfo.get("JDBC_PW");
        String jdbcUrl = (String) metaInfo.get("JDBC_URL");
        String driver = (String) metaInfo.get("JDBC_DRIVER");
        int conCnt = (int) metaInfo.get("CON_CNT");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcId);
        config.setPassword(Decrypt.decrypt(jdbcPw, "redwoodk"));
        config.setDriverClassName(driver);
        config.setMaximumPoolSize(conCnt);
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxLifetime(maxLifetime);

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(HikariDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}