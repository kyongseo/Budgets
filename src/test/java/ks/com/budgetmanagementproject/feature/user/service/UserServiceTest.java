package ks.com.budgetmanagementproject.feature.user.service;

import jakarta.servlet.http.Cookie;
import ks.com.budgetmanagementproject.feature.role.entity.Role;
import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.entity.RefreshToken;
import ks.com.budgetmanagementproject.feature.token.repository.RefreshRepository;
import ks.com.budgetmanagementproject.feature.user.dto.LoginReqDto;
import ks.com.budgetmanagementproject.feature.user.dto.LoginResDto;
import ks.com.budgetmanagementproject.feature.user.dto.SignUpReqDto;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        ResponseEntity<?> res = userService.login(req, response);

        // then
        assertEquals(200, res.getStatusCodeValue());
        assertInstanceOf(LoginResDto.class, res.getBody());

        LoginResDto body = (LoginResDto) res.getBody();
        assertEquals("AT", body.getAccessToken());
        assertEquals("RT", body.getRefreshToken());
        assertEquals(100L, body.getUserId());
        assertEquals("test1234@test.com", body.getUsername());

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
}