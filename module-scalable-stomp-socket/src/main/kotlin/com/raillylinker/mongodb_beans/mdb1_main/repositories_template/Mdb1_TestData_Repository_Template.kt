package com.raillylinker.mongodb_beans.mdb1_main.repositories_template

import com.raillylinker.configurations.mongodb_configs.Mdb1MainConfig
import com.raillylinker.mongodb_beans.mdb1_main.documents.Mdb1_TestData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset

@Repository
class Mdb1_TestData_Repository_Template(
    @Qualifier(Mdb1MainConfig.MONGO_DB_DIRECTORY_NAME)
    private val mongoTemplate: MongoTemplate
) {

    fun findAllFromTemplateTestDataByNotDeletedWithRandomNumDistance(num: Int): List<FindAllFromTemplateTestDataByNotDeletedWithRandomNumDistanceOutputVo> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("rowDeleteDateStr").`is`("/")),
            Aggregation.project(
                "content",
                "randomNum",
                "testDatetime",
                "nullableValue",
                "uid",
                "rowCreateDate",
                "rowUpdateDate",
                "rowDeleteDateStr"
            )
                .andExpression("abs(randomNum - $num)").`as`("distance"),
            Aggregation.sort(Sort.by(Sort.Order.asc("distance")))
        )

        return mongoTemplate.aggregate(
            aggregation,
            Mdb1_TestData::class.java,
            FindAllFromTemplateTestDataByNotDeletedWithRandomNumDistanceOutputVo::class.java
        ).mappedResults
    }

    data class FindAllFromTemplateTestDataByNotDeletedWithRandomNumDistanceOutputVo(
        val content: String,
        val randomNum: Int,
        val testDatetime: LocalDateTime,
        val nullableValue: String?,
        val uid: String,
        val rowCreateDate: LocalDateTime,
        val rowUpdateDate: LocalDateTime,
        val rowDeleteDateStr: String,
        val distance: Int // 차이값 추가
    )


    ////
    fun findAllFromTemplateTestDataByNotDeletedWithRowCreateDateDistance(date: LocalDateTime): List<FindAllFromTemplateTestDataByNotDeletedWithRowCreateDateDistanceOutputVo> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("rowDeleteDateStr").`is`("/")), // 삭제되지 않은 데이터 필터링
            Aggregation.project(
                "content",
                "randomNum",
                "testDatetime",
                "nullableValue",
                "uid",
                "rowCreateDate",
                "rowUpdateDate",
                "rowDeleteDateStr"
            )
                .and(
                    DateOperators.dateValue(
                        ArithmeticOperators.Subtract.valueOf("rowCreateDate")
                            .subtract(date.toInstant(ZoneOffset.UTC).toEpochMilli())
                    ).millisecond()
                ).`as`("timeDiffMicroSec"), // rowCreateDate와 입력된 date 간 차이 계산
            Aggregation.sort(Sort.by(Sort.Order.asc("timeDiffMicroSec"))) // 차이값을 기준으로 오름차순 정렬
        )

        return mongoTemplate.aggregate(
            aggregation,
            Mdb1_TestData::class.java,
            FindAllFromTemplateTestDataByNotDeletedWithRowCreateDateDistanceOutputVo::class.java
        ).mappedResults
    }

    data class FindAllFromTemplateTestDataByNotDeletedWithRowCreateDateDistanceOutputVo(
        val content: String,
        val randomNum: Int,
        val testDatetime: LocalDateTime,
        val nullableValue: String?,
        val uid: String,
        val rowCreateDate: LocalDateTime,
        val rowUpdateDate: LocalDateTime,
        val rowDeleteDateStr: String,
        val timeDiffMicroSec: Long // 차이값 추가 (초 단위)
    )


    ////
    fun findFromTemplateTestDataByNotDeletedAndUid(testTableUid: String): FindFromTemplateTestDataByNotDeletedAndUidOutputVo? {
        val criteria = Criteria.where("rowDeleteDateStr").`is`("/") // 삭제되지 않은 데이터 필터링
            .and("uid").`is`(testTableUid.toString()) // uid 필터링

        val query = Aggregation.newAggregation(
            Aggregation.match(criteria),
            Aggregation.project(
                "content",
                "randomNum",
                "testDatetime",
                "nullableValue",
                "uid",
                "rowCreateDate",
                "rowUpdateDate",
                "rowDeleteDateStr"
            )
        )

        return mongoTemplate.aggregate(
            query,
            Mdb1_TestData::class.java,
            FindFromTemplateTestDataByNotDeletedAndUidOutputVo::class.java
        ).mappedResults.firstOrNull()
    }

    data class FindFromTemplateTestDataByNotDeletedAndUidOutputVo(
        val content: String,
        val randomNum: Int,
        val testDatetime: LocalDateTime,
        val nullableValue: String?,
        val uid: String,
        val rowCreateDate: LocalDateTime,
        val rowUpdateDate: LocalDateTime,
        val rowDeleteDateStr: String
    )


    ////
    fun countFromTemplateTestDataByNotDeleted(): CountFromTemplateTestDataByNotDeletedOutputVo? {
        val criteria = Criteria.where("rowDeleteDateStr").`is`("/") // 삭제되지 않은 데이터 필터링

        val aggregation = Aggregation.newAggregation(
            Aggregation.match(criteria),
            Aggregation.count().`as`("count") // 문서 개수 카운트
        )

        val result = mongoTemplate.aggregate(
            aggregation,
            Mdb1_TestData::class.java,
            CountFromTemplateTestDataByNotDeletedOutputVo::class.java
        ).uniqueMappedResult

        return result
    }

    data class CountFromTemplateTestDataByNotDeletedOutputVo(val count: Long)


    ////
    fun findPageAllFromTemplateTestDataByNotDeletedWithRandomNumDistance(
        num: Int,
        pageable: Pageable
    ): Page<FindPageAllFromTemplateTestDataByNotDeletedWithRandomNumDistanceOutputVo> {
        val matchStage = Aggregation.match(Criteria.where("rowDeleteDateStr").`is`("/"))

        val projectStage = Aggregation.project(
            "content",
            "randomNum",
            "testDatetime",
            "nullableValue",
            "uid",
            "rowCreateDate",
            "rowUpdateDate",
            "rowDeleteDateStr"
        ).andExpression("abs(randomNum - $num)").`as`("distance")

        val sortStage = Aggregation.sort(Sort.by(Sort.Order.asc("distance")))

        val skipStage = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())
        val limitStage = Aggregation.limit(pageable.pageSize.toLong())

        val aggregation = Aggregation.newAggregation(
            matchStage,
            projectStage,
            sortStage,
            skipStage,
            limitStage
        )

        val results = mongoTemplate.aggregate(
            aggregation,
            Mdb1_TestData::class.java,
            FindPageAllFromTemplateTestDataByNotDeletedWithRandomNumDistanceOutputVo::class.java
        ).mappedResults

        val totalElements = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(Criteria.where("rowDeleteDateStr").`is`("/")),
                Aggregation.count().`as`("count") // 문서 개수 카운트
            ),
            Mdb1_TestData::class.java,
            CountFromTemplateTestDataByNotDeletedOutputVo::class.java
        ).uniqueMappedResult?.count ?: 0

        return org.springframework.data.domain.PageImpl(results, pageable, totalElements)
    }

    data class FindPageAllFromTemplateTestDataByNotDeletedWithRandomNumDistanceOutputVo(
        val content: String,
        val randomNum: Int,
        val testDatetime: LocalDateTime,
        val nullableValue: String?,
        val uid: String,
        val rowCreateDate: LocalDateTime,
        val rowUpdateDate: LocalDateTime,
        val rowDeleteDateStr: String,
        val distance: Int // 차이값 추가
    )


    ////
    fun findPageAllFromTemplateTestDataBySearchKeyword(
        searchKeyword: String,
        pageable: Pageable
    ): Page<FindPageAllFromTemplateTestDataBySearchKeywordOutputVo> {
        // 검색 키워드로 content 필드를 부분 일치 검색
        val criteria = Criteria.where("rowDeleteDateStr").`is`("/") // 삭제되지 않은 데이터 필터링
            .and("content").regex(".*$searchKeyword.*", "i") // content 필드에서 searchKeyword를 포함하는 문서 검색, 대소문자 구분 안함

        val matchStage = Aggregation.match(criteria)

        val projectStage = Aggregation.project(
            "content",
            "randomNum",
            "testDatetime",
            "nullableValue",
            "uid",
            "rowCreateDate",
            "rowUpdateDate",
            "rowDeleteDateStr"
        )

        val skipStage = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())
        val limitStage = Aggregation.limit(pageable.pageSize.toLong())

        val aggregation = Aggregation.newAggregation(
            matchStage,
            projectStage,
            skipStage,
            limitStage
        )

        // Aggregation 실행
        val results = mongoTemplate.aggregate(
            aggregation,
            Mdb1_TestData::class.java,
            FindPageAllFromTemplateTestDataBySearchKeywordOutputVo::class.java
        ).mappedResults

        // 전체 문서 개수 카운트
        val totalElements = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.count().`as`("count") // 문서 개수 카운트
            ),
            Mdb1_TestData::class.java,
            CountFromTemplateTestDataByNotDeletedOutputVo::class.java
        ).uniqueMappedResult?.count ?: 0

        return org.springframework.data.domain.PageImpl(results, pageable, totalElements)
    }

    data class FindPageAllFromTemplateTestDataBySearchKeywordOutputVo(
        val content: String,
        val randomNum: Int,
        val testDatetime: LocalDateTime,
        val nullableValue: String?,
        val uid: String,
        val rowCreateDate: LocalDateTime,
        val rowUpdateDate: LocalDateTime,
        val rowDeleteDateStr: String
    )
}