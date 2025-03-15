package com.raillylinker.controllers

import com.fasterxml.jackson.annotation.JsonProperty
import com.raillylinker.services.WebSocketStompService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

// [WebSocket STOMP 컨트롤러]
// 특정 채널에 메시지를 발행시에는 /app/topic/**, /app/session/queue/** 이런 식으로 시작하고,
// 그외에는 자유롭게 정하면 됩니다.
@Controller
class WebSocketStompController(
    private val service: WebSocketStompService
) {
    // 메세지 함수 호출 경로 (WebSocketStompConfig 의 setApplicationDestinationPrefixes 설정과 합쳐서 호출, ex : /app/topic/test-channel)
    @MessageMapping("/topic/test-channel")
    fun appTopicTestChannel(inputVo: AppTopicTestChannelInputVo) {
        service.appTopicTestChannel(inputVo)
    }

    data class AppTopicTestChannelInputVo(
        @JsonProperty("chat")
        val chat: String
    )
}