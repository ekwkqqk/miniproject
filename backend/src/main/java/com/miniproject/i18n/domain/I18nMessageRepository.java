package com.miniproject.i18n.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface I18nMessageRepository {

    Optional<I18nMessage> findById(Long id);

    List<I18nMessage> findByGroupIdOrderByIdAsc(Long groupId);

    Optional<I18nMessage> findByGroupIdAndCode(@Param("groupId") Long groupId, @Param("code") String code);

    boolean existsByGroupIdAndCode(@Param("groupId") Long groupId, @Param("code") String code);

    void deleteByGroupId(@Param("groupId") Long groupId);

    List<I18nMessage> findByGroupCode(@Param("groupCode") String groupCode);

    List<I18nMessage> findAllWithGroup();

    int insert(I18nMessage message);

    int update(I18nMessage message);

    int deleteById(Long id);

    default I18nMessage save(I18nMessage message) {
        if (message.getId() == null) {
            insert(message);
        } else {
            update(message);
        }
        return message;
    }

    default void delete(I18nMessage message) {
        deleteById(message.getId());
    }
}
