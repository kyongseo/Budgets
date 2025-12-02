package ks.com.budgetmanagementproject.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ks.com.budgetmanagementproject.feature.user.dto.LoginRequest;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResponse;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpRequest;
import ks.com.budgetmanagementproject.feature.user.dto.UpdateRequest;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.service.UserService;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User", description = "User API")
@SecurityRequirement(name = "bearer Authentication")
public class UserController {

    private final UserService userService;

    @Operation(summary = "✅ 회원가입", description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Validated @RequestBody SignUpRequest request) {

        userService.signUp(request);

        return ResponseEntity
                .status(BaseResponseStatus.SIGN_UP_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.SIGN_UP_SUCCESS));
    }

    @Operation(summary = "✅ 로그인", description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = userService.login(request, response);

        return ResponseEntity
                .status(BaseResponseStatus.LOGIN_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.LOGIN_SUCCESS, loginResponse));
    }

    @Operation(summary = "✅ AccessToken 재발급", description = "AccessToken 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> reissueToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String newLoginResponse = userService.reissueToken(request, response);

        return ResponseEntity
                .status(BaseResponseStatus.ACCESS_TOKEN_REISSUE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.ACCESS_TOKEN_REISSUE_SUCCESS, newLoginResponse));
    }

    @Operation(summary = "✅ 사용자 정보 변경", description = "사용자 정보 변경")
    @PatchMapping()
    public ResponseEntity<?> getUpdateUser(
            @AuthenticationPrincipal User user,
            @Validated @RequestBody UpdateRequest request) {

        userService.updateUser(user, request);

        return ResponseEntity
                .status(BaseResponseStatus.USER_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.USER_UPDATE_SUCCESS));
    }

    @Operation(summary = "✅ 로그아웃", description = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response)
    {
        userService.logout(response);

        return ResponseEntity
                .status(BaseResponseStatus.LOGOUT_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.LOGOUT_SUCCESS));
    }
}
