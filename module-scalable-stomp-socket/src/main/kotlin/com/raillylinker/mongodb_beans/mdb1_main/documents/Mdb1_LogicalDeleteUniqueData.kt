package com.raillylinker.mongodb_beans.mdb1_main.documents

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "logical_delete_unique_data")
@CompoundIndexes(
    CompoundIndex(
        name = "unique_value_row_delete_date_str_idx",
        def = "{'unique_value': 1, 'row_delete_date_str': 1}",
        unique = true
    )
)
data class Mdb1_LogicalDeleteUniqueData(
    // 논리적 삭제 유니크 제약 테스트 테이블
    @Field("unique_value")
    var uniqueValue: Int
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