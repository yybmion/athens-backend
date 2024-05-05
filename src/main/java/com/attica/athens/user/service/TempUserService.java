package com.attica.athens.user.service;

import com.attica.athens.security.JWTUtil;
import com.attica.athens.user.domain.TempUser;
import com.attica.athens.user.domain.UserRole;
import com.attica.athens.user.repository.TempUserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempUserService {

    private static final long EXPIRED_MS = 60 * 60 * 10L;

    private final TempUserRepository tempUserRepository;
    private final JWTUtil jwtUtil;

    @Transactional
    public String createTempUser() {

        String tempUserId = UUID.randomUUID().toString();
        TempUser tempUser = new TempUser(tempUserId);

        tempUserRepository.save(tempUser);

        return jwtUtil.createJwt(tempUserId, UserRole.ROLE_TEMP_USER.name(), EXPIRED_MS);
    }
}
