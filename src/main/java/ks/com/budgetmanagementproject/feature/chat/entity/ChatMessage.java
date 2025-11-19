package ks.com.budgetmanagementproject.feature.chat.entity;

import ks.com.budgetmanagementproject.global.common.model.BaseTimeEntity;
import lombok.*;

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
}