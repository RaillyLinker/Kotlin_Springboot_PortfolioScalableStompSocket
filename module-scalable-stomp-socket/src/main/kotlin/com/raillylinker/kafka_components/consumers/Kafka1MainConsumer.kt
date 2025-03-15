package com.raillylinker.kafka_components.consumers

import com.google.gson.Gson
import com.raillylinker.configurations.jpa_configs.Db1MainConfig
import com.raillylinker.configurations.kafka_configs.Kafka1MainConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.transaction.annotation.Transactional

@Component
class Kafka1MainConsumer(
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    // <멤버 변수 공간>
    private val classLogger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        // !!!모듈 컨슈머 그룹 아이디!!!
        private const val CONSUMER_GROUP_ID = "com.raillylinker.service_board"
    }

    // ---------------------------------------------------------------------------------------------
    // <공개 메소드 공간>
    // (Stomp 메시지 전송 이벤트)
    @KafkaListener(
        topics = ["stomp_send-message"],
        containerFactory = Kafka1MainConfig.CONSUMER_BEAN_NAME
    )
    fun stompSendMessage(data: ConsumerRecord<String, String>) {
        val inputVo = Gson().fromJson(
            data.value(),
            StompSendMessageInputVo::class.java
        )

        if (inputVo.principalName == null) {
            simpMessagingTemplate.convertAndSend(
                inputVo.destination,
                inputVo.messageJsonString
            )
        } else {
            simpMessagingTemplate.convertAndSendToUser(
                inputVo.principalName,
                inputVo.destination,
                inputVo.messageJsonString
            )
        }
    }

    data class StompSendMessageInputVo(
        // principalName : null 이라면 topic 전송, not null 이라면 queue 전송
        val principalName: String?,
        // 전송 주소(ex : /topic/server-heartbeat, /session/queue/test-channel)
        val destination: String,
        // 전송 메시지 Object 직렬화 String
        val messageJsonString: String
    )


    ////
    // (Auth 모듈의 통합 멤버 정보 삭제 이벤트에 대한 리스너)
    // 이와 연관된 데이터 삭제 및 기타 처리
    @Transactional(transactionManager = Db1MainConfig.TRANSACTION_NAME)
    @KafkaListener(
        topics = ["auth_member-deleted"],
        groupId = CONSUMER_GROUP_ID,
        containerFactory = Kafka1MainConfig.CONSUMER_BEAN_NAME
    )
    fun fromAuthDbDeleteFromRaillyLinkerCompanyTotalAuthMemberListener(data: ConsumerRecord<String, String>) {
        classLogger.info(
            """
                KafkaConsumerLog>>
                {
                    "data" : {
                        "$data"
                    }
                }
            """.trimIndent()
        )

        val inputVo = Gson().fromJson(
            data.value(),
            FromAuthDbDeleteFromRaillyLinkerCompanyTotalAuthMemberListenerInputVo::class.java
        )

        classLogger.info("Deleted Member Uid : ${inputVo.deletedMemberUid}")

        // !!!멤버 테이블을 조회중인 테이블이 있을 경우 회원 탈퇴에 따른 처리를 이곳에 작성하세요.!!!
    }

    data class FromAuthDbDeleteFromRaillyLinkerCompanyTotalAuthMemberListenerInputVo(
        val deletedMemberUid: Long
    )
}