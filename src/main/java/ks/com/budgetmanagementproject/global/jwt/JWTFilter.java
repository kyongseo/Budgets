package ks.com.budgetmanagementproject.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.service.RedisBlackTokenService;
import ks.com.budgetmanagementproject.feature.token.service.RedisRefreshTokenService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
public class  JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final RedisBlackTokenService redisBlackTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/chat")
                || path.endsWith(".html")
                || path.endsWith(".js")
                || path.endsWith(".css")
                || path.endsWith(".ico")
                || path.startsWith("/static/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken(request);

        if (accessToken == null || accessToken.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        accessToken = accessToken.trim();

        if (redisBlackTokenService.isTokenBlacklisted(accessToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            jwtUtil.isExpired(accessToken);
            setAuthentication(accessToken);
            filterChain.doFilter(request, response);
            return;
        } catch (ExpiredJwtException e) {
            String refreshToken = resolveRefreshToken(request);

            if (refreshToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (!redisRefreshTokenService.isRefreshTokenValid(refreshToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            try {
                jwtUtil.isExpired(refreshToken);
            } catch (ExpiredJwtException ex) {
                redisRefreshTokenService.deleteRefreshToken(refreshToken);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String refreshEntity = redisRefreshTokenService.getUsernameByRefreshToken(refreshToken);
            if (refreshEntity == null || refreshEntity.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username = jwtUtil.getUsername(refreshToken);
            List<String> roles = Arrays.stream(jwtUtil.getRole(refreshToken).split(",")).toList();
            Long userId = jwtUtil.getUserId(refreshToken);

            String newAccessToken = jwtUtil.createAccessToken(
                    userId, username, roles, JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT
            );

            Cookie newAccessCookie = new Cookie("accessToken", newAccessToken);
            newAccessCookie.setHttpOnly(true);
            newAccessCookie.setPath("/");
            newAccessCookie.setMaxAge(Math.toIntExact(JWTUtil.ACCESS_TOKEN_EXPIRE_COUNT / 1000));
            response.addCookie(newAccessCookie);

            setAuthentication(newAccessToken);
            filterChain.doFilter(request, response);
            return;
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setAuthentication(String accessToken) {
        String username = jwtUtil.getUsername(accessToken);
        Long userId = jwtUtil.getUserId(accessToken);

        List<Role> roles = Arrays.stream(jwtUtil.getRole(accessToken).trim().split(","))
                .map(roleRepository::findByName)
                .map(opt -> opt.orElseThrow(() -> new RuntimeException("Role not found")))
                .toList();

        User user = User.builder()
                .id(userId)
                .username(username)
                .roles(new HashSet<>(roles))
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}