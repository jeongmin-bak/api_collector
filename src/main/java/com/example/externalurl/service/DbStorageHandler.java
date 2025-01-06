package com.example.externalurl.service;

import com.example.externalurl.util.ShellCommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class DbStorageHandler extends AbstractStorageHandler {

    public DbStorageHandler(RestTemplate restTemplate, ShellCommandUtil shellCommandUtil) {
        super(restTemplate, shellCommandUtil);
    }

    @Override
    protected long executeJob(Map<String, Object> requestParam) throws Exception {
//        String pgmType = (String) requestParam.get("PGM_TYPE");
        String pgmType = "02";
        //String dbType = (String) requestParam.get("DB_TYPE");
        String dbType = "01";
        String srcUseType = (String) requestParam.get("SRC_USE_TYPE");

        log.info("DbStorageHandler - 작업 시작. 요청 파라미터: {}", requestParam);
        String command = shellCommandUtil.buildShellCommand(requestParam, "01");

        long pid;
        if ("01".equals(pgmType)) {
            pid = handleJavaProgram(command);
        } else if ("02".equals(pgmType)) {
            pid = handleProcProgram(requestParam, "02", srcUseType, command);
        } else {
            log.error("잘못된 프로그램 유형: {}", pgmType);
            throw new IllegalArgumentException("잘못된 프로그램 유형");
        }
        log.info("작업 실행 완료");
        return pid;
    }

    private long handleJavaProgram(String command) throws Exception {
        long pid = shellCommandUtil.runShellCommand(command);
        log.info("Java 프로그램 실행 완료, PID: {}", pid);
        return pid;
    }

    private long handleProcProgram(Map<String, Object> requestParam, String dbType, String srcUseType, String command) throws Exception {
        long pid = 0;
        if ("01".equals(dbType)) {
            if ("01".equals(srcUseType)) {
                pid = handleOracleCollection(command);
            } else if ("02".equals(srcUseType)) {
                pid = handleOracleLoading(requestParam, command);
            } else {
                log.error("잘못된 소스 사용 유형: {}", srcUseType);
                throw new IllegalArgumentException("잘못된 소스 사용 유형");
            }
        }
        return pid;
    }

    private long handleOracleCollection(String command) throws Exception {
        long pid = shellCommandUtil.runShellCommand(command);
        log.info("Proc 명령어 실행 완료 (데이터 수집), PID: {}", pid);
        return pid;
    }

    private long handleOracleLoading(Map<String, Object> requestParam, String command) throws Exception {
        String strgType = (String) requestParam.get("STRG_TYPE");
        String cprsType = (String) requestParam.get("CPRS_TYPE");
        String sourceFilePath = (String) requestParam.get("FILE_PATH");
        String downloadPath = tmpDataDir + "/product/" + requestParam.get("JB_ID");

        log.info("파일 다운로드 시작 - 저장 유형: {}, 압축 유형: {}", strgType, cprsType);
        if ("01".equals(strgType)) {    // local
            log.info("로컬에서 파일 이동 중...");
            Path sourcePath = Path.of(sourceFilePath); // 로컬 다운로드 원천 경로
            Path targetPath = Path.of(downloadPath); // 로컬 다운로드 타깃 경로
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }
            try (Stream<Path> files = Files.list(sourcePath)) {
                files.forEach(file -> {
                    try {
                        if (Files.isRegularFile(file) && !file.getFileName().toString().endsWith(".crc")) {
                            Path targetFileName = targetPath.resolve(file.getFileName());
                            Files.move(file, targetFileName, StandardCopyOption.REPLACE_EXISTING); // 파일 이동
                            log.info("Moved: {}", file + " -> " + targetFileName);
                        }
                    } catch (IOException e) {
                        log.info("Error moving file: {}", file + " - " + e.getMessage());
                    }
                });

            }
        }
//        } else if ("02".equals(strgType)) { // Nas
//            log.info("NAS에서 파일 다운로드 중...");
//            SftpUtil.downloadFilesFromSftp((Map<String, Object>) requestParam.get("NAS_STRG_CONF"),
//                    sourceFilePath, tmpDataDir, (String) requestParam.get("JB_ID"));
//        } else {    // Object Storage
//            log.info("오브젝트 스토리지에서 파일 다운로드 중...");
//            S3Util.downloadFilesFromS3((Map<String, Object>) requestParam.get("OBJ_STRG_CONF"),
//                    sourceFilePath, tmpDataDir, (String) requestParam.get("JB_ID"));
//        }

        String charSet = (String) requestParam.get("CHAR_SET");
        Charset charsetCode;
        if (charSet.equals("01")) {
            charsetCode = Charset.forName("MS949");
        } else if (charSet.equals("02")) {
            charsetCode = Charset.forName("EUC-KR");
        } else {
            charsetCode = StandardCharsets.UTF_8;
        }
        log.info("파일 압축 해제 및 삭제 처리 시작 - 다운로드 경로: {}", downloadPath);

//        if (!cprsType.equals("01")) {
//            CompressionHandler.decompressAndDeleteFiles(downloadPath, charsetCode, new Configuration());
//        }
        List<Path> deleteFileList;
        try (Stream<Path> paths = Files.list(Paths.get(downloadPath))) {
            deleteFileList = paths.filter(path -> path.getFileName().toString().endsWith(".crc")).peek(path -> log.info(".crc 파일 삭제: {}", path)).toList();
        }
        deleteFileList.forEach(crcFile -> {
            try {
                Files.delete(crcFile);
            } catch (IOException e) {
                log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
            }
        });

        long pid = shellCommandUtil.runShellCommand(command);
        log.info("Proc 명령어 실행 완료 (데이터 적재), PID: {}", pid);
        return pid;
    }
}