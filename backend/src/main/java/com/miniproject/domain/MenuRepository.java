package com.miniproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderBySortOrderAscIdAsc();

    List<Menu> findByParent_IdOrderBySortOrderAscIdAsc(Long parentId);

    Optional<Menu> findByUrl(String url);

    boolean existsByUrl(String url);

    boolean existsByParent_Id(Long parentId);

    long countByParent_Id(Long parentId);
}
