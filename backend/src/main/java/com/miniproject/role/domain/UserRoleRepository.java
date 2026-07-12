package com.miniproject.role.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    @Query("""
            select ur.role from UserRole ur
            where ur.user.email = :email
            """)
    List<Role> findRolesByUserEmail(@Param("email") String email);

    @Query("""
            select ur.role.code from UserRole ur
            where ur.user.email = :email
            """)
    List<String> findRoleCodesByUserEmail(@Param("email") String email);

    boolean existsByUser_EmailAndRole_Code(String email, String roleCode);

    @Modifying
    @Query("delete from UserRole ur where ur.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from UserRole ur where ur.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
