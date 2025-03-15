package com.raillylinker.configurations

import com.raillylinker.web_socket_stomp_src.StompInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration

// [WebSocket STOMP 설정]
@EnableWebSocketMessageBroker
@Configuration
class WebSocketStompConfig(
    private val stompInterceptor: StompInterceptor
) : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            // STOMP 접속 EndPoint (ex : var socket = new SockJS('http://localhost:8080/stomp');)
            .addEndpoint("/stomp")
            // webSocket 연결 CORS 는 WebConfig 가 아닌 여기서 설정 (* 는 모든 것을 허용합니다.)
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.2/sockjs.js")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        /*
             발행 주소 prefix
             WebSocketStompController 의 MessageMapping 연결 주소 prefix
             이 설정이 /app 이고, @MessageMapping("/test") 라고 되어있다면,
             stompClient.send("/app/test", {}, JSON.stringify({'chat': "sample Text"}));
             이처럼 요청 합니다.
         */
        registry.setApplicationDestinationPrefixes("/app")

        /*
             구독 주소 prefix
             stompClient.subscribe('/topic/test-channel', function (topic) {
                 // 구독 콜백 : 구독된 채널에 메세지가 날아오면 여기서 받음
             });
             위와 같이 /topic/test-channel 이라는 것을 구독하면,
             simpMessagingTemplate.convertAndSend("/topic/test-channel", TopicVo("waiting..."))
             이렇게 메세지 전달시 그 메세지를 받을 수 있습니다.

             topic 은 Broadcast 의 의미고,
             queue 는 session 하나에 대한 발신입니다.(queue 는 아래의 user prefix 와 함께 사용합니다.)
         */
        registry.enableSimpleBroker("/topic", "/queue")

        /*
             유저 개별 주소 prefix
             stompClient.subscribe('/session/queue/test-channel', function (topic) {
                 // 구독 콜백 : 구독된 채널에 메세지가 날아오면 여기서 받음
             });
             위와 같이 /session/queue/test-channel 이라는 것을 구독하면,
             simpMessagingTemplate.convertAndSendToUser(
                userName, // socket session 의 user principal 이 반환하는 userName
                "/queue/test-channel",
                WebSocketStompController.SendToTopicTestOutputVo("Subscription denied: Unauthorized user. ${accessor.user?.name}")
             )
             이렇게 메세지 전달시 그 메세지를 받을 수 있습니다.
         */
        registry.setUserDestinationPrefix("/session")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompInterceptor)
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        // WebSocket 으로 전송되는 메시지의 최대 크기 설정
        registry.setMessageSizeLimit(160 * 64 * 1024)
        // 메시지 전송에 대한 시간 제한 설정
        registry.setSendTimeLimit(100 * 10000)
        // 송신 버퍼의 크기 제한을 설정
        registry.setSendBufferSizeLimit(3 * 512 * 1024)
    }
}