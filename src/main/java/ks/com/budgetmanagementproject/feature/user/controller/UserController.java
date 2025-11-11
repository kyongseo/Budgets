package ks.com.budgetmanagementproject.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ks.com.budgetmanagementproject.feature.user.dto.LoginReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResDto;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.UserEditDto;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.service.UserService;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpReqDto signUpReqDto) {

        userService.signUp(signUpReqDto);

        return ResponseEntity
                .status(BaseResponseStatus.SIGN_UP_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.SIGN_UP_SUCCESS));
    }

    @Operation(summary = "✅ 로그인", description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginReqDto loginReqDto, HttpServletResponse response) {

        LoginResDto loginResDto = userService.login(loginReqDto, response);

        return ResponseEntity
                .status(BaseResponseStatus.LOGIN_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.LOGIN_SUCCESS, loginResDto));
    }

    @Operation(summary = "✅ AccessToken 재발급", description = "AccessToken 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String newToken = userService.reissueToken(request, response);
        return ResponseEntity
                .status(BaseResponseStatus.ACCESS_TOKEN_REISSUE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.ACCESS_TOKEN_REISSUE_SUCCESS, newToken));
    }

    @Operation( summary = "✅ 사용자 정보 변경", description = "사용자 정보 변경")
    @PatchMapping()
    public ResponseEntity<?> getUpdateUser(Authentication authentication, @RequestBody UserEditDto userEditDto) {

        User updated = userService.updateUser(authentication, userEditDto);
        return ResponseEntity
                .status(BaseResponseStatus.USER_UPDATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.USER_UPDATE_SUCCESS, updated));
    }

    /**
     * 로그아웃
     * @param response 응답
     */
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
