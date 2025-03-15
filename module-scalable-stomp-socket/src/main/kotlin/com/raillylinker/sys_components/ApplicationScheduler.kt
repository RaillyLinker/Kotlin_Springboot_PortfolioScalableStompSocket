package com.raillylinker.sys_components

import com.raillylinker.redis_map_components.redis1_main.Redis1_Map_StompSessionInfo
import com.raillylinker.web_socket_stomp_src.StompInterceptorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

// [Springboot Scheduler]
// 일정 시간마다 실행되는 함수들의 클래스 (프로젝트 Application 클래스 선언 위에 @EnableScheduling 추가 필요)
// scheduler 를 사용할 Class 에 @Component, Method 에 @Scheduled 추가
// @Scheduled 규칙 : Method 반환값 void, 매개변수 0개의 인터페이스 형태 사용

// 주의할 점으로는, Scale Out 으로 프로세스 복제시 스케쥴러 작업 역시 중복 실행될 수 있다는 것을 고려해야합니다.
@Component
@EnableAsync
class ApplicationScheduler(
    private val stompInterceptorService: StompInterceptorService,
    private val redis1MapStompSessionInfo: Redis1_Map_StompSessionInfo
) {
    private val classLogger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        // Socket Stomp 서버 HeartBeat 간격 (ms)
        const val STOMP_HEARTBEAT_MILLIS = 1000L
    }

    // (Stomp 서버 하트비트)
    // 일정 간격으로 STOMP 서버가 살아있다는 신호를 발생시킵니다.
    @Scheduled(fixedDelay = STOMP_HEARTBEAT_MILLIS)
    fun stompHeartBeatTask() {
        // redis 에 현재 서버내 세션 정보 저장
        // 서버 분산 환경에서 각 서버 내의 세션 정보를 Redis 안에 저장하여 공유합니다.
        // 하트비트 간격마다 갱신되지 못한 유저는 접속이 끊겼다고 간주합니다.
        for (sessionInfo in stompInterceptorService.sessionInfoMap) {
            redis1MapStompSessionInfo.saveKeyValue(
                sessionInfo.key,
                sessionInfo.value,
                // Stomp 서버 하트비트 스케쥴마다 Redis 에 정보를 갱신할 것이므로, 하트비트 타임 + 추가 여분 시간 설정
                STOMP_HEARTBEAT_MILLIS + 100L
            )
        }
    }

    // (initialDelay + fixedDelay)
    // initialDelay 값 이후 처음 실행 되고, fixedDelay 값에 따라 계속 실행 = fixedDelay 에 최초 실행 시간이 달린 것
//    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
//    fun scheduleFixedRateWithInitialDelayTask() {
//
//    }

    // (fixedRate)
    // 해당 메서드가 시작하는 시간 기준, milliseconds 간격으로 실행
    // 병렬로 Scheduler 를 사용할 경우, Class에 @EnableAsync, Method에 @Async 추가
//    @Async
//    @Scheduled(fixedRate = 1000)
//    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")  // 문자열 milliseconds 사용 시
//    fun scheduleFixedRateTask() {
//
//    }

    // (Cron)
    // 작업 예약으로 실행
//    @Scheduled(cron = "0 15 10 15 * ?", zone = "Asia/Seoul") // 매월 15일 오전 10시 15분에 실행
//    // @Scheduled(cron = "0 15 10 15 11 ?", zone = "Asia/Seoul") // 11월 15일 오전 10시 15분에 실행
//    // @Scheduled(cron = "${cron.expression}", zone = "Asia/Seoul")
//    // @Scheduled(cron = "0 15 10 15 * ?", zone = "Asia/Seoul") // timezone 설정
//    fun scheduleTaskUsingCronExpression() {
//
//    }
}