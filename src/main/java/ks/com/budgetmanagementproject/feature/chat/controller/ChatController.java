package ks.com.budgetmanagementproject.feature.chat.controller;

import ks.com.budgetmanagementproject.feature.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    // /pub/chat/{roomId} 로 받은 메시지를 /sub/{roomId} 로 전달
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessage message, Principal principal) {

        message.setSender(principal.getName());
        message.setRoomId(roomId);

        messagingTemplate.convertAndSend("/sub/" + roomId, message);
    }
}