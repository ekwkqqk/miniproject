package com.miniproject.i18n.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface I18nLocaleRepository extends JpaRepository<I18nLocale, Long> {

    Optional<I18nLocale> findByCode(String code);

    boolean existsByCode(String code);

    List<I18nLocale> findAllByOrderBySortOrderAscIdAsc();

    List<I18nLocale> findByEnabledTrueOrderBySortOrderAscIdAsc();
}
