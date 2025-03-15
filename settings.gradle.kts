plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "scalable_stomp"

// 확장 가능 소켓 서버 (13001)
include("module-scalable-stomp-socket")