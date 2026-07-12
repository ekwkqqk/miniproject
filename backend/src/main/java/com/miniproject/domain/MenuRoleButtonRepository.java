package com.miniproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRoleButtonRepository extends JpaRepository<MenuRoleButton, Long> {

    List<MenuRoleButton> findByMenuId(Long menuId);

    List<MenuRoleButton> findByMenuIdAndRoleIdIn(Long menuId, List<Long> roleIds);

    Optional<MenuRoleButton> findByMenuIdAndRoleId(Long menuId, Long roleId);

    @Modifying
    @Query("delete from MenuRoleButton mrb where mrb.menu.id = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);

    @Modifying
    @Query("delete from MenuRoleButton mrb where mrb.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
