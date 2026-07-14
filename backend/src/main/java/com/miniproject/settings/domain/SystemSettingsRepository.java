package com.miniproject.settings.domain;

import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface SystemSettingsRepository {

    Optional<SystemSettings> findById(Long id);

    int insert(SystemSettings settings);

    int update(SystemSettings settings);

    default SystemSettings save(SystemSettings settings) {
        Optional<SystemSettings> existing = findById(settings.getId());
        if (existing.isEmpty()) {
            insert(settings);
        } else {
            update(settings);
        }
        return settings;
    }
}
