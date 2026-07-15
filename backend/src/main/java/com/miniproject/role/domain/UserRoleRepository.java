package com.miniproject.role.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRoleRepository {

    Optional<UserRole> findById(Long id);

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdIn(@Param("userIds") List<Long> userIds);

    List<Role> findRolesByUserEmail(@Param("email") String email);

    List<String> findRoleCodesByUserEmail(@Param("email") String email);

    boolean existsByUser_EmailAndRole_Code(@Param("email") String email, @Param("roleCode") String roleCode);

    void deleteByUserId(@Param("userId") Long userId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    int insert(UserRole userRole);

    int deleteById(Long id);

    default UserRole save(UserRole userRole) {
        if (userRole.getId() == null) {
            insert(userRole);
        }
        return userRole;
    }

    default void delete(UserRole userRole) {
        deleteById(userRole.getId());
    }
}
