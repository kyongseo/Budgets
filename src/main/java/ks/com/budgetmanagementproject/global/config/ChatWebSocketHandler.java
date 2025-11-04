package ks.com.budgetmanagementproject.global.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ks.com.budgetmanagementproject.feature.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomId(session);
        log.info("✅ WebSocket 연결 성공: roomId={}, sessionId={}", roomId, session.getId());

        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.info("📩 메시지 수신: {}", payload);

            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

            // Kafka로 발행
            kafkaTemplate.send("chat", chatMessage.getRoomId(), chatMessage);
            log.info("📤 Kafka 발행 완료: roomId={}, sender={}, message={}",
                    chatMessage.getRoomId(), chatMessage.getSender(), chatMessage.getMessage());

        } catch (Exception e) {
            log.error("❌ 메시지 처리 실패", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = extractRoomId(session);
        log.info("❎ WebSocket 연결 종료: roomId={}, sessionId={}, status={}",
                roomId, session.getId(), status);

        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("❌ WebSocket 전송 오류: sessionId={}", session.getId(), exception);
    }

    public void broadcastMessage(String roomId, ChatMessage message) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("⚠️ 방에 연결된 세션 없음: roomId={}", roomId);
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(payload);

            log.info("📨 브로드캐스트: roomId={}, 세션 수={}, sender={}, message={}",
                    roomId, sessions.size(), message.getSender(), message.getMessage());

            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.error("❌ 메시지 전송 실패: sessionId={}", session.getId(), e);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 변환 실패", e);
        }
    }

    private String extractRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}