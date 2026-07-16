package com.miniproject.file.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(FileAppProperties.class)
public class FileStorageConfig {

    private final FileAppProperties properties;

    public FileStorageConfig(FileAppProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void ensureUploadDir() throws IOException {
        Path dir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(dir);
    }
}
