package ks.com.budgetmanagementproject.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ks.com.budgetmanagementproject.feature.role.Role;
import ks.com.budgetmanagementproject.feature.role.RoleRepository;
import ks.com.budgetmanagementproject.feature.user.User;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;

    public JWTFilter(JWTUtil jwtUtil, RoleRepository roleRepository) {
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = resolveAccessToken(request);

        if (accessToken == null || accessToken.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        accessToken = accessToken.trim();

        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("access token expired");
            return;
        }

        String category = jwtUtil.getCategory(accessToken);
        if (!"accessToken".equals(category)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("invalid access token");
            return;
        }

        String username = jwtUtil.getUsername(accessToken);
        List<Role> roles = Arrays.stream(jwtUtil.getRole(accessToken).trim().split(","))
                .map(roleRepository::findByName)
                .map(opt -> opt.orElseThrow(() -> new RuntimeException("Role not found")))
                .collect(Collectors.toList());

        User user = User.builder()
                .username(username)
                .roles(new HashSet<>(roles))
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
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
}