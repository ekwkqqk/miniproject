package com.miniproject.approval.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ApprovalDocumentRepository {

    int insert(ApprovalDocument document);

    int update(ApprovalDocument document);

    int deleteById(@Param("id") Long id);

    Optional<ApprovalDocument> findById(@Param("id") Long id);

    List<ApprovalDocument> findPage(
            @Param("viewerId") Long viewerId,
            @Param("box") String box,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countPage(
            @Param("viewerId") Long viewerId,
            @Param("box") String box,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    int nextDocSeq(@Param("seqDate") LocalDate seqDate);

    int upsertDocSeq(@Param("seqDate") LocalDate seqDate);

    List<ApprovalDocument> findDueScheduled(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
