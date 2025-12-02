package ks.com.budgetmanagementproject.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import ks.com.budgetmanagementproject.feature.token.service.RedisRefreshTokenService;
import ks.com.budgetmanagementproject.feature.user.dto.LoginRequest;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RedisRefreshTokenService redisRefreshTokenService;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RedisRefreshTokenService redisRefreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisRefreshTokenService = redisRefreshTokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginDTO = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = jwtUtil.createAccessToken(userId, username, Collections.singletonList(roles), JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT);
        String refreshToken = jwtUtil.createRefreshToken(userId, username, Collections.singletonList(roles), JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT);

        addRefreshEntity(username, refreshToken, JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT);

        response.addCookie(createCookie("accessToken", accessToken, false));
        response.addCookie(createCookie("refreshToken", refreshToken, true));
        response.setHeader("accessToken", accessToken);
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setRefresh(refresh);
        refreshToken.setExpiresAt(date.getTime());

        redisRefreshTokenService.addRefreshToken(refreshToken, JWTUtil.REFRESH_TOKEN_EXPIRE_COUNT);
    }

    private Cookie createCookie(String key, String value, boolean httpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}