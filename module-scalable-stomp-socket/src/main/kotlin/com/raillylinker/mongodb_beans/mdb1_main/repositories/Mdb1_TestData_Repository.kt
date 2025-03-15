package com.raillylinker.mongodb_beans.mdb1_main.repositories

import com.raillylinker.mongodb_beans.mdb1_main.documents.Mdb1_TestData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface Mdb1_TestData_Repository : MongoRepository<Mdb1_TestData, String> {
    fun countByRowDeleteDateStr(
        rowDeleteDateStr: String
    ): Long

    fun findAllByRowDeleteDateStrOrderByRowCreateDate(
        rowDeleteDateStr: String,
        pageable: Pageable
    ): Page<Mdb1_TestData>

    fun findAllByRowDeleteDateStrOrderByRowCreateDate(
        rowDeleteDateStr: String
    ): List<Mdb1_TestData>

    fun findByUidAndRowDeleteDateStr(
        uid: String,
        rowDeleteDateStr: String
    ): Mdb1_TestData?

    fun findAllByRowDeleteDateStrNotOrderByRowCreateDate(
        rowDeleteDateStr: String
    ): List<Mdb1_TestData>
}