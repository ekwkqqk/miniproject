package com.miniproject.mail.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {

    Optional<MailTemplate> findByCode(String code);

    boolean existsByCode(String code);

    List<MailTemplate> findAllByOrderByIdAsc();
}
