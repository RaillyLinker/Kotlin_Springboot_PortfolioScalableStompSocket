package com.raillylinker.web_socket_stomp_src

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class StompInterceptor(
    private val stompInterceptorService: StompInterceptorService
) : ChannelInterceptor {
    /*
        [ChannelInterceptor 콜백 실행 순서]
        메시지가 발생되면 preSend -> postSend -> afterSendCompletion 순으로 실행됩니다.
        인터셉터 내에서 Exception 이 발생되면 preSend 의 DISCONNECT -> postSend -> afterSendCompletion 순서로 연결이 제거되며,
        DISCONNECT 에서 Exception 이 발생되면 더이상 진행되지 않습니다.
     */

    /*
        (메시지가 전송되기 전에 실행됨)
        Message 가 실제로 채널로 전송되기 전에 호출됩니다.
        필요한 경우 Message 를 수정할 수 있습니다.
        이 메서드가 null 을 반환하면 실제 전송 호출이 발생하지 않습니다.
     */
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        // 각 이벤트 콜백 처리를 서비스로 이관
        return when (accessor?.command) {
            StompCommand.CONNECT, StompCommand.STOMP -> {
                stompInterceptorService.connectFromPreSend(message, channel, accessor)
            }

            StompCommand.SUBSCRIBE -> {
                stompInterceptorService.subscribeFromPreSend(message, channel, accessor)
            }

            StompCommand.SEND -> {
                stompInterceptorService.sendFromPreSend(message, channel, accessor)
            }

            StompCommand.UNSUBSCRIBE -> {
                stompInterceptorService.unSubscribeFromPreSend(message, channel, accessor)
            }

            StompCommand.DISCONNECT -> {
                stompInterceptorService.disconnectFromPreSend(message, channel, accessor)
            }

            else -> {
                message
            }
        }
    }

    /*
        (메시지가 전송된 후 실행됨)
        send 호출 직후에 호출됩니다.
        sent 파라미터로 메시지 전송 성공 여부를 알 수 있습니다.
        preSend 함수가 null 을 반환한 경우 호출되지 않습니다.
     */
//    override fun postSend(message: Message<*>, channel: MessageChannel, sent: Boolean) {
//        super.postSend(message, channel, sent)
//    }

    /*
        (메시지 전송이 완료된 후 실행됨)
        예외가 발생했는지 여부에 관계없이 전송이 완료된 후 호출됩니다.
        preSend 함수가 null 을 반환한 경우 호출되지 않습니다.
     */
//    override fun afterSendCompletion(
//        message: Message<*>,
//        channel: MessageChannel,
//        sent: Boolean,
//        ex: Exception?
//    ) {
//        super.afterSendCompletion(message, channel, sent, ex)
//    }
}