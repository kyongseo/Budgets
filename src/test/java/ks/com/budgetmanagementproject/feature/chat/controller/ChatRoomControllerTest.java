package ks.com.budgetmanagementproject.feature.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.CreateRoomRequest;
import ks.com.budgetmanagementproject.feature.chat.service.ChatRoomService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import ks.com.budgetmanagementproject.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ChatRoomController.class)
@WithMockUser(username = "testUser")
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ChatRoomService chatRoomService;
    @MockitoBean
    private UserRepository userRepository;

    private User createMockUser() {
        return User.builder()
                .id(1L)
                .username("testUser")
                .password("password")
                .roles(new HashSet<>())
                .build();
    }

    @Nested
    @DisplayName("POST /rooms - 채팅방 생성")
    class CreateRoom {

        @Test
        @DisplayName("채팅방_생성_성공")
        void createRoomSuccess() throws Exception {
            // given
            User mockUser = createMockUser();
            CreateRoomRequest request = new CreateRoomRequest("테스트방");
            ChatRoomResponse response = new ChatRoomResponse(1L, "테스트방", "testUser", 1);

            CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

            // when & then
            mockMvc.perform(post("/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(customUserDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").exists());
            verify(chatRoomService).createRoom(any(CreateRoomRequest.class), eq("testUser"));
        }

        @Test
        @DisplayName("채팅방_생성_실패_유효하지_않은_요청")
        void createRoomFail_InvalidRequest() throws Exception {
            // given
            User mockUser = createMockUser();
            CreateRoomRequest request = new CreateRoomRequest("");

            // when & then
            mockMvc.perform(post("/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(new CustomUserDetails(mockUser)))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /rooms - 채팅방 목록 조회")
    class GetRoomList {

        @Test
        @DisplayName("채팅방_목록_조회_성공")
        void getRoomListSuccess() throws Exception {
            // given
            List<ChatRoomResponse> responses = Arrays.asList(
                    new ChatRoomResponse(1L, "테스트방1", "user1", 3),
                    new ChatRoomResponse(2L, "테스트방2", "user2", 5)
            );

            when(chatRoomService.getAllRooms()).thenReturn(responses);

            // when & then
            mockMvc.perform(get("/rooms")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2));

            verify(chatRoomService).getAllRooms();
        }

        @Test
        @DisplayName("채팅방_목록_조회_성공_빈_목록")
        void getRoomListSuccess_EmptyList() throws Exception {
            // given
            when(chatRoomService.getAllRooms()).thenReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/rooms")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(0));

            verify(chatRoomService).getAllRooms();
        }
    }

    @Nested
    @DisplayName("GET /rooms/{roomId} - 채팅방 상세 조회")
    class GetRoomDetail {

        @Test
        @DisplayName("채팅방_상세_조회_성공")
        void getRoomDetailSuccess() throws Exception {
            // given
            Long roomId = 1L;
            ChatRoomResponse response = new ChatRoomResponse(roomId, "테스트방", "testUser", 5);

            when(chatRoomService.getRoomById(roomId)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/rooms/{roomId}", roomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.result.id").value(roomId))
                    .andExpect(jsonPath("$.result.roomName").value("테스트방"));


            verify(chatRoomService).getRoomById(roomId);
        }
    }

    @Nested
    @DisplayName("POST /rooms/{roomId}/join - 채팅방 입장")
    class JoinRoom {

        @Test
        @DisplayName("채팅방_입장_성공")
        void joinRoomSuccess() throws Exception {
            // given
            Long roomId = 1L;

            doNothing().when(chatRoomService).joinChatRoom(eq(roomId), any());

            // when & then
            mockMvc.perform(post("/rooms/{roomId}/join", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(chatRoomService).joinChatRoom(eq(roomId), any());
        }

        @Test
        @DisplayName("채팅방_입장_실패_이미_참여중")
        void joinRoomFail_AlreadyJoined() {
            // given
            Long roomId = 1L;

            doThrow(new BaseException(BaseExceptionStatus.CHATROOM_ALREADY_JOINED))
                    .when(chatRoomService).joinChatRoom(eq(roomId), any());

            Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(post("/rooms/{roomId}/join", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andDo(print()));

            assertInstanceOf(BaseException.class, exception.getCause());

            verify(chatRoomService).joinChatRoom(eq(roomId), any());
        }
    }

    @Nested
    @DisplayName("DELETE /rooms/{roomId}/leave - 채팅방 퇴장")
    class LeaveRoom {

        @Test
        @DisplayName("채팅방_퇴장_성공")
        void leaveRoomSuccess() throws Exception {
            // given
            Long roomId = 1L;

            doNothing().when(chatRoomService).leaveRoom(eq(roomId), any());

            // when & then
            mockMvc.perform(post("/rooms/{roomId}/join", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").exists());

            verify(chatRoomService).joinChatRoom(eq(roomId), any());
        }

        @Test
        @DisplayName("채팅방_퇴장_실패_참여하지_않은_방")
        void leaveRoomFail_NotJoined() {
            // given
            Long roomId = 1L;

            doThrow(new BaseException(BaseExceptionStatus.CHATROOM_NOT_MEMBER))
                    .when(chatRoomService).leaveRoom(eq(roomId), any());

            // when & then
            Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(delete("/rooms/{roomId}/leave", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andDo(print()));

            assertInstanceOf(BaseException.class, exception.getCause());

            verify(chatRoomService).leaveRoom(eq(roomId), any());
        }
    }
}