package com.miniproject.menu.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuAccessLogRepository {

    int insert(MenuAccessLog log);

    List<MenuAccessLog> findRecent(@Param("limit") int limit, @Param("offset") int offset);

    long countAll();
}
