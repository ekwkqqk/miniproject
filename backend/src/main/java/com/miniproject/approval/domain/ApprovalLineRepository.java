package com.miniproject.approval.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApprovalLineRepository {

    int insert(ApprovalLine line);

    int update(ApprovalLine line);

    int deleteByDocumentId(@Param("documentId") Long documentId);

    Optional<ApprovalLine> findById(@Param("id") Long id);

    List<ApprovalLine> findByDocumentId(@Param("documentId") Long documentId);

    long countActedByDocumentId(@Param("documentId") Long documentId);
}
