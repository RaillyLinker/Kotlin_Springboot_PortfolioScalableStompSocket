package com.raillylinker.mongodb_beans.mdb1_main.documents

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "test_data")
data class Mdb1_TestData(
    // 테스트 본문
    @Field("content")
    var content: String,

    // 테스트 랜덤 번호
    @Field("random_num")
    var randomNum: Int,

    // 테스트용 일시 데이터
    @Field("test_datetime")
    var testDatetime: LocalDateTime,

    // 테스트용 nullable 데이터
    @Field("nullable_value")
    var nullableValue: String?
) {
    // 행 고유값
    @Id
    var uid: String? = null

    // 행 생성일
    @CreatedDate
    @Field("row_create_date")
    var rowCreateDate: LocalDateTime? = null

    // 행 수정일
    @LastModifiedDate
    @Field("row_update_date")
    var rowUpdateDate: LocalDateTime? = null

    // 행 삭제일(yyyy_MM_dd_T_HH_mm_ss_SSS_z, 삭제되지 않았다면 /)
    @Field("row_delete_date_str")
    var rowDeleteDateStr: String = "/"
}