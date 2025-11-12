package ks.com.budgetmanagementproject.feature.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import ks.com.budgetmanagementproject.feature.token.repository.RefreshRepository;
import ks.com.budgetmanagementproject.feature.user.dto.LoginReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResDto;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.UserEditDto;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
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
    RefreshRepository refreshRepository;

    @Mock
    JWTUtil jwtUtil;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("회원가입_성공")
    void signUpTestSuccess() {

        // given
        SignUpReqDto req = new SignUpReqDto("test1234@test.com", "1234");
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
        LoginReqDto req = new LoginReqDto("test1234@test.com", "1234");

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
        given(refreshRepository.save(any(RefreshToken.class)))
                .willAnswer(inv -> inv.getArgument(0));

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        LoginResDto res = userService.login(req, response);

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
        verify(refreshRepository).save(any(RefreshToken.class));
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtUtil, refreshRepository);
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
        UserEditDto editDto = new UserEditDto("newNickname", "010-1234-5678");

        User user = new User();
        user.setId(1L);
        user.setUsername("test@test.com");
        user.setNickname("oldNickname");
        user.setPhoneNumber("010-0000-0000");

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        given(customUserDetails.getUsername()).willReturn("test@test.com");

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(customUserDetails);

        given(userRepository.findByUsername("test@test.com"))
                .willReturn(Optional.of(user));
        given(userRepository.save(any(User.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        User updatedUser = userService.updateUser(authentication, editDto);

        // then
        assertNotNull(updatedUser);
        assertEquals("newNickname", updatedUser.getNickname());
        assertEquals("010-1234-5678", updatedUser.getPhoneNumber());
        assertEquals("test@test.com", updatedUser.getUsername());

        verify(userRepository).findByUsername("test@test.com");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("사용자_정보_업데이트_실패_사용자_없음")
    void updateUserFailUserNotFound() {

        // given
        UserEditDto editDto = new UserEditDto("newNickname", "010-1234-5678");

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        given(customUserDetails.getUsername()).willReturn("notexist@test.com");

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(customUserDetails);

        given(userRepository.findByUsername("notexist@test.com"))
                .willReturn(Optional.empty());

        // when & then
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateUser(authentication, editDto));

        assertEquals(BaseExceptionStatus.NON_EXISTENT_USER, exception.getStatus());
        verify(userRepository).findByUsername("notexist@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자_정보_업데이트_실패_잘못된_Principal")
    void updateUserFailInvalidPrincipal() {

        // given
        UserEditDto editDto = new UserEditDto("newNickname", "010-1234-5678");

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn("InvalidPrincipal"); // String 타입

        // when & then
        BaseException exception = assertThrows(BaseException.class,
                () -> userService.updateUser(authentication, editDto));

        assertEquals(BaseExceptionStatus.NON_EXISTENT_USER, exception.getStatus());
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("로그아웃_성공_모든_토큰_삭제")
    void logoutSuccessAllTokens() {

        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("accessToken", "AT");
        response.setHeader("refreshToken", "RT");

        // when
        userService.logout(response);

        // then
        Cookie accessCookie = response.getCookie("accessToken");
        Cookie refreshCookie = response.getCookie("refreshToken");

        assertNotNull(accessCookie);
        assertNotNull(refreshCookie);
        assertNull(accessCookie.getValue());
        assertNull(refreshCookie.getValue());
        assertEquals(0, accessCookie.getMaxAge());
        assertEquals(0, refreshCookie.getMaxAge());
        assertEquals("/", accessCookie.getPath());
        assertEquals("/", refreshCookie.getPath());
        assertTrue(accessCookie.isHttpOnly());
        assertTrue(refreshCookie.isHttpOnly());
        assertFalse(accessCookie.getSecure());
        assertFalse(refreshCookie.getSecure());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("로그아웃_성공_AccessToken만_삭제")
    void logoutSuccessOnlyAccessToken() {

        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("accessToken", "AT");

        // when
        userService.logout(response);

        // then
        Cookie accessCookie = response.getCookie("accessToken");
        assertNotNull(accessCookie);
        assertNull(accessCookie.getValue());
        assertEquals(0, accessCookie.getMaxAge());
        assertTrue(accessCookie.isHttpOnly());

        Cookie refreshCookie = response.getCookie("refreshToken");
        assertNull(refreshCookie);
    }

    @Test
    @DisplayName("로그아웃_성공_RefreshToken만_삭제")
    void logoutSuccessOnlyRefreshToken() {

        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("refreshToken", "RT");

        // when
        userService.logout(response);

        // then
        Cookie refreshCookie = response.getCookie("refreshToken");
        assertNotNull(refreshCookie);
        assertNull(refreshCookie.getValue());
        assertEquals(0, refreshCookie.getMaxAge());
        assertTrue(refreshCookie.isHttpOnly());

        Cookie accessCookie = response.getCookie("accessToken");
        assertNull(accessCookie);
    }

    @Test
    @DisplayName("로그아웃_토큰_없음_이미_로그아웃_상태")
    void logoutNoTokensAlreadyLoggedOut() {

        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        userService.logout(response);

        // then
        Cookie accessCookie = response.getCookie("accessToken");
        Cookie refreshCookie = response.getCookie("refreshToken");
        assertNull(accessCookie);
        assertNull(refreshCookie);
    }
}