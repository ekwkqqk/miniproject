package com.miniproject.auth.service;

import com.miniproject.settings.domain.SystemSettings;
import com.miniproject.settings.service.SystemSettingsService;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 실패 카운트/잠금은 인증 실패 예외로 outer 트랜잭션이 롤백되어도
 * 반드시 커밋되도록 독립 트랜잭션으로 처리한다.
 */
@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final SystemSettingsService systemSettingsService;
    private final RefreshTokenService refreshTokenService;

    public LoginAttemptService(UserRepository userRepository,
                               SystemSettingsService systemSettingsService,
                               RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.systemSettingsService = systemSettingsService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * @return true if the account was locked by this failed attempt
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registerFailedLoginAttempt(String email) {
        SystemSettings settings = systemSettingsService.getOrCreate();
        int maxAttempts = settings.getMaxFailedLoginAttempts();
        if (maxAttempts <= 0 || email == null || email.isBlank()) {
            return false;
        }

        return userRepository.findByEmail(email.trim()).map(user -> {
            if (!user.isEnabled()) {
                return false;
            }
            int attempts = user.registerFailedLogin();
            if (attempts >= maxAttempts) {
                user.changeEnabled(false);
                userRepository.save(user);
                refreshTokenService.revokeAllActiveSessions(user.getId());
                return true;
            }
            userRepository.save(user);
            return false;
        }).orElse(false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedLoginAttempts(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedLoginAttempts();
                userRepository.save(user);
            }
        });
    }
}
