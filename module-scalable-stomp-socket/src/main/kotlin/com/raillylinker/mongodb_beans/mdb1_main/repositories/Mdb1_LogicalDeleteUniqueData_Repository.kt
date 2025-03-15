package com.raillylinker.mongodb_beans.mdb1_main.repositories

import com.raillylinker.mongodb_beans.mdb1_main.documents.Mdb1_LogicalDeleteUniqueData
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface Mdb1_LogicalDeleteUniqueData_Repository : MongoRepository<Mdb1_LogicalDeleteUniqueData, String> {
    fun findAllByRowDeleteDateStrOrderByRowCreateDate(
        rowDeleteDateStr: String
    ): List<Mdb1_LogicalDeleteUniqueData>

    fun findByUidAndRowDeleteDateStr(
        uid: String,
        rowDeleteDateStr: String
    ): Mdb1_LogicalDeleteUniqueData?

    fun findAllByRowDeleteDateStrNotOrderByRowCreateDate(
        rowDeleteDateStr: String
    ): List<Mdb1_LogicalDeleteUniqueData>

    fun findByUniqueValueAndRowDeleteDateStr(
        uniqueValue: Int,
        rowDeleteDateStr: String
    ): Mdb1_LogicalDeleteUniqueData?
}