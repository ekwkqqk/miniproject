package com.miniproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface I18nMessageRepository extends JpaRepository<I18nMessage, Long> {

    List<I18nMessage> findByGroupIdOrderByIdAsc(Long groupId);

    Optional<I18nMessage> findByGroupIdAndCode(Long groupId, String code);

    boolean existsByGroupIdAndCode(Long groupId, String code);

    @Modifying
    @Query("delete from I18nMessage m where m.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Query("""
            select m from I18nMessage m
            join fetch m.group g
            where g.code = :groupCode
            order by m.id asc
            """)
    List<I18nMessage> findByGroupCode(@Param("groupCode") String groupCode);

    @Query("""
            select m from I18nMessage m
            join fetch m.group g
            order by g.id asc, m.id asc
            """)
    List<I18nMessage> findAllWithGroup();
}
