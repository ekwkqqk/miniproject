package com.miniproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRoleRepository extends JpaRepository<MenuRole, Long> {

    List<MenuRole> findByMenuId(Long menuId);

    @Query("""
            select distinct mr.menu from MenuRole mr
            where mr.role.id in :roleIds
            """)
    List<Menu> findMenusByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Modifying
    @Query("delete from MenuRole mr where mr.menu.id = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);

    @Modifying
    @Query("delete from MenuRole mr where mr.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
