package ks.com.budgetmanagementproject.feature.chat.dto;

import ks.com.budgetmanagementproject.feature.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponse {

    private Long id;
    private String roomName;
    private String creatorName;
    private Integer memberCount;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .creatorName(chatRoom.getCreatorName())
                .memberCount(chatRoom.getMembers().size())
                .build();
    }

    public static ChatRoomResponse of(ChatRoom chatRoom, int memberCount) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .creatorName(chatRoom.getCreatorName())
                .memberCount(memberCount)
                .build();
    }
}