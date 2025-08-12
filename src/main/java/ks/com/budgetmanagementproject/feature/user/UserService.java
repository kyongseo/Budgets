package ks.com.budgetmanagementproject.feature.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ks.com.budgetmanagementproject.feature.role.Role;
import ks.com.budgetmanagementproject.feature.role.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.RefreshRepository;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    private final RefreshRepository refreshRepository;

    @Transactional
    public boolean isExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User signup(@Valid SignUpReqDto signUpReqDto) {

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER not found"));

        if (isExistsByUsername(signUpReqDto.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        User user = User.builder()
                .username(signUpReqDto.getUsername())
                .password(passwordEncoder.encode(signUpReqDto.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
        return user;
    }

    public ResponseEntity<?> login(LoginReqDto userLoginDto, HttpServletResponse response) {

        Optional<User> user = userRepository.findByUsername(userLoginDto.getUsername());
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID does not exist");
        }
        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("password is incorrect");
        }

        List<String> roles = user.get().getRoles().stream().map(Role::getName).collect(Collectors.toList());

        String accessToken = jwtUtil.createAccessToken(user.get().getId(), user.get().getUsername(), roles, jwtUtil.ACCESS_TOKEN_EXPIRE_COUNT);
        String refreshToken = jwtUtil.createRefreshToken(user.get().getId(), user.get().getUsername(), roles, jwtUtil.REFRESH_TOKEN_EXPIRE_COUNT);

        refreshRepository.saveRefreshToken(refreshToken, jwtUtil.REFRESH_TOKEN_EXPIRE_COUNT);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(jwtUtil.ACCESS_TOKEN_EXPIRE_COUNT / 1000));

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(Math.toIntExact(jwtUtil.REFRESH_TOKEN_EXPIRE_COUNT / 1000));

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        LoginResDto loginResponseDto = LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.get().getId())
                .username(user.get().getUsername())
                .build();

        return ResponseEntity.ok(loginResponseDto);
    }
}