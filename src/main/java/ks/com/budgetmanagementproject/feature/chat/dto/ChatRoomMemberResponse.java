package ks.com.budgetmanagementproject.feature.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoom;
import ks.com.budgetmanagementproject.feature.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMemberResponse {

    private Long roomId;
    private String roomName;
    private Long userId;
    private String username;
    private Integer currentMemberCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;

    public static ChatRoomMemberResponse of(ChatRoom chatRoom, User user) {
        return ChatRoomMemberResponse.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .userId(user.getId())
                .username(user.getUsername())
                .currentMemberCount(chatRoom.getMemberCount())
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
