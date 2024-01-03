package seoultech.capstone.menjil.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /*
        /queue: 1대1 대화의 경우
        /topic: 1대다 대화의 경우(현재는 고려하지 않음)
         */
        registry.enableSimpleBroker("/queue", "/topic"); // 메세지 구독 요청 url -> 메세지 받을 때

        registry.setApplicationDestinationPrefixes("/pub");  // 메세지 발행 요청 url -> 메세지 보낼 때
    }
}
