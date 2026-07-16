package com.miniproject.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file")
public class FileAppProperties {

    /** 업로드 파일이 저장되는 디렉터리 경로 */
    private String uploadDir = "./uploads";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
