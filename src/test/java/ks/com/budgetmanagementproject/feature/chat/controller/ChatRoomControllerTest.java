package ks.com.budgetmanagementproject.feature.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.CreateRoomRequest;
import ks.com.budgetmanagementproject.feature.chat.service.ChatRoomService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static io.lettuce.core.KillArgs.Builder.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            //given
            User mockUser = User.builder()
                    .id(1L)
                    .username("testUser")
                    .password("password")
                    .roles(new HashSet<>())
                    .build();

            CreateRoomRequest request = new CreateRoomRequest("테스트방");
            ChatRoomResponse response = new ChatRoomResponse(1L, "테스트방", "testUser", 1);

            when(chatRoomService.createRoom(any(), any())).thenReturn(response);

            // when & then
            mockMvc.perform(post("/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with((org.springframework.test.web.servlet.request.RequestPostProcessor) user(String.valueOf(mockUser)))  // User 직접 전달
                            .with(csrf()))
                    .andExpect(status().isCreated());

            verify(chatRoomService).createRoom(any(), eq(mockUser));
        }
    }
}