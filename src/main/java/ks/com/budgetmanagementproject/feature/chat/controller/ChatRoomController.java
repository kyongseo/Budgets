package ks.com.budgetmanagementproject.feature.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.CreateRoomRequest;
import ks.com.budgetmanagementproject.feature.chat.service.ChatRoomService;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponse;
import ks.com.budgetmanagementproject.global.common.logger.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ChatRoom", description = "ChatRoom API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    @Operation(summary = "✅ 채팅방 생성", description = "로그인한 사용자가 채팅방 생성")
    public ResponseEntity<?> createRoom_endpoint(
            @Validated @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal User user) {

        chatRoomService.createRoom(request, user);

        return ResponseEntity
                .status(BaseResponseStatus.CHATROOM_CREATE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.CHATROOM_CREATE_SUCCESS));
    }

    @GetMapping
    @Operation(summary = "✅ 채팅방 목록 조회", description = "모든 채팅방 목록 조회")
    public ResponseEntity<?> getRoomList_endpoint() {

        List<ChatRoomResponse> response = chatRoomService.getAllRooms();

        return ResponseEntity
                .status(BaseResponseStatus.CHATROOM_LIST_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.CHATROOM_LIST_SUCCESS, response));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "✅ 채팅방 상세 조회", description = "채팅방 상세 정보 및 멤버 목록 조회")
    public ResponseEntity<?> getRoomDetail_endpoint(@PathVariable Long roomId) {

        ChatRoomResponse response = chatRoomService.getRoomById(roomId);

        return ResponseEntity
                .status(BaseResponseStatus.CHATROOM_DETAIL_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.CHATROOM_DETAIL_SUCCESS, response));
    }

    @PostMapping("/{roomId}/join")
    @Operation(summary = "✅ 채팅방 입장", description = "채팅방에 입장")
    public ResponseEntity<?> joinRoom_endpoint(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user) {

        chatRoomService.joinChatRoom(roomId, user);

        return ResponseEntity
                .status(BaseResponseStatus.CHATROOM_JOIN_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.CHATROOM_JOIN_SUCCESS));
    }

    @DeleteMapping("/{roomId}/leave")
    @Operation(summary = "✅ 채팅방 퇴장", description = "채팅방에서 나가기")
    public ResponseEntity<?> leaveRoom_endpoint(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User user) {

        chatRoomService.leaveRoom(roomId, user);

        return ResponseEntity
                .status(BaseResponseStatus.CHATROOM_LEAVE_SUCCESS.getStatus())
                .body(BaseResponse.of(BaseResponseStatus.CHATROOM_LEAVE_SUCCESS));
    }
}