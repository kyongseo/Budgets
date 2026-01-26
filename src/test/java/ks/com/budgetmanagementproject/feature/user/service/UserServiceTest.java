package ks.com.budgetmanagementproject.feature.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.service.RedisBlackTokenService;
import ks.com.budgetmanagementproject.feature.token.service.RedisRefreshTokenService;
import ks.com.budgetmanagementproject.feature.user.dto.LoginRequest;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResponse;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpRequest;
import ks.com.budgetmanagementproject.feature.user.dto.UpdateRequest;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JWTUtil jwtUtil;

    @InjectMocks
    UserService userService;

    @Mock
    RedisRefreshTokenService redisRefreshTokenService;

    @Mock
    RedisBlackTokenService redisBlackTokenService;

    @Test
    @DisplayName("회원가입_성공")
    void signUpTestSuccess() {

        // given
        SignUpRequest req = new SignUpRequest("test1234@test.com", "1234");
        given(userRepository.existsByUsername(req.getUsername())).willReturn(false);

        Role role = new Role(1L, "USER", new HashSet<>());
        given(roleRepository.findByName("USER")).willReturn(Optional.of(role));

        // when
        assertDoesNotThrow(() -> userService.signUp(req));

        // then
        verify(userRepository).existsByUsername(req.getUsername());
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("로그인_성공")
    void loginTestSuccess() {

        // given
        LoginRequest req = new LoginRequest("test1234@test.com", "1234");
        User user = new User();
        user.setId(100L);
        user.setUsername("test1234@test.com");
        user.setPassword("ENC_PW");
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(1L, "USER", new HashSet<>()));
        user.setRoles(roles);

        given(userRepository.findByUsername(req.getUsername()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("1234", "ENC_PW"))
                .willReturn(true);
        given(jwtUtil.createAccessToken(eq(100L), eq("test1234@test.com"), anyList(), anyLong()))
                .willReturn("AT");
        given(jwtUtil.createRefreshToken(eq(100L), eq("test1234@test.com"), anyList(), anyLong()))
                .willReturn("RT");

        doNothing().when(redisRefreshTokenService)
                .addRefreshToken(eq("RT"), anyLong());

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        LoginResponse res = userService.login(req, response);

        // then
        assertNotNull(res);
        assertEquals("AT", res.getAccessToken());
        assertEquals("RT", res.getRefreshToken());
        assertEquals(100L, res.getUserId());
        assertEquals("test1234@test.com", res.getUsername());

        Cookie accessCookie = response.getCookie("accessToken");
        Cookie refreshCookie = response.getCookie("refreshToken");
        assertNotNull(accessCookie);
        assertNotNull(refreshCookie);
        assertEquals("AT", accessCookie.getValue());
        assertEquals("RT", refreshCookie.getValue());
        assertTrue(accessCookie.isHttpOnly());
        assertTrue(refreshCookie.isHttpOnly());

        verify(userRepository).findByUsername("test1234@test.com");
        verify(passwordEncoder).matches("1234", "ENC_PW");
        verify(jwtUtil).createAccessToken(eq(100L), eq("test1234@test.com"), anyList(), anyLong());
        verify(jwtUtil).createRefreshToken(eq(100L), eq("test1234@test.com"), anyList(), anyLong());
        verify(redisRefreshTokenService).addRefreshToken(eq("RT"), anyLong());
    }

    @Test
    @DisplayName("AccessToken_재발급_성공")
    void reissueTokenTestSuccess() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Cookie refreshCookie = new Cookie("refreshToken", "RT");
        request.setCookies(refreshCookie);

        doNothing().when(jwtUtil).isExpired("RT");

        given(jwtUtil.getUsername("RT")).willReturn("test@test.com");
        given(jwtUtil.getUserId("RT")).willReturn(1L);
        given(jwtUtil.getRole("RT")).willReturn("ROLE_USER");
        given(jwtUtil.createAccessToken(eq(1L), eq("test@test.com"), anyList(), anyLong()))
                .willReturn("NEW_AT");

        // when
        String newAccessToken = userService.reissueToken(request, response);

        // then
        assertNotNull(newAccessToken);
        assertEquals("NEW_AT", newAccessToken);

        Cookie accessCookie = response.getCookie("accessToken");
        assertNotNull(accessCookie);
        assertEquals("NEW_AT", accessCookie.getValue());
        assertTrue(accessCookie.isHttpOnly());
        assertEquals("/", accessCookie.getPath());

        verify(jwtUtil).isExpired("RT");
        verify(jwtUtil).getUsername("RT");
        verify(jwtUtil).getUserId("RT");
        verify(jwtUtil).getRole("RT");
        verify(jwtUtil).createAccessToken(eq(1L), eq("test@test.com"), anyList(), anyLong());
    }

    @Test
    @DisplayName("AccessToken_재발급_실패_RefreshToken_없음")
    void reissueTokenFailNoToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        BaseException exception = assertThrows(BaseException.class,
                () -> userService.reissueToken(request, response));

        assertEquals(BaseExceptionStatus.NON_EXISTENT_TOKEN, exception.getStatus());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("AccessToken_재발급_실패_RefreshToken_만료")
    void reissueTokenFailExpired() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Cookie refreshCookie = new Cookie("refreshToken", "EXPIRED_RT");
        request.setCookies(refreshCookie);

        doThrow(new ExpiredJwtException(null, null, "Token expired"))
                .when(jwtUtil).isExpired("EXPIRED_RT");

        // when & then
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.reissueToken(request, response));

        assertEquals(BaseExceptionStatus.NON_EXISTENT_TOKEN, exception.getStatus());
        verify(jwtUtil).isExpired("EXPIRED_RT");
        verify(jwtUtil, never()).getUsername(any());
    }

    @Test
    @DisplayName("사용자_정보_업데이트_성공")
    void updateUserSuccess() {
        // given
        UpdateRequest request = new UpdateRequest("newNickname", "010-1234-5678");
        User user = new User();
        user.setId(1L);
        user.setUsername("test@test.com");
        user.setNickname("oldNickname");
        user.setPhoneNumber("010-0000-0000");

        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));

        // when
        userService.updateUser(user, request);

        // then
        assertEquals("newNickname", user.getNickname());
        assertEquals("010-1234-5678", user.getPhoneNumber());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("사용자_정보_업데이트_실패_사용자_없음")
    void updateUserFailUserNotFound() {
        // given
        UpdateRequest request = new UpdateRequest("newNickname", "010-1234-5678");
        User user = new User();
        user.setId(999L);

        given(userRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateUser(user, request));

        assertEquals(BaseExceptionStatus.NON_EXISTENT_USER, exception.getStatus());
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("로그아웃_성공_모든_토큰_삭제")
    void logoutSuccessAllTokens() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("accessToken", "AT");
        response.setHeader("refreshToken", "RT");

        given(jwtUtil.getExpiration("AT"))
                .willReturn(System.currentTimeMillis() + 10000);

        doNothing().when(redisRefreshTokenService).deleteRefreshToken("RT");
        doNothing().when(redisBlackTokenService).addBlacklistedToken(eq("AT"), anyLong());

        // when
        userService.logout(response);

        // then
        verify(redisRefreshTokenService).deleteRefreshToken("RT");
        verify(redisBlackTokenService).addBlacklistedToken(eq("AT"), anyLong());
        verify(jwtUtil).getExpiration("AT");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("로그아웃_성공_AccessToken만_삭제")
    void logoutSuccessOnlyAccessToken() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("accessToken", "AT");

        given(jwtUtil.getExpiration("AT"))
                .willReturn(System.currentTimeMillis() + 10000);

        doNothing().when(redisBlackTokenService).addBlacklistedToken(eq("AT"), anyLong());

        // when
        userService.logout(response);

        // then
        verify(redisBlackTokenService).addBlacklistedToken(eq("AT"), anyLong());
        verify(jwtUtil).getExpiration("AT");
        verify(redisRefreshTokenService, never()).deleteRefreshToken(any());
    }

    @Test
    @DisplayName("로그아웃_성공_RefreshToken만_삭제")
    void logoutSuccessOnlyRefreshToken() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("refreshToken", "RT");

        doNothing().when(redisRefreshTokenService).deleteRefreshToken("RT");

        // when
        userService.logout(response);

        // then
        verify(redisRefreshTokenService).deleteRefreshToken("RT");
        verify(redisBlackTokenService, never()).addBlacklistedToken(any(), anyLong());
        verify(jwtUtil, never()).getExpiration(any());
    }

    @Test
    @DisplayName("로그아웃_토큰_없음_이미_로그아웃_상태")
    void logoutNoTokensAlreadyLoggedOut() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        userService.logout(response);

        // then
        verifyNoInteractions(redisRefreshTokenService);
        verifyNoInteractions(redisBlackTokenService);
        verifyNoInteractions(jwtUtil);
    }
}