package com.miniproject.approval.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalHistoryRepository {

    int insert(ApprovalHistory history);

    List<ApprovalHistory> findByDocumentId(@Param("documentId") Long documentId);
}
