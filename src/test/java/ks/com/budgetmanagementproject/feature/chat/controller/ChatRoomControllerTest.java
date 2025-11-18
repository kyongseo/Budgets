package ks.com.budgetmanagementproject.feature.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.CreateRoomRequest;
import ks.com.budgetmanagementproject.feature.chat.service.ChatRoomService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ChatRoomController.class)
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ChatRoomService chatRoomService;
    @MockitoBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("POST /rooms - 채팅방 생성")
    class CreateRoom {

        @Test
        @DisplayName("채팅방_생성_성공")
        void createRoomSuccess() throws Exception {
            // given
            User mockUser = User.builder()
                    .id(1L)
                    .username("testUser")
                    .password("password")
                    .roles(new HashSet<>())
                    .build();

            CreateRoomRequest request = new CreateRoomRequest("테스트방");
            ChatRoomResponse response = new ChatRoomResponse(1L, "테스트방", "testUser", 1);

            when(chatRoomService.createRoom(any(CreateRoomRequest.class), eq("testUser")))
                    .thenReturn(response);

            // when & then
            mockMvc.perform(post("/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(new CustomUserDetails(mockUser)))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("채팅방이 생성되었습니다."));
            verify(chatRoomService).createRoom(any(CreateRoomRequest.class), eq("testUser"));
        }
    }
}