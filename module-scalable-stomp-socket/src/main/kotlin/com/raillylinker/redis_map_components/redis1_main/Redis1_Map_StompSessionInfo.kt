package com.raillylinker.redis_map_components.redis1_main

import com.raillylinker.abstract_classes.BasicRedisMap
import com.raillylinker.configurations.redis_configs.Redis1MainConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

// [RedisMap 컴포넌트]
// StompSession 정보 (Stomp 서버에서 정한 하트비트 인터벌마다 Session 정보를 갱신하며, 만약 갱신이 되지 않는다면 해당 유저의 정보는 사라지고 연결 종료로 취급됩니다.)
// Key : ${serverUuid}_${sessionId}
@Component
class Redis1_Map_StompSessionInfo(
    // !!!RedisConfig 종류 변경!!!
    @Qualifier(Redis1MainConfig.REDIS_TEMPLATE_NAME) val redisTemplate: RedisTemplate<String, String>
) : BasicRedisMap<Redis1_Map_StompSessionInfo.ValueVo>(redisTemplate, MAP_NAME, ValueVo::class.java) {
    // <멤버 변수 공간>
    companion object {
        // !!!중복되지 않도록, 본 클래스명을 MAP_NAME 으로 설정하기!!!
        const val MAP_NAME = "Redis1_Map_StompSessionInfo"
    }

    // !!!본 RedisMAP 의 Value 클래스 설정!!!
    data class ValueVo(
        // 세션 연결 일시(yyyy_MM_dd_'T'_HH_mm_ss_SSS_z)
        var connectDatetime: String,
        // 서버 고유값
        var serverUuid: String,
        // 세션 principal UserName
        var principalUserName: String,

        // MemberUid (비 로그인시 null)
        var memberUid: Long?
    )
}