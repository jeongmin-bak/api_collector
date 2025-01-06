package com.example.externalurl.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@Component
public class ShellCommandUtil {
    @Value("${agent.batch.shell_scripts.start_java_batch}")
    private String javaShellScript;

    @Value("${agent.batch.shell_scripts.start_multi_batch}")
    private String javaBatchShellScript;

    @Value("${server.agent.url}")
    private String agentUrl;

    @Value("${server.agent.port}")
    private String agentPort;

    /**
     * 쉘 명령어를 실행하고 프로세스 ID를 반환하는 메서드 (단일 명령어)
     */
    public long runShellCommand(String command) throws Exception {
        return executeCommand(command);
    }

    /**
     * 실제로 명령어를 실행하는 메서드
     */
    private long executeCommand(String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            long returnedPid = Long.parseLong(output.toString().trim());

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Shell command failed with exit code: " + exitCode);
            }
            return returnedPid;
        }
    }

    /**
     * 파라미터 기반으로 쉘 명령어를 생성하는 메서드 (간단한 버전)
     */
    public String buildShellCommand(Map<String, Object> params) {
        String srcType = (String) params.get("SRC_TYPE");
        StringBuilder command = new StringBuilder();

        switch (srcType) {
            case "02": // 소스 유형 : OBJECT
                command.append(javaBatchShellScript).append(" ");
                break;
            case "03": // 소스 유형 : FILE
                command.append(javaBatchShellScript).append(" ");
                break;
            case "04": // 소스 유형 : REST API
                command.append(javaBatchShellScript).append(" ");
                break;
            default:
                throw new IllegalArgumentException("Unknown source type: " + srcType);
        }

        if (params.containsKey("JB_ID")) {
            command.append(params.get("JB_ID")).append(" ");
        }

        if (params.containsKey("JB_DT")) {
            command.append(params.get("JB_DT")).append(" ");
        }

        if (params.containsKey("SRC_USE_TYPE")) {
            command.append(params.get("SRC_USE_TYPE")).append(" ");
        }

        return command.toString().trim();
    }

    /**
     * 파라미터 기반으로 쉘 명령어를 생성하는 메서드 (프로그램 유형 포함)
     */
    public String buildShellCommand(Map<String, Object> params, String pgmType) {
        StringBuilder command = new StringBuilder();
        boolean isNative = "02".equalsIgnoreCase(pgmType);
        String separator = isNative ? "," : " ";

        if ("01".equals(pgmType)) {
            command.append(javaShellScript).append(" ");
        }
        if (params.containsKey("JB_ID")) {
            command.append(params.get("JB_ID")).append(" ");
        }
        if (params.containsKey("JB_DT")) {
            command.append(params.get("JB_DT")).append(" ");
        }
        if (params.containsKey("EXEC_CNT")) {
            command.append(params.get("EXEC_CNT")).append(" ");
        }
        if (params.containsKey("SRC_USE_TYPE")) {
            command.append(params.get("SRC_USE_TYPE")).append(" ");
        }
        if (params.containsKey("DB_TYPE")) {
            command.append(params.get("DB_TYPE")).append(" ");
        }

        appendParam(command, "CON_CNT", params, separator);
        appendParam(command, "BAT_CNT", params, separator);
        appendParam(command, "BAT_ERR_MODE", params, separator);

        if (isNative) {
            command.append(",").append("AGENT_URL").append("=").append("http://").append(agentUrl).append(":").append(agentPort);
        }
        return command.toString().trim();
    }

    /**
     * 명령어에 파라미터를 추가하는 유틸리티 메서드
     */
    private void appendParam(StringBuilder command, String key, Map<String, Object> params, String separator) {
        if (params.containsKey(key)) {
            if (command.charAt(command.length() - 1) != ' ') {
                command.append(separator);
            }
            command.append(key).append("=").append(params.get(key));
        }
    }
}
