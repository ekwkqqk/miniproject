package com.miniproject.menu.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MenuRepository {

    Optional<Menu> findById(Long id);

    List<Menu> findAll();

    List<Menu> findAllByOrderBySortOrderAscIdAsc();

    List<Menu> findByParent_IdOrderBySortOrderAscIdAsc(@Param("parentId") Long parentId);

    Optional<Menu> findByUrl(String url);

    boolean existsByUrl(String url);

    boolean existsByParent_Id(@Param("parentId") Long parentId);

    long countByParent_Id(@Param("parentId") Long parentId);

    int insert(Menu menu);

    int update(Menu menu);

    int deleteById(Long id);

    default Menu save(Menu menu) {
        if (menu.getId() == null) {
            insert(menu);
        } else {
            update(menu);
        }
        return menu;
    }

    default void delete(Menu menu) {
        deleteById(menu.getId());
    }
}
