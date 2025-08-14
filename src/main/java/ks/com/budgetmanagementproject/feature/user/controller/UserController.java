package ks.com.budgetmanagementproject.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ks.com.budgetmanagementproject.feature.user.dto.LoginReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpReqDto;
import ks.com.budgetmanagementproject.feature.user.service.UserService;
import ks.com.budgetmanagementproject.global.security.AuthUtil;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User", description = "User API")
@SecurityRequirement(name = "bearer Authentication")
public class UserController {

    private final UserService userService;

    @Operation(operationId = "01-signup", summary = "✅ 회원가입", description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpReqDto signUpReqDto) {
        try {
            userService.signup(signUpReqDto);
            return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
        }catch (Exception e) {
            log.error("signUp Error: {}", e.getMessage());
            System.out.println("signUp Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "회원가입 실패",  "message", e.getMessage()));
        }
    }

    @Operation(operationId = "02-login", summary = "✅ 로그인", description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginReqDto loginReqDto, BindingResult bindingResult, HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("필드 에러 발생");
        }
        return userService.login(loginReqDto, response);
    }

    @Operation(operationId = "03-currentUser", summary = "✅ 로그인 사용자 정보 조회", description = "로그인 사용자 정보 조회")
    @GetMapping("/")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (!AuthUtil.isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인되지 않은 사용자입니다."));
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUser) {
            return ResponseEntity.ok(Map.of(
                    "username", customUser.getUsername(),
                    "roles", customUser.getAuthorities()
            ));
        }

        return ResponseEntity.ok(Map.of("principal", principal.toString()));
    }
}
