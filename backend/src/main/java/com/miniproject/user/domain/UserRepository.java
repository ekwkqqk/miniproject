package com.miniproject.user.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRepository {

    Optional<User> findById(Long id);

    List<User> findAll();

    List<User> searchEnabled(
            @Param("keyword") String keyword,
            @Param("excludeUserId") Long excludeUserId,
            @Param("limit") int limit
    );

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    default User save(User user) {
        if (user.getId() == null) {
            insert(user);
        } else {
            update(user);
        }
        return user;
    }

    default void delete(User user) {
        deleteById(user.getId());
    }
}
