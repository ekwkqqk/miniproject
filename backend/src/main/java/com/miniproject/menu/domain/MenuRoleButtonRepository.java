package com.miniproject.menu.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MenuRoleButtonRepository {

    Optional<MenuRoleButton> findById(Long id);

    List<MenuRoleButton> findByMenuId(Long menuId);

    List<MenuRoleButton> findByMenuIdAndRoleIdIn(@Param("menuId") Long menuId,
                                                 @Param("roleIds") List<Long> roleIds);

    Optional<MenuRoleButton> findByMenuIdAndRoleId(@Param("menuId") Long menuId,
                                                   @Param("roleId") Long roleId);

    void deleteByMenuId(@Param("menuId") Long menuId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    int insert(MenuRoleButton button);

    int update(MenuRoleButton button);

    int deleteById(Long id);

    default MenuRoleButton save(MenuRoleButton button) {
        if (button.getId() == null) {
            insert(button);
        } else {
            update(button);
        }
        return button;
    }

    default void delete(MenuRoleButton button) {
        deleteById(button.getId());
    }
}
