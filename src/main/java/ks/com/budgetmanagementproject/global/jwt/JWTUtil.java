package ks.com.budgetmanagementproject.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성 / 검증 과정에 해당하는 유틸리티 클래스
 */
@Component
@Getter
public class JWTUtil {

    private SecretKey secretKey;

    public static Long ACCESS_TOKEN_EXPIRE_COUNT = 30 * 60 * 1000L; // 30분
    public static Long REFRESH_TOKEN_EXPIRE_COUNT = 7 * 24 * 60 * 60 * 1000L; // 7일

    public JWTUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    public Long getUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String getCategory(String token) {
        return extractClaims(token).get("category", String.class);
    }

    public Boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String createAccessToken(Long userId, String username, List<String> roles, Long expiredMs) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", String.join(",", roles))
                .claim("category", "accessToken")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId, String username, List<String> roles, Long expiredMs) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", String.join(",", roles))
                .claim("category", "refreshToken")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}