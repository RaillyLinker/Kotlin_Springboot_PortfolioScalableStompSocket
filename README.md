# [포트폴리오 병렬 확장 웹소켓 프로젝트]

## 프로젝트 설명

- 본 프로젝트는 Springboot 를 사용하여 WebSocket 서버를 구축하는 샘플 프로젝트입니다.<br>
  <br>
  WebSocket 통신 구축 핵심 기술 외,<br><br>

  MSA,<br>
  세션 보안<br>
  메시지 브로커 연동,<br>
  서버 복제를 통한 수평적 확장,<br><br>

  등의 응용기술을 추가하여 실용성을 높였으며,<br><br>

  Springboot 를 이용한 WebSocket 서버 개발시 가독성, 유지보수성, 효율성을 보장하는 개발 패턴을 정리하였습니다.<br>

## 프로젝트 설명 상세

### STOMP

저는 본 springboot 프로젝트에서 Socket 모듈을 만들 때,<br>
STOMP 를 사용하였습니다.<br>

#### STOMP(Simple/Stream Text Oriented Message Protocol) 란,

이름 그대로 websocket 위에서 동작하는 텍스트 기반 메세징 프로토콜입니다.<br>

STOMP 의 주요 특징은<br>

1. 메시지를 프레임 단위로 전송하여 HTTP 와 비슷한 구조를 가짐
2. pub/sub 패턴을 가짐<br>

위와 같습니다.<br>

쉽게 설명하기 위하여, websocket 을 그대로 사용할 때와 비교하자면,<br>

연결 - 메시지 전송(반복) - 연결 종료<br>

websocket 은 위와 같은 단순한 프로세스로, 메시지의 종류, 형태, 방식 등을 정하는 것이 자유롭지만,
서비스를 구현하기 위해 동일한 로직이라도 천차만별의 구현 방식이 있을 수 있습니다.<br>

이러한 자유로움으로 인하여 동일한 기능을 개발할 때에도 매번 같은 어려움과 같은 혼란을 겪을 수도 있습니다.<br>

STOMP 의 경우는,<br>
클라이언트-서버 간 보내는 메시지의 형태와 종류를 정형화하였습니다.<br>

STOMP 프레임의 구조는,<br>

```
COMMAND
header1:value1
header2:value2

Body
```

위와 같으며,<br>

앞서 설명과 같이 HTTP 와 유사한 형태로,<br>
이 중 COMMAND 의 종류를 살펴보자면,<br>

CONNECT : 클라이언트가 서버와 연결<br>
CONNECTED : 서버가 클라이언트 연결 승인<br>
SUBSCRIBE : 클라이언트가 특정 주제 구독<br>
UNSUBSCRIBE : 클라이언트가 특정 주제 구독 취소<br>
SEND : 클라이언트가 메시지 발행<br>
MESSAGE : 서버가 구독한 클라이언트에게 메시지를 발행<br>
DISCONNECT : 클라이언트가 연결 종료<br>

위와 같습니다.<br>

예를 들자면,<br>

```
SEND
destination:/topic/chat
content-type:text/plain

안녕하세요!
```

위와 같은 형태로 메시지를 보낸다면 /topic/chat 이라는 위치로 "안녕하세요!" 라는 메시지를 보내는 것으로 해석되는 것입니다.<br>

이처럼 STOMP 는 그 이름답게 간단하게 메시지 기반의 통신을 위해 필요한 기능들을 구현함에 있어서 가장 효율적인 형식을 정해두었으므로,<br>
채팅 서비스와 같은 대표적인 소켓 통신 서비스를 만들 때 유리합니다.<br>

다만, 위와 같이 정해진 규칙을 준수해야하므로 반드시 형식에 맞는 요청을 보내야만 하기에 쓸모없는 오버헤드가 통신 성능에 악영향을 끼칠 수도 있다는 단점이 있습니다.

#### 제가 본 프로젝트에서 STOMP 를 선택한 이유는,

JWM 위에서 돌아가는 Java 기반의 프레임워크인 Springboot 는 최고 성능을 기대하기 위해 사용하는 프레임워크가 아닌, 개발 효율성과 가독성, 유지보수성에 중점을 둔 가치를 지니고 있기에,<br>

마찬가지로 최고 효율을 보장하지는 않지만 가독성과 유지보수성을 높여주는 STOMP 야 말로 Springboot 에서 개발하기 좋은 Socket 프로토콜이라고 생각했으며,<br>

STOMP 의 pub/sub 구조가 Kafka 와 같은 메시지 브로커의 구조와 동일하기에 이와 연동하여 MSA 를 구축하기 좋기에 선택하였습니다.<br>

추가로,<br>
만약 온라인 게임과 같은 서비스에서 좌표 데이터, 실시간 버튼 입력 데이터 등의 비정형의 실시간 정보 통신의 경우는 성능이 최우선이므로 C 언어와 같은 고성능 언어로 별도의 서버 모듈을 만들어 구현을 하는 것이
유리한
결정일 수도 있을테지만,<br>

텍스트 기반의 정보 전달이 주가 되는 채팅과 같은 서비스를 구현할 때에는 성능보다는 개발자 편의가 우선된다고 생각되므로 저는 Springboot + STOMP 기술 스택을 선택할 것입니다.

### Springboot STOMP 개발 규약

springboot 에서 STOMP 를 개발할 때 저는 아래와 같은 규약을 정해두었습니다.<br>

1. 서버별 STOMP 연결 엔드포인트는 /stomp 로 결정하고, 또한 HTML 버전 호환성을 위하여 SockJS 를 사용하였습니다.<br>
   Javascript 에서는,
    ```
    var socket = new SockJS('http://localhost:13001/stomp');
    ```
   이처럼 연결객체를 생성합니다.<br><br>

2. 메시지 발행 prefix 는 /app, 수신 prefix 는 /topic 과 /queue 로 설정하였습니다.<br>
   /topic 은 단체 메시지, /queue 는 개별 메시지를 의미하며,<br>
   이처럼 일반적인 STOMP prefix 규약과 동일하게 설정하였지만,<br>
   userDestination prefix 는 일반적인 /user 가 아닌 /session 으로 변경하였습니다.<br>
   이유는, 개별 메시지를 보낼 때, user 단위가 아닌 소켓의 세션 단위를 사용하기 때문이며,<br>
   서비스에 가입한 한 유저는 여러 위치에서 서버에 접속하여 여러 세션을 만들 수 있기 때문에 이처럼 구분하였습니다.<br><br>

3. MSA 구조를 적용하였습니다.<br>
   이를 위하여 Redis, Kafka 를 사용하여 서버의 유기적 상태값을 외부에 두고,<br>
   메시지 브로커를 통해 통신하게 하였습니다.<br><br>

4. 인증/인가 보안 처리는 JWT 를 적용하였습니다.<br>
   인증/인가 모듈에서 로그인 api 를 통해 발급받은 JWT 를,<br>
    ```
    stompClient.subscribe('/topic/test-channel', function (greeting) {
        showMessage(JSON.parse(greeting.body).content);
    }, {Authorization : "Bearer aa", "client-request-code" : "SUBSCRIBE"});
   ```
   위의 Authorization 과 같이 STOMP Header 에 넣어 보내면,<br>
   서버측에서 이를 받아 메시지별 인증/인가 처리를 하는 것입니다.<br><br>

5. 웹소켓 서버의 에러 처리는 에러 채널을 subscribe 하여 구현하였습니다.<br>
   예를들어 JWT 인증/인가를 할 때, 입력한 토큰이 만료된 경우에는,<br>
   클라이언트가 구독한 /session/queue/request-error 라는 위치로,<br>
   미리 정해둔 에러 코드를 반환하여 JWT 를 다시 발급받도록 유도합니다.<br>
   이리하면 HTTP API 와 같이 Request - Response 구조로 처리가 가능한데,<br>
   WebSocket 은 메시지를 발급하고 응답이 올 때까지 기다리는 것이 아니므로, 응답이 어느 위치에서 온 것인지를 확인해야 하므로,<br>
   클라이언트는 Header 에 client-request-code 라는 키로 메시지 고유 값을 입력하고,<br>
   서버가 응답을 보낼 때에는 이 고유값을 실어 보냄으로써 요청과 응답간 쌍을 맞추는 것입니다.<br><br>

6. 인터셉터 게이트웨이를 정리하였습니다.<br>
   STOMP 로 오는 요청에 대하여 중간에 위치하여 각 시점별 일괄적인 처리를 할 수 있도록 제공되는 콜백이 인터셉터입니다.<br>
   web_socket_stomp_src/StompInterceptor 클래스 안에,<br>
   메시지가 전송되기 전의 콜백인 preSend 를 마련해 두었으며,<br>
   이를 통하여 클라이언트가 CONNECT, SUBSCRIBE, SEND, UNSUBSCRIBE, DISCONNECT 중 하나의 요청을 보냈을 때의 처리를 할 수 있습니다.<br>
   이 중 주로 처리가 되는 것은 SUBSCRIBE 와 SEND 로,<br>
   인증/인가시 자격이 없는 유저가 접근 불가능한 경로를 SUBSCRIBE 하거나, 부적절한 메시지를 SEND 하는 것을 막는 코드를 작성하는 경우가 많기에,<br>
   이와 같은 코드들을 StompGateway 라는 클래스 안에 모아두었습니다.<br><br>

7. STOMP 의 구독 채널별 메시지 Body 의 데이터 스키마는 모두 다를 수 있습니다.<br>
   이에 대한 DTO 는 StompSubVos 클래스 안에 모아두었으므로 구독 채널 경로와 함께 데이터 스키마를 한눈에 볼 수 있습니다.<br><br>

8. 본 프로젝트의 WebSocket 서버의 확장성 처리는 아래와 같이 하였습니다.<br>
   먼저 Socket 세션에 대한 정보는 Springboot 내부에서 자동으로 저장하고 관리합니다.<br>
   즉, 서버별 개별 세션 정보가 메모리에 저장된다는 것이고, 이로인해 서버 복제를 통한 확장을 한다면 각 서버별 세션 정보가 공유되지 않으므로, 서버 외적으로 이에대한 처리가 필요합니다.<br>
   일단 StompInterceptorService 클래스 안에<br>
    ```
   // 본 서버의 Stomp 소켓 세션 정보(key : ${serverUuid}_${sessionId}, value : Redis1_Map_StompSessionInfo.ValueVo)
    val sessionInfoMap: HashMap<String, Redis1_Map_StompSessionInfo.ValueVo> = hashMapOf()
   ```
   위와 같은 변수로 서버 내의 정보를 저장합니다.<br>
   sessionId 는 서버에서 각 세션마다 고유하게 발급하는 정보이고, serverUuid 는,<br>
   ```
   "${System.currentTimeMillis()}/${UUID.randomUUID()}"
   ```
   위와 같은 형태로, 서버가 실행될 때 static 메모리 상에 저장됩니다.<br>
   보시다시피 serverUuid 는 생성일과 랜덤 Uuid 로 고유성이 보장되고,<br>
   이를 sessionId 와 결합하면 서버별 세션에 고유성이 보장됩니다.<br>
   sessionInfoMap 변수의 value 값은,<br>
   ```
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
   ```
   위와 같이 되어 있습니다.<br>
   세션을 구분하는데 필요한 정보로 이루어져 있으며,<br>
   이 중 principalUserName 은 세션 개별 메시지를 보낼 때 사용하는 변수라고 생각하면 됩니다.<br>
   이리하면 서버별 세션 구분을 위해 필요한 정보가 갖춰진 샘인데, 이처럼 서버 개별로 저장되어 있는 데이터를 각 서버들이 공유할 수 있게 공개해야할 것입니다.<br>
   이때 Redis 를 사용합니다.<br>
   Redis 역시 Map 변수와 동일하게 Key, Value 로 이루어져 있으므로, Key 와 Value 를 모두 동일하게 설정하면 되며,<br>
   Redis 데이터의 ExpireDate 를 설정하여 예상치 못한 서버 장애로 인한 접속 종료를 대비할 것입니다.<br>
   Springboot 서버는 Scheduler 를 사용하여 하트비트 시그널과 같이 인터벌마다 Redis 에 본인의 서버 내에 있는 세션 정보를 갱신할 것이고,<br>
   만약 설정한 인터벌을 초과할 때까지 데이터가 갱신되지 않는다면 자동으로 해당 서버 내의 세션 정보들은 사라져서 연결 종료로 취급되는 것입니다.<br>
   마지막으로, 서버간 메시지 발행의 경우는 kafka 를 사용하였습니다.<br>
   메시지를 발행하는 클라이언트의 입장에서는 어느 서버에 어느 세션이 존재하는지 모르기 때문에,<br>
   pub/sub 구조를 이용하여 모든 서버에 메시지를 보내면 되는 것입니다.<br>
   클라이언트 메시지 발행 요청 -> 서버 검증 및 해당 채널에 kafka 메시지 전송 -> 모든 서버가 sub 중이던 kafka consumer 에서 메시지 수령 -> 각 서버들은 내부 연결된 세션으로 메시지 발행<br>
   위와 같은 방식으로 서버간 메시지 발행이 이루어지며,<br>
   kafka 발행시 코드상으로 가장 효율적으로 처리하기 위하여,<br>
   ```
   @Component
   class Kafka1MainProducer(
   @Qualifier(Kafka1MainConfig.PRODUCER_BEAN_NAME) private val kafka1MainProducerTemplate: KafkaTemplate<String, Any>,
   ) {
   // <멤버 변수 공간>
   private val classLogger: Logger = LoggerFactory.getLogger(this::class.java)

    // ---------------------------------------------------------------------------------------------
    // <공개 메소드 공간>
    // (stomp 메시지 발송)
    fun sendMessageToStomp(message: SendMessageToStompInputVo) {
        // stomp 에 토픽 메세지 발행
        kafka1MainProducerTemplate.send("stomp_send-message", Gson().toJson(message))
    }

    data class SendMessageToStompInputVo(
        // principalName : null 이라면 topic 전송, not null 이라면 queue 전송
        val principalName: String?,
        // 전송 주소(ex : /topic/server-heartbeat, /session/queue/test-channel)
        val destination: String,
        // 전송 메시지 Object 직렬화 String
        val messageJsonString: String
    )
   }
   ```
   kafka Producer 의 메시지 발행 함수를 하나,<br>
   ```
       // (Stomp 메시지 전송 이벤트)
    @KafkaListener(
        topics = ["stomp_send-message"],
        containerFactory = Kafka1MainConfig.CONSUMER_BEAN_NAME
    )
    fun stompSendMessage(data: ConsumerRecord<String, String>) {
        val inputVo = Gson().fromJson(
            data.value(),
            StompSendMessageInputVo::class.java
        )

        if (inputVo.principalName == null) {
            simpMessagingTemplate.convertAndSend(
                inputVo.destination,
                inputVo.messageJsonString
            )
        } else {
            simpMessagingTemplate.convertAndSendToUser(
                inputVo.principalName,
                inputVo.destination,
                inputVo.messageJsonString
            )
        }
    }

    data class StompSendMessageInputVo(
        // principalName : null 이라면 topic 전송, not null 이라면 queue 전송
        val principalName: String?,
        // 전송 주소(ex : /topic/server-heartbeat, /session/queue/test-channel)
        val destination: String,
        // 전송 메시지 Object 직렬화 String
        val messageJsonString: String
    )
   ```
   kafka Consumer 함수를 위와 같이 하나씩만 만들어서 사용한다면,<br>
   ```
   kafka1MainProducer.sendMessageToStomp(
       Kafka1MainProducer.SendMessageToStompInputVo(
           null,
           "/topic/test-channel",
           Gson().toJson(StompSubVos.TopicTestChannelVo("from simpMessagingTemplate {inputVo.chat : ${inputVo.chat}}"))
       )
   )
   ```
   위와 같이 깔끔하고 간편하게 전 서버에 대한 소켓 메시지 발행 처리가 가능하게 됩니다.<br><br>

9. 위와 같은 방식으로 STOMP Socket 서버를 깔끔하게 구현이 가능하며,<br>
   WebSocket 서비스를 만들 시에 활용하면 됩니다.<br>
   추가적으로 채팅 서비스를 구현한다고 가정한다면 본 프로젝트에서 몇가지 기능만 구현하면 됩니다.<br>
   예를들어 채팅방과 같은 정보는 실시간성이 필요한 정보가 아니기 때문에 RDBMS 와 같은 데이터베이스에 유저 테이블의 고유값을 외래키로 잡아 만들면 되고,<br>
   채팅 내역의 경우는 빠른 입력 성능이 필요하므로, 본 프로젝트 내에 설정되어 있는 MongoDB 라이브러리를 사용하면 됩니다.<br>
   이외에는 채팅방 입장/퇴장 규칙, 메시지 전송 규칙, 그리고 controller 에 기존 채팅 내역 요청 api, 채팅방 목록 api, 현재 접속 유저 정보 api 등을 구현하면 완성입니다.

## 프로젝트 실행 방법

본 프로젝트의 설명은 이상 마치도록 하겠습니다.<br>
마지막으로, 본 프로젝트의 구동 결과물을 간단히 확인하는 방법을 설명드리겠습니다.<br>

본 프로젝트는 채팅 서비스와 같은 정식 서비스 구현이 목표가 아닌, 웹소켓을 다루는 것이 목표이므로, 간단하게 채팅방 1개에 대한 연결을 가정하는 간단한 HTML 및 Javascript 예시를
준비해두었습니다.<br>
동작 확인을 위해서는 아래와 같이 진행하세요.<br>

1. module-scalable-stomp-socket 모듈의 src/main/kotlin/{package}/ApplicationMain.kt 를 Springboot 로 실행<br>
   <br>
2. 동일 모듈의 external_files/socket_test_html/websocket-stomp.html 열기<br>