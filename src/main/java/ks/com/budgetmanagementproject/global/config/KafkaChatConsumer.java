package ks.com.budgetmanagementproject.global.config;

import ks.com.budgetmanagementproject.feature.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaChatConsumer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @KafkaListener(
            topics = "chat",
            groupId = "chat-websocket-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessage(ChatMessage message) {
        log.info("🎧 ==================== Kafka CONSUME 시작 ====================");
        log.info("🎧 roomId: {}", message.getRoomId());
        log.info("🎧 sender: {}", message.getSender());
        log.info("🎧 message: {}", message.getMessage());

        try {
            chatWebSocketHandler.broadcastMessage(message.getRoomId(), message);
            log.info("✅ 브로드캐스트 완료!");
        } catch (Exception e) {
            log.error("❌ 브로드캐스트 실패", e);
        }

        log.info("🎧 ==================== Kafka CONSUME 종료 ====================");
    }
}