package ks.com.budgetmanagementproject.feature.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ks.com.budgetmanagementproject.feature.user.dto.LoginRequest;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResponse;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpRequest;
import ks.com.budgetmanagementproject.feature.user.dto.UpdateRequest;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.service.UserService;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("POST /users/signup - 회원가입")
    class SignUp {

        @Test
        @DisplayName("회원가입_성공")
        void signUpSuccess() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .username("test@example.com")
                    .password("password123!")
                    .build();

            doNothing().when(userService).signUp(any(SignUpRequest.class));

            // when & then
            mockMvc.perform(post("/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").exists());

            verify(userService).signUp(any(SignUpRequest.class));
        }

        @Test
        @DisplayName("회원가입_실패_중복된_이메일")
        void signUpFailDuplicateEmail() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .username("test@example.com")
                    .password("password123!")
                    .build();

            doThrow(new BaseException(BaseExceptionStatus.DUPLICATE_EMAIL))
                    .when(userService).signUp(any(SignUpRequest.class));

            // when & then
            assertThrows(ServletException.class, () -> {
                mockMvc.perform(post("/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print());
            });

            verify(userService).signUp(any(SignUpRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /users/login - 로그인")
    class Login {

        @Test
        @DisplayName("로그인_성공")
        void loginSuccess() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .username("test@example.com")
                    .password("password123!")
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .accessToken("accessToken123")
                    .refreshToken("refreshToken123")
                    .userId(1L)
                    .username("test@example.com")
                    .build();

            given(userService.login(any(LoginRequest.class), any(HttpServletResponse.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(userService).login(any(LoginRequest.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("로그인_실패_존재하지_않는_사용자")
        void loginFailNonExistentUser() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .username("wrong@example.com")
                    .password("password123!")
                    .build();

            given(userService.login(any(LoginRequest.class), any(HttpServletResponse.class)))
                    .willThrow(new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));

            // when & then
            assertThrows(Exception.class, () -> {
                mockMvc.perform(post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print());
            });

            verify(userService).login(any(LoginRequest.class), any(HttpServletResponse.class));
        }
    }

    @Nested
    @DisplayName("POST /users/refresh - AccessToken 재발급")
    class ReissueToken {

        @Test
        @DisplayName("AccessToken_재발급_성공")
        void reissueTokenSuccess() throws Exception {
            // given
            String newAccessToken = "newAccessToken123";
            given(userService.reissueToken(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                    .willReturn(newAccessToken);

            // when & then
            mockMvc.perform(post("/users/refresh")
                            .cookie(new Cookie("refreshToken", "refreshToken123")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(userService).reissueToken(any(HttpServletRequest.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("AccessToken_재발급_실패_RefreshToken_없음")
        void reissueTokenFailNoRefreshToken() {
            // given
            doThrow(new BaseException(BaseExceptionStatus.NON_EXISTENT_TOKEN))
                    .when(userService).reissueToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

            // when & then
            assertThrows(Exception.class, () -> {
                mockMvc.perform(post("/users/refresh"))
                        .andDo(print());
            });

            verify(userService).reissueToken(any(), any(HttpServletResponse.class));
        }
    }

    @Nested
    @DisplayName("PATCH /users - 사용자 정보 변경")
    class UpdateUser {

        @Test
        @DisplayName("사용자_정보_변경_성공")
        void updateUserSuccess() throws Exception {
            // given
            UpdateRequest request = UpdateRequest.builder()
                    .nickname("newNickname")
                    .phoneNumber("010-1234-5678")
                    .build();

            doNothing().when(userService).updateUser(any(User.class), any(UpdateRequest.class));

            // when & then
            mockMvc.perform(patch("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(userService).updateUser(any(User.class), any(UpdateRequest.class));
        }

        @Test
        @DisplayName("사용자_정보_변경_실패_존재하지_않는_사용자")
        void updateUserFailNonExistentUser() {
            // given
            UpdateRequest request = UpdateRequest.builder()
                    .nickname("newNickname")
                    .phoneNumber("010-1234-5678")
                    .build();

            doThrow(new BaseException(BaseExceptionStatus.NON_EXISTENT_USER))
                    .when(userService).updateUser(any(User.class), any(UpdateRequest.class));

            // when & then
            assertThrows(Exception.class, () -> {
                mockMvc.perform(patch("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print());
            });

            verify(userService).updateUser(any(User.class), any(UpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /users/logout - 로그아웃")
    class Logout {

        @Test
        @DisplayName("로그아웃_성공")
        void logoutSuccess() throws Exception {
            // given
            doNothing().when(userService).logout(any(HttpServletResponse.class));

            // when & then
            mockMvc.perform(post("/users/logout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(userService).logout(any(HttpServletResponse.class));
        }
    }
}