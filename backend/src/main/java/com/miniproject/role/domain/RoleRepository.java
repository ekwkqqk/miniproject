package com.miniproject.role.domain;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleRepository {

    Optional<Role> findById(Long id);

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    List<Role> findAll();

    List<Role> findAllByOrderByIdAsc();

    int insert(Role role);

    int update(Role role);

    int deleteById(Long id);

    default Role save(Role role) {
        if (role.getId() == null) {
            insert(role);
        } else {
            update(role);
        }
        return role;
    }

    default void delete(Role role) {
        deleteById(role.getId());
    }
}
