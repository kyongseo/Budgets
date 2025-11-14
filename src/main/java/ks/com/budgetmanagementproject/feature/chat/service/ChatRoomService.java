package ks.com.budgetmanagementproject.feature.chat.service;

import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomMemberResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.ChatRoomResponse;
import ks.com.budgetmanagementproject.feature.chat.dto.CreateRoomRequest;
import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoom;
import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoomMember;
import ks.com.budgetmanagementproject.feature.chat.repository.ChatRoomMemberRepository;
import ks.com.budgetmanagementproject.feature.chat.repository.ChatRoomRepository;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import ks.com.budgetmanagementproject.feature.user.repository.UserRepository;
import ks.com.budgetmanagementproject.global.common.logger.BaseException;
import ks.com.budgetmanagementproject.global.common.logger.BaseExceptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    /**
     * 채팅방 생성
     * @param request CreateRoomRequest
     * @param user user
     * @return save
     */
    public ChatRoomResponse createRoom(CreateRoomRequest request, User user) {

        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(request.getRoomName())
                .creatorName(user.getUsername())
                .build();

        ChatRoomMember member = ChatRoomMember.builder()
                .userId(persistedUser.getId())
                .username(persistedUser.getUsername())
                .chatRoom(chatRoom)
                .build();

        chatRoom.addMember(member);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(savedChatRoom);
    }

    /**
     * 채팅방 목록 조회
     * @return 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getAllRooms() {

        List<ChatRoom> chatroom = chatRoomRepository.findAll();
        return chatroom.stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    /**
     * 채팅방 목록 상세 조회
     * @param roomId 방번호
     * @return 목록 상세
     */
    @Transactional(readOnly = true)
    public ChatRoomResponse getRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_FOUND));

        return ChatRoomResponse.from(chatRoom);
    }
    /**
     * 채팅방 입장
     * @param roomId 방번호
     * @param user   사용자
     */
    public void joinChatRoom(Long roomId, User user) {

        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_FOUND));

        if (chatRoom.hasMember(persistedUser.getId())) {
            throw new BaseException(BaseExceptionStatus.CHATROOM_ALREADY_JOINED);
        }

        ChatRoomMember member = ChatRoomMember.builder()
                .userId(persistedUser.getId())
                .username(persistedUser.getUsername())
                .chatRoom(chatRoom)
                .build();

        chatRoom.addMember(member);

        ChatRoomMemberResponse.of(chatRoom, persistedUser);
    }

    /**
     * 채팅방 퇴장
     * @param roomId 방번호
     * @param user   사용자
     */
    public void leaveRoom(Long roomId, User user) {

        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.NON_EXISTENT_USER));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_FOUND));
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoom_IdAndUserId(roomId, persistedUser.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.CHATROOM_NOT_MEMBER));

        chatRoom.removeMember(member);
        chatRoomMemberRepository.delete(member);

        ChatRoomMemberResponse.of(chatRoom, persistedUser);
    }
}