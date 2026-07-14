package com.miniproject.auth.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenRepository {

    Optional<RefreshToken> findById(Long id);

    Optional<RefreshToken> findByToken(String token);

    void revokeAllActiveByUserId(@Param("userId") Long userId);

    int insert(RefreshToken refreshToken);

    int update(RefreshToken refreshToken);

    int deleteById(Long id);

    default RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            insert(refreshToken);
        } else {
            update(refreshToken);
        }
        return refreshToken;
    }

    default void delete(RefreshToken refreshToken) {
        deleteById(refreshToken.getId());
    }
}
