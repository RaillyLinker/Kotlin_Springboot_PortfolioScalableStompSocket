package com.raillylinker.web_socket_stomp_src

// (Socket STOMP 구독 Value Object)
// STOMP 채널별 구독시 발행받는 메시지에 대한 Value Object
class StompSubVos {
    // (/session/queue/request-error)
    // 클라이언트가 보낸 Request 에 대하여, 서버측 에러 메시지
    data class SessionQueueRequestErrorVo(
        // 클라이언트 발송 요청 코드
        // 클라이언트가 요청 메시지를 전송할 때, 요청 헤더에 client-request-code 라는 키로 각 요청별 구분 가능한 고유값을 같이 전송할 수 있습니다.
        // 여기선 해당 요청에 대한 에러가 발생했다는 것을 의미하며, 요청 코드를 입력하지 않았다면 null 을 반환합니다.
        val clientRequestCode: String?,
        // 에러 코드
        // 1 : 로그인이 필요합니다. (Jwt 갱신 필요)
        val errorCode: Int,
        // 에러 메시지
        val errorMessage: String
    )

    // (/topic/test-channel)
    data class TopicTestChannelVo(
        val content: String
    )
}