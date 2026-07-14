package com.miniproject.menu.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MenuRoleRepository {

    Optional<MenuRole> findById(Long id);

    List<MenuRole> findByMenuId(Long menuId);

    List<Menu> findMenusByRoleIds(@Param("roleIds") List<Long> roleIds);

    void deleteByMenuId(@Param("menuId") Long menuId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    int insert(MenuRole menuRole);

    int deleteById(Long id);

    default MenuRole save(MenuRole menuRole) {
        if (menuRole.getId() == null) {
            insert(menuRole);
        }
        return menuRole;
    }

    default void delete(MenuRole menuRole) {
        deleteById(menuRole.getId());
    }
}
