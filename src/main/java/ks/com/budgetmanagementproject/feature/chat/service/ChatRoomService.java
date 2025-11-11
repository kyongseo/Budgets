package ks.com.budgetmanagementproject.feature.chat.service;

import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.CreateRoomRequest;
import ks.com.budgetmanagementproject.feature.chat.dto.MemberInfo;
import ks.com.budgetmanagementproject.feature.chat.dto.RoomDetailResponse;
import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoom;
import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoomMember;
import ks.com.budgetmanagementproject.feature.chat.repository.ChatRoomMemberRepository;
import ks.com.budgetmanagementproject.feature.chat.repository.ChatRoomRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 채팅방 생성
     * @param request CreateRoomRequest
     * @param user user
     * @return save
     */
    public ChatRoomResponse createRoom(CreateRoomRequest request, User user) {

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(request.getRoomName())
                .creatorName(user.getUsername())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMember member = ChatRoomMember.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .chatRoom(savedRoom)
                .build();
        chatRoomMemberRepository.save(member);

        return ChatRoomResponse.builder()
                .id(savedRoom.getId())
                .roomName(savedRoom.getRoomName())
                .creatorName(savedRoom.getCreatorName())
                .memberCount(1)
                .build();
    }

    /**
     *  채팅방 목록 조회
     * @return 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRoomList() {

        List<ChatRoomResponse> rooms = chatRoomRepository.findAll().stream()
                .map(room -> ChatRoomResponse.builder()
                        .id(room.getId())
                        .roomName(room.getRoomName())
                        .creatorName(room.getCreatorName())
                        .memberCount(room.getMembers().size())
                        .build())
                .collect(Collectors.toList());
        return rooms;
    }

    /**
     * 채팅방 목록 상세 조회
     * @param roomId 방번호
     * @return 목록 상세
     */
    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetail(Long roomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_FOUND));

        List<MemberInfo> members = chatRoom.getMembers().stream()
                .map(member -> new MemberInfo(member.getUserId(), member.getUsername()))
                .collect(Collectors.toList());

        return RoomDetailResponse.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .creatorName(chatRoom.getCreatorName())
                .members(members)
                .build();
    }

    /**
     * 채팅방 입장
     * @param roomId 방번호
     * @param user 사용자
     */
    public void joinRoom(Long roomId, User user) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_FOUND));

        if (chatRoomMemberRepository.existsByChatRoom_IdAndUserId(roomId, user.getId())) {
            throw new BaseException(BaseExceptionStatus.CHATROOM_ALREADY_JOINED);
        }

        ChatRoomMember member = ChatRoomMember.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .chatRoom(chatRoom)
                .build();

        chatRoomMemberRepository.save(member);
    }

    /**
     * 채팅방 퇴장
     * @param roomId 방번호
     * @param user 사용자
     */
    public void leaveRoom(Long roomId, User user) {

        ChatRoomMember member = chatRoomMemberRepository.findByChatRoom_IdAndUserId(roomId, user.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_MEMBER));

        chatRoomMemberRepository.delete(member);
    }
}