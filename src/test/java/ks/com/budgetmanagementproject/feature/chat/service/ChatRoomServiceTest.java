package ks.com.budgetmanagementproject.feature.chat.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private UserRepository userRepository;


    @Test
    @DisplayName("성공_채팅방 생성")
    void createRoom_success() {
        // given
        CreateRoomRequest request = new CreateRoomRequest("테스트방");
        User user = User.builder().id(1L).username("kyeong").build();

        when(userRepository.findByUsername("kyeong"))
                .thenReturn(Optional.of(user));

        ChatRoom savedChatRoom = ChatRoom.builder()
                .id(10L)
                .roomName("테스트방")
                .creatorName("kyeong")
                .build();

        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(savedChatRoom);

        // when
        ChatRoomResponse response = chatRoomService.createRoom(request, "kyeong");

        // then
        assertThat(response.getId()).isEqualTo(10L);
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("실패_채팅방_생성_사용자존재안홤")
    void createRoom_fail_userNotFound() {
        // given
        CreateRoomRequest request = new CreateRoomRequest("테스트방");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatRoomService.createRoom(request, "unknown"))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.NON_EXISTENT_USER);
    }


    @Test
    @DisplayName("성공_채팅방_전체_조회")
    void getAllRooms_success() {
        // give
        ChatRoom chatRoom = ChatRoom.builder()
                .id(1L)
                .roomName("roomA")
                .build();

        when(chatRoomRepository.findAll()).thenReturn(List.of(chatRoom));

        // when
        List<ChatRoomResponse> list = chatRoomService.getAllRooms();

        // then
        assertThat(list.get(0).getRoomName()).isEqualTo("roomA");
        assertThat(list.size()).isEqualTo(1);
    }


    @Test
    @DisplayName("성공_채팅방_상세조회")
    void getRoomById_success() {
        ChatRoom room = ChatRoom.builder().id(1L).roomName("roomA").build();

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

        ChatRoomResponse response = chatRoomService.getRoomById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패_채팅방_상세조회_존재하지않음")
    void getRoomById_fail_notFound() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.getRoomById(1L))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.CHATROOM_NOT_FOUND);
    }


    @Test
    @DisplayName("성공_채팅방_입장")
    void joinChatRoom_success() {
        // give
        User user = User.builder().id(1L).username("abc").build();
        ChatRoom room = Mockito.spy(ChatRoom.builder().id(10L).roomName("A").build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        doReturn(false).when(room).hasMember(1L);

        // when
        chatRoomService.joinChatRoom(10L, user);

        // then
        verify(room).addMember(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("실패_채팅방_입장_이미입장상태")
    void joinChatRoom_fail_alreadyJoined() {
        User user = User.builder().id(1L).username("abc").build();
        ChatRoom room = Mockito.spy(ChatRoom.builder().id(10L).roomName("A").build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        doReturn(true).when(room).hasMember(1L);

        assertThatThrownBy(() -> chatRoomService.joinChatRoom(10L, user))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.CHATROOM_ALREADY_JOINED);
    }


    @Test
    @DisplayName("성공_채팅방_퇴장")
    void leaveRoom_success() {
        // give
        User user = User.builder().id(1L).username("abc").build();
        ChatRoom room = Mockito.spy(ChatRoom.builder().id(10L).roomName("A").build());
        ChatRoomMember member = ChatRoomMember.builder().id(100L).userId(1L).chatRoom(room).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatRoomMemberRepository.findByChatRoom_IdAndUserId(10L, 1L))
                .thenReturn(Optional.of(member));

        // when
        chatRoomService.leaveRoom(10L, user);

        // then
        verify(room).removeMember(member);
        verify(chatRoomMemberRepository).delete(member);
    }

    @Test
    @DisplayName("실패_채팅방_퇴장_멤버없음")
    void leaveRoom_fail_notMember() {
        User user = User.builder().id(1L).username("abc").build();
        ChatRoom room = ChatRoom.builder().id(10L).roomName("A").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(chatRoomMemberRepository.findByChatRoom_IdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.leaveRoom(10L, user))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("status", BaseExceptionStatus.CHATROOM_NOT_MEMBER);
    }
}
