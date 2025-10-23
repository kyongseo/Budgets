package ks.com.budgetmanagementproject.global.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 스프링의 인메모리 기반 메시지 브로커를 사용한다는 설정
        // 클라이언트가 메시지를 구독할 때 사용할 접두사를 /sub 설정
        registry.enableSimpleBroker("/sub");

        // 클라이언트가 메시지를 서버로 보낼 때 사용할 접두사를 /pub 설정
        registry.setApplicationDestinationPrefixes("/pub");

        // 특정 사용자에게 메시지를 보낼 때 사용할 접두사를 /user 설정
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 오프닝 핸드셰이크 과정에서 사용할 앤드포인트 지정
     * ws://localhost:{port}/ws-stomp --> https를 사용하면 wss
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 주소 (프론트에서 SockJS가 여길 호출함)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
