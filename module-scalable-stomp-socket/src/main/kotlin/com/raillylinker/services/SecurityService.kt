package com.raillylinker.services

import com.raillylinker.configurations.SecurityConfig.AuthTokenFilterTotalAuth
import com.raillylinker.configurations.jpa_configs.Db1MainConfig
import com.raillylinker.jpa_beans.db1_main.repositories.Db1_RaillyLinkerCompany_TotalAuthMember_Repository
import com.raillylinker.util_components.JwtTokenUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SecurityService(
    // (프로젝트 실행시 사용 설정한 프로필명 (ex : dev8080, prod80, local8080, 설정 안하면 default 반환))
    @Value("\${spring.profiles.active:default}") private var activeProfile: String,
    private val authTokenFilterTotalAuth: AuthTokenFilterTotalAuth,

    private val jwtTokenUtil: JwtTokenUtil,
    private val db1RaillyLinkerCompanyTotalAuthMemberRepository: Db1_RaillyLinkerCompany_TotalAuthMember_Repository
) {
    // <멤버 변수 공간>
    private val classLogger: Logger = LoggerFactory.getLogger(this::class.java)

    // (현 프로젝트 동작 서버의 외부 접속 주소)
    // 프로필 이미지 로컬 저장 및 다운로드 주소 지정을 위해 필요
    // !!!프로필별 접속 주소 설정하기!!
    // ex : http://127.0.0.1:8080
    private val externalAccessAddress: String
        get() {
            return when (activeProfile) {
                "prod80" -> {
                    "http://127.0.0.1"
                }

                "dev8080" -> {
                    "http://127.0.0.1:8080"
                }

                else -> {
                    "http://127.0.0.1:8080"
                }
            }
        }


    // ---------------------------------------------------------------------------------------------
    // <공개 메소드 공간>
    // (비 로그인 접속 테스트)
    fun noLoggedInAccessTest(httpServletResponse: HttpServletResponse): String? {
        httpServletResponse.status = HttpStatus.OK.value()
        return externalAccessAddress
    }


    // ----
    // (로그인 진입 테스트 <>)
    @Transactional(transactionManager = Db1MainConfig.TRANSACTION_NAME, readOnly = true)
    fun loggedInAccessTest(httpServletResponse: HttpServletResponse, authorization: String): String? {
        val memberUid = jwtTokenUtil.getMemberUid(
            authorization.split(" ")[1].trim(),
            authTokenFilterTotalAuth.authJwtClaimsAes256InitializationVector,
            authTokenFilterTotalAuth.authJwtClaimsAes256EncryptionKey
        )

        // 멤버 데이터 조회
        val memberEntity =
            db1RaillyLinkerCompanyTotalAuthMemberRepository.findByUidAndRowDeleteDateStr(memberUid, "/")!!
        classLogger.info("Member Id : ${memberEntity.accountId}")

        httpServletResponse.status = HttpStatus.OK.value()
        return "Member No.$memberUid : Test Success"
    }


    // ----
    // (ADMIN 권한 진입 테스트 <'ADMIN'>)
    @Transactional(transactionManager = Db1MainConfig.TRANSACTION_NAME, readOnly = true)
    fun adminAccessTest(httpServletResponse: HttpServletResponse, authorization: String): String? {
        val memberUid = jwtTokenUtil.getMemberUid(
            authorization.split(" ")[1].trim(),
            authTokenFilterTotalAuth.authJwtClaimsAes256InitializationVector,
            authTokenFilterTotalAuth.authJwtClaimsAes256EncryptionKey
        )

        // 멤버 데이터 조회
        val memberEntity =
            db1RaillyLinkerCompanyTotalAuthMemberRepository.findByUidAndRowDeleteDateStr(memberUid, "/")!!
        classLogger.info("Member Id : ${memberEntity.accountId}")

        httpServletResponse.status = HttpStatus.OK.value()
        return "Member No.$memberUid : Test Success"
    }


    // ----
    // (Developer 권한 진입 테스트 <'ADMIN' or 'Developer'>)
    @Transactional(transactionManager = Db1MainConfig.TRANSACTION_NAME, readOnly = true)
    fun developerAccessTest(httpServletResponse: HttpServletResponse, authorization: String): String? {
        val memberUid = jwtTokenUtil.getMemberUid(
            authorization.split(" ")[1].trim(),
            authTokenFilterTotalAuth.authJwtClaimsAes256InitializationVector,
            authTokenFilterTotalAuth.authJwtClaimsAes256EncryptionKey
        )

        // 멤버 데이터 조회
        val memberEntity =
            db1RaillyLinkerCompanyTotalAuthMemberRepository.findByUidAndRowDeleteDateStr(memberUid, "/")!!
        classLogger.info("Member Id : ${memberEntity.accountId}")

        httpServletResponse.status = HttpStatus.OK.value()
        return "Member No.$memberUid : Test Success"
    }


    // ----
    // (로그인 / 비로그인 진입 테스트 <>?)
    fun optionalLoggedInAccessTest(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        authorization: String?
    ): String? {
        val notLoggedIn = authTokenFilterTotalAuth.checkRequestAuthorization(httpServletRequest) == null

        val memberEntity =
            if (notLoggedIn) {
                null
            } else {
                val memberUid = jwtTokenUtil.getMemberUid(
                    authorization!!.split(" ")[1].trim(),
                    authTokenFilterTotalAuth.authJwtClaimsAes256InitializationVector,
                    authTokenFilterTotalAuth.authJwtClaimsAes256EncryptionKey
                )
                db1RaillyLinkerCompanyTotalAuthMemberRepository.findByUidAndRowDeleteDateStr(memberUid, "/")!!
            }

        classLogger.info("Member Id : ${memberEntity?.accountId}")

        httpServletResponse.status = HttpStatus.OK.value()
        return "Member No.${memberEntity?.uid} : Test Success"
    }
}