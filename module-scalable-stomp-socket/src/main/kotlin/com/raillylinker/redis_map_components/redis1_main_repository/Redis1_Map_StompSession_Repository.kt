package com.raillylinker.redis_map_components.redis1_main_repository

import com.google.gson.Gson
import com.raillylinker.abstract_classes.BasicRedisMap
import com.raillylinker.redis_map_components.redis1_main.Redis1_Map_StompSessionInfo
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

// [RedisMap 컴포넌트]
@Component
class Redis1_Map_StompSession_Repository(
    // !!!RedisConfig 종류 변경!!!
    redisObject: Redis1_Map_StompSessionInfo
) {
    // <멤버 변수 공간>
    // gson 객체
    private val gson = Gson()

    // redisTemplate 객체
    val redisTemplate = redisObject.redisTemplate

    // "$mapName:${key}" // 실제 저장되는 키 = 그룹명:키
    val mapName = Redis1_Map_StompSessionInfo.MAP_NAME


    //// ---------------------------------------------------------------------------------------------------------------
    // <Repository 함수 공간>
    // (memberUid 에 속하는 리스트 반환)
    fun findAllByMemberUid(memberUid: Long?): List<BasicRedisMap.RedisMapDataVo<Redis1_Map_StompSessionInfo.ValueVo>> {
        val resultList = mutableListOf<BasicRedisMap.RedisMapDataVo<Redis1_Map_StompSessionInfo.ValueVo>>()

        val scanOptions = ScanOptions.scanOptions().match("$mapName:*").build()
        val cursor = redisTemplate.scan(scanOptions)

        cursor.use {
            while (it.hasNext()) {
                val innerKey = it.next()

                // 키에서 mapName 제거하여 외부에서 사용할 key 추출
                val key = innerKey.removePrefix("$mapName:")

                // Redis Storage 에 실제로 저장되는 Value (Json String 형식)
                val innerValue = redisTemplate.opsForValue()[innerKey] ?: continue

                // JSON → 객체 변환 (Jackson 사용 가능)
                val valueObject = gson.fromJson(innerValue, Redis1_Map_StompSessionInfo.ValueVo::class.java)

                // content 값이 "test"로 시작하는 경우만 추가
                if (valueObject.memberUid == memberUid) {
                    resultList.add(
                        BasicRedisMap.RedisMapDataVo(
                            key,
                            valueObject,
                            redisTemplate.getExpire(innerKey, TimeUnit.MILLISECONDS) ?: -1L // null 방지
                        )
                    )
                }
            }
        }

        return resultList
    }
}