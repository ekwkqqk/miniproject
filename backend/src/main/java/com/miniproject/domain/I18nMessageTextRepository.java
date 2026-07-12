package com.miniproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface I18nMessageTextRepository extends JpaRepository<I18nMessageText, Long> {

    List<I18nMessageText> findByMessageId(Long messageId);

    Optional<I18nMessageText> findByMessageIdAndLocaleId(Long messageId, Long localeId);

    @Query("""
            select t from I18nMessageText t
            join fetch t.message m
            join fetch m.group g
            join fetch t.locale l
            where l.code = :localeCode
              and (:groupCode is null or g.code = :groupCode)
            """)
    List<I18nMessageText> findBundle(@Param("localeCode") String localeCode,
                                     @Param("groupCode") String groupCode);

    @Modifying
    @Query("delete from I18nMessageText t where t.message.id = :messageId")
    void deleteByMessageId(@Param("messageId") Long messageId);

    @Modifying
    @Query("delete from I18nMessageText t where t.message.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("delete from I18nMessageText t where t.locale.id = :localeId")
    void deleteByLocaleId(@Param("localeId") Long localeId);
}
