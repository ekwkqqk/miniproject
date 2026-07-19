package com.miniproject.demo.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DemoItemRepository {

    int insert(DemoItem item);

    Optional<DemoItem> findById(@Param("id") Long id);

    long countAll();

    List<DemoItem> findPage(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("status") String status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("featured") Boolean featured,
            @Param("inStock") Boolean inStock,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countPage(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("status") String status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("featured") Boolean featured,
            @Param("inStock") Boolean inStock
    );
}
