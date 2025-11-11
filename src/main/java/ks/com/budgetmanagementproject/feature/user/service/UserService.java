package ks.com.budgetmanagementproject.feature.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import ks.com.budgetmanagementproject.feature.token.repository.RefreshRepository;
import ks.com.budgetmanagementproject.feature.user.dto.LoginReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResDto;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.UserEditDto;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Transactional
    public boolean isExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 회원가입
     * @param signUpReqDto : 이메일, 비밀번호
     */
    @Transactional
    public void signUp(@Valid SignUpReqDto signUpReqDto) {

        if (isExistsByUsername(signUpReqDto.getUsername())) {
            throw new BaseException(DUPLICATE_EMAIL);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BaseException(FORBIDDEN_USER));

        User user = User.builder()
                .username(signUpReqDto.getUsername())
                .password(passwordEncoder.encode(signUpReqDto.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     * @param userLoginDto 이메일, 비밀번호
     * @param response 응답
     * @return 토큰
     */
    public LoginResDto login(LoginReqDto userLoginDto, HttpServletResponse response) {

        User user = userRepository.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> new BaseException(NON_EXISTENT_USER));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new BaseException(LOGIN_USER_NOT_EXIST);
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

        refreshRepository.save(rt);

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

        return LoginResDto.builder()
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
            throw new BaseException(NON_EXISTENT_TOKEN);
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BaseException(NON_EXISTENT_TOKEN);
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
     * @param authentication 인증정보
     * @param userEditDto nickname, phoneNumber
     * @return 저장
     */
    @Transactional
    public User updateUser(Authentication authentication, UserEditDto userEditDto) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails customUser))
            throw new BaseException(NON_EXISTENT_USER);

        User user = userRepository.findByUsername(customUser.getUsername())
                .orElseThrow(() -> new BaseException(NON_EXISTENT_USER));

        user.setNickname(userEditDto.getNickname());
        user.setPhoneNumber(userEditDto.getPhoneNumber());

        return userRepository.save(user);
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
        }
        if (refreshToken != null) {
            deleteCookie(response, "refreshToken");
            // refreshTokenRepository.deleteByToken(refreshToken);
        }
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