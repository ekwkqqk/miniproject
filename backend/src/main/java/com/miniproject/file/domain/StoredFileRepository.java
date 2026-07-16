package com.miniproject.file.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StoredFileRepository {

    Optional<StoredFile> findById(Long id);

    List<StoredFile> findByFileGroupId(@Param("fileGroupId") Long fileGroupId);

    Long nextFileGroupId();

    int insert(StoredFile file);

    int deleteById(Long id);

    default StoredFile save(StoredFile file) {
        if (file.getId() == null) {
            insert(file);
        }
        return file;
    }

    default void delete(StoredFile file) {
        deleteById(file.getId());
    }
}
