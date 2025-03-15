package com.raillylinker.web_socket_stomp_src

import com.raillylinker.configurations.SecurityConfig.AuthTokenFilterTotalAuth
import com.raillylinker.kafka_components.producers.Kafka1MainProducer
import org.springframework.context.annotation.Lazy
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component

// (STOMP 게이트웨이)
// StompInterceptorService 에서 각 요청에 대한 필터 역할을 합니다.
@Component
class StompGateway(
    @Lazy private val stompInterceptorService: StompInterceptorService,
    private val authTokenFilterTotalAuth: AuthTokenFilterTotalAuth,
    private val kafka1MainProducer: Kafka1MainProducer
) {
    // (Topic 구독 필터 함수)
    // 반환값 null 반환시 SUBSCRIBE 되지 않고, Exception 발생시엔 DISCONNECT 가 됩니다.
    fun filterSubscribe(
        message: Message<*>,
        channel: MessageChannel,
        accessor: StompHeaderAccessor
    ): Message<*>? {
        // 소켓 세션 아이디 (CONNECT 에 발행된 후 DISCONNECT 전까지 변화 없음)
        val sessionId = accessor.sessionId!!
        // 메시지 발행 경로 (ex : /app/send-to-topic-test)
        val destination = accessor.destination ?: return null
        // Authorization 헤더
        val authorization: String? = accessor.getFirstNativeHeader("Authorization")
        // 요청 코드 헤더
        val clientRequestCode: String? = accessor.getFirstNativeHeader("client-request-code")

        // 1. 클라이언트 요청 에러 토픽 (항상 오픈)
        if (destination == "/session/queue/request-error") {
            return message
        }

        // 아래로 경로(destination) 에 대한 필터링 처리를 진행하면 됩니다.

        // 2. 토픽 테스트 채널
        if (destination == "/topic/test-channel") {
            return message
        }

        // 위에서 정의되지 않은 경로는 모두 구독 거절
        return null
    }


    ////
    // (메시지 전송 필터 함수)
    // 반환값 null 반환시 메시지 전달이 되지 않고 Exception 발생시엔 DISCONNECT 가 됩니다.
    fun filterSend(
        message: Message<*>,
        channel: MessageChannel,
        accessor: StompHeaderAccessor
    ): Message<*>? {
        // 소켓 세션 아이디 (CONNECT 에 발행된 후 DISCONNECT 전까지 변화 없음)
        val sessionId = accessor.sessionId!!
        // 메시지 발행 경로 (ex : /app/send-to-topic-test)
        val destination = accessor.destination
        // Authorization 헤더
        val authorization: String? = accessor.getFirstNativeHeader("Authorization")
        // 요청 코드 헤더
        val clientRequestCode: String? = accessor.getFirstNativeHeader("client-request-code")

        // 메시지 발행 금지 설정시 아래와 같이 null 을 반환하고 에러 메시지를 발행합니다.
//        if (destination == "/app/topic/test-channel") {
//            // 개별 에러 메시지 발송
//            val stompSessionInfoKey = "${ModuleConst.SERVER_UUID}_${sessionId}"
//            val stompSessionInfoValue = stompInterceptorService.sessionInfoMap[stompSessionInfoKey]
//            if (stompSessionInfoValue != null) {
//                kafka1MainProducer.sendMessageToStomp(
//                    Kafka1MainProducer.SendMessageToStompInputVo(
//                        stompSessionInfoValue.principalUserName,
//                        "/queue/request-error",
//                        Gson().toJson(StompSubVos.SessionQueueRequestErrorVo(clientRequestCode, 1, "Need Login"))
//                    )
//                )
//            }
//
//            return null
//        }

        return message
    }
}