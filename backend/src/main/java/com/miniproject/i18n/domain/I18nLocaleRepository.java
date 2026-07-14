package com.miniproject.i18n.domain;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface I18nLocaleRepository {

    Optional<I18nLocale> findById(Long id);

    Optional<I18nLocale> findByCode(String code);

    boolean existsByCode(String code);

    List<I18nLocale> findAllByOrderBySortOrderAscIdAsc();

    List<I18nLocale> findByEnabledTrueOrderBySortOrderAscIdAsc();

    int insert(I18nLocale locale);

    int update(I18nLocale locale);

    int deleteById(Long id);

    default I18nLocale save(I18nLocale locale) {
        if (locale.getId() == null) {
            insert(locale);
        } else {
            update(locale);
        }
        return locale;
    }

    default void delete(I18nLocale locale) {
        deleteById(locale.getId());
    }
}
