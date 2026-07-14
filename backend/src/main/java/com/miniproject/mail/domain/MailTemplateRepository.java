package com.miniproject.mail.domain;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MailTemplateRepository {

    Optional<MailTemplate> findById(Long id);

    Optional<MailTemplate> findByCode(String code);

    boolean existsByCode(String code);

    List<MailTemplate> findAllByOrderByIdAsc();

    int insert(MailTemplate template);

    int update(MailTemplate template);

    int deleteById(Long id);

    default MailTemplate save(MailTemplate template) {
        if (template.getId() == null) {
            insert(template);
        } else {
            update(template);
        }
        return template;
    }

    default void delete(MailTemplate template) {
        deleteById(template.getId());
    }
}
