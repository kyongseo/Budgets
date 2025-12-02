package ks.com.budgetmanagementproject.feature.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import ks.com.budgetmanagementproject.feature.token.service.RedisBlackTokenService;
import ks.com.budgetmanagementproject.feature.token.service.RedisRefreshTokenService;
import ks.com.budgetmanagementproject.feature.user.dto.*;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JWTUtil jwtUtil;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final RedisBlackTokenService redisBlackTokenService;

    /**
     * 회원가입
     * @param request : 이메일, 비밀번호
     */
    @Transactional
    public void signUp(SignUpRequest request) {

        if (isExistsByUsername(request.getUsername())) {
            throw new BaseException(BaseExceptionStatus.DUPLICATE_EMAIL);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.FORBIDDEN_USER));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     * @param request 이메일, 비밀번호
     * @param response 응답
     * @return 토큰
     */
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(BaseExceptionStatus.LOGIN_USER_NOT_EXIST);
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getUsername(), roles, JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT);
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getUsername(), roles, JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT);

        RefreshToken rt = RefreshToken.builder()
                .username(user.getUsername())
                .refresh(refreshToken)
                .expiresAt(System.currentTimeMillis() + JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT)
                .build();

        redisRefreshTokenService.addRefreshToken(refreshToken, JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT);

        // refreshRepository.save(rt);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT / 1000));

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(Math.toIntExact(JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT / 1000));

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        response.setHeader("accessToken", accessToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    /**
     * accessToken 재발급
     * @param request 요청
     * @param response 응답
     * @return newAccessToken
     */
    public String reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            throw new BaseException(BaseExceptionStatus.NON_EXISTENT_TOKEN);
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BaseException(BaseExceptionStatus.NON_EXISTENT_TOKEN);
        }

        String username = jwtUtil.getUsername(refreshToken);
        Long userId = jwtUtil.getUserId(refreshToken);
        List<String> roles = List.of(jwtUtil.getRole(refreshToken).split(","));

        String newAccessToken = jwtUtil.createAccessToken(
                userId, username, roles, JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT);

        Cookie cookie = new Cookie("accessToken", newAccessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT / 1000));
        response.addCookie(cookie);

        return newAccessToken;
    }

    /**
     * 사용자 정보 업데이트
     * @param request nickname, phoneNumber
     */
    @Transactional
    public void updateUser(User user, UpdateRequest request) {
        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));

        persistedUser.updateUserInfo(request.getNickname(), request.getPhoneNumber());

        UpdateResponse.from(user);
    }

    /**
     * 로그아웃 처리
     * @param response HTTP 응답
     */
    public void logout(HttpServletResponse response) {

        String accessToken = response.getHeader("accessToken");
        String refreshToken = response.getHeader("refreshToken");

        if (accessToken == null && refreshToken == null) {
            return;
        }
        SecurityContextHolder.clearContext();

        if (accessToken != null) {
            deleteCookie(response, "accessToken");
            long remainingTime = jwtUtil.getExpiration(accessToken) - System.currentTimeMillis();
            if (remainingTime > 0) {
                redisBlackTokenService.addBlacklistedToken(accessToken, remainingTime);
            }
        }
        if (refreshToken != null) {
            deleteCookie(response, "refreshToken");
            redisRefreshTokenService.deleteRefreshToken(refreshToken);
        }
    }

    private boolean isExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}