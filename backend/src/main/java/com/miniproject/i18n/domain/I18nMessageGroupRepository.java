package com.miniproject.i18n.domain;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface I18nMessageGroupRepository {

    Optional<I18nMessageGroup> findById(Long id);

    Optional<I18nMessageGroup> findByCode(String code);

    boolean existsByCode(String code);

    List<I18nMessageGroup> findAllByOrderByIdAsc();

    int insert(I18nMessageGroup group);

    int update(I18nMessageGroup group);

    int deleteById(Long id);

    default I18nMessageGroup save(I18nMessageGroup group) {
        if (group.getId() == null) {
            insert(group);
        } else {
            update(group);
        }
        return group;
    }

    default void delete(I18nMessageGroup group) {
        deleteById(group.getId());
    }
}
