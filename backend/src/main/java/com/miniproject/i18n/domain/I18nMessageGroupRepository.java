package com.miniproject.i18n.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface I18nMessageGroupRepository extends JpaRepository<I18nMessageGroup, Long> {

    Optional<I18nMessageGroup> findByCode(String code);

    boolean existsByCode(String code);

    List<I18nMessageGroup> findAllByOrderByIdAsc();
}
