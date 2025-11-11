package ks.com.budgetmanagementproject.feature.chat.entity;

import ks.com.budgetmanagementproject.global.common.model.BaseTimeEntity;
import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseTimeEntity {

    private String messageId;
    private String roomId;
    private Long senderId;
    private String sender;
    private String message;

    public ChatMessage(String roomId, String sender, String message) {
        this.messageId = UUID.randomUUID().toString();
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
    }
}