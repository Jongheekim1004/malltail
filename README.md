![image](https://user-images.githubusercontent.com/117339609/209891871-f1556010-1727-4df7-b8eb-1651e1be3707.png)

# 몰테일 - 해외직구시스템

# Table of contents

- [해외직구시스템](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [Saga(Pub/Sub) 의 적용](#saga-적용)
    - [CQRS 의 적용](#cqrs-적용)
    - [Correlation 의 적용](#ddd-의-적용)     
    - [Request/Response 의 적용](#ddd-의-적용)         
    - [Polyglot persistence / programming](#폴리글랏-퍼시스턴스-및-프로그래밍-(Persistence Volume))
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)  
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [Deploy](#Deploy)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리 / 모니터링 ](#동기식-호출--서킷-브레이킹--장애격리--모니터링) 
    - [오토스케일 아웃](#오토스케일-아웃)
    - [셀프힐링 / 무정지 재배포](#무정지-재배포)

# 서비스 시나리오

기능적 요구사항
1. 고객이 상품을 선택하여 주문한다
2. 주문된 상품이 결제된다
3. 결제가 되면 주문 내역이 상점주인에게 전달된다
4. 상점주인이 확인하여 상품을 배송가능여부에 따라 주문을 수락 또는 거절한다
5. 주문이 수락되면, 상품이 등록한 배대지로 출발한다 (해외배송)
6. 고객이 주문을 취소할 수 있다
8. 주문한 상품이 이미 배대지 배송이 시작된 경우에는 취소할 수 없다
9. 주문이 취소되면 결제가 취소되고, 상점주인이 취소된 상품내역을 확인할수 있다
10. 배대지에 상품 도착 시 한국 주소로 출발한다
11. 통관담당자는 상품의 통관을 승인하거나 거절할 수 있다
12. 통관 절차가 승인되면 국내 배송이 진행된다 (국내배송)
13. 통관 절차가 거절되면 고객과실로 판단되어 상품은 폐기되며, 주문내역이 취소된다
14. 고객이 주문상태를 중간 중간 조회할 수 있다

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 주문건은 아예 거래가 성립되지 않아야 한다 - Sync 호출 
1. 장애격리
    1. 상점관리 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다 - Async (event-driven), Eventual Consistency
    1. 해외배송, 국내배송 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다 - Async (event-driven), Eventual Consistency
    1. 주문요청이 과도하여, 결제 처리가 지연되는 경우 결제를 잠시후에 하도록 유도한다  - Circuit breaker, fallback
1. 성능
    1. 고객이 주문한 상품의 주문/배송/결제 정보에 대한 상태를 조회시스템(프론트엔드)에서 확인할 수 있어야 한다 - CQRS


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 4개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅


# 분석/설계

## AS-IS 조직 (Horizontally-Aligned)
![image](https://user-images.githubusercontent.com/117131393/209925974-cfb15977-c2b6-4613-bc5a-654ccc3c08e7.png)


## TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/117131393/209926207-82ea4be3-6cb2-49a5-9ec6-5e1d0ff6a914.png)



## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  https://labs.msaez.io/#/storming/9193c1c4a611b2f6d8832acb7d0af300


### 이벤트 도출
![image](https://user-images.githubusercontent.com/117247400/209891427-8d014278-9d36-4dbd-b41c-271c351a2d93.JPG)

    - 각 팀원이 서비스 시나리오에 맞추어 이벤트를 도출한다
    - Naming Rule을 준수하여 이벤트명을 변경한다 (영문표기, 대문자로 시작)
        
### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/117131393/209905369-40d3c678-c964-4c94-b7e6-9971f8815e26.png)

    - 과정 중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행한다
        1. 상품을 선택(ProductSeleted)하는 이벤트는 오더 처리에 포함되므로 제외
        2. 명칭은 다르지만 기능이 중복된 이벤트를 제외 (deliverytoDomastic가 DeliveryStarted와 중복됨)
        3. view로 구현되어야 하는 기능 제외 (kakaoNotification, Notified)

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/117131393/209906519-2eaca7f1-a0a7-4e00-82e0-54038c3a1e8b.JPG)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/117131393/209906610-0c425aa3-bd65-4156-b047-b22d13fe0e4d.JPG)

    - Order, Shop의 주문 및 배송처리, 결제, 해외배송(shipping), 국내배송(delivery)으로 역할을 나눈다 
      이 다섯가지 범위 내에서 각각의 command와 event들이 트랜잭션을 유지해야 한다

### 바운디드 컨텍스트로 묶기

![image](https://user-images.githubusercontent.com/117131393/209908342-9b45f146-51e8-4f56-a87f-38eac6c84cfc.JPG)

    - 도메인 서열 분리 
       1. Core Domain - order, shop  
          핵심 서비스이며, Up-time SLA 수준을 99.999% 목표, 배포주기는 1주일 1회 미만
       2. Supporting Domain - shipping, delivery
          경쟁력을 내기 위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 1주일 1회 이상을 기준으로 함
       3. General Domain - pay
          결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음
    - View Model 추가
        - 고객이 주문상태를 중간 중간 조회하기 위한 페이지는 view 모델로 생성한다

### 폴리시 부착

![image](https://user-images.githubusercontent.com/117131393/209895674-be5e3413-480d-4b29-adcb-8efcb5f1c18e.JPG)

    - 일부 command를 policy로 변경한다
       - shop에서 orderSeliveryStart를 진행하면 shipping이 시작되므로 start shipping은 policy로 변경 
       - cancel payment는 앞선 order처리에서 orderCanceled 시 후속되는 과정이므로 policy로 변경
    - 신규로 policy를 추가한다
       - 결제가 완료된 주문 건은 orderInfoUpdate라는 policy로 구현 후 shop에 추가
       - 통관절차에서 승인된 상품 리스트는 add to customs list라는 policy로 구현 후 delivery에 추가
 
### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![image](https://user-images.githubusercontent.com/117131393/209895686-71c4645f-10a8-41e6-8a93-461df4addc07.JPG)

    - Req/Resp
       - 주문을 할 경우(Order) Pay를 요청하고, Pay 여부에 대한 응답 결과를 요구한다
    - Pub/Sub
       - 주문이 취소되면(OrderCanceled) 결제요청도 취소된다(canceled payment)
       - 결제가 완료되면(OrderPaid) Shop에 주문된 상품을 업데이트한다(orderinfoUpdate)
       - 상점 주인이 상품 주문을 진행하면(OrderDeliveryStarted) 해외배송이 시작된다(start shipping)
       - 해외배송이 완료되면(ShippingCompleted) 통관절차 리스트에 추가된다(add to customs list)
       
       
### 완성된 모형

![image](https://user-images.githubusercontent.com/117131393/209906848-f28a57bf-decc-4f84-b812-1a848c6d06a4.JPG)

    - Attribute를 추가한다
       - 변수를 전달하는 주체와 타겟을 고려하여 Attribute를 선언하며 Event에 Sync를 맞춤

### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/117247400/209891413-1af06231-f9a6-4bb5-bbfc-43b06516ef8c.JPG)

    - 고객이 상품을 선택하여 주문한다 (ok)
    - 고객이 결제한다 (ok)
    - 주문이 되면 주문 내역이 상점주인에게 전달된다 (ok)
    - 상점주인이 확인하여 해외배송을 시작한다 (ok)
    - 해외배송이 완료되면 통관리스트에 상품이 추가된다 (ok)
    - 세무사가 통관을 승인하면 국내배송을 시작한다 (ok)
    - 국내배송이 완료된다 (ok)

![image](https://user-images.githubusercontent.com/117247400/209891438-feb3025c-1a34-4eaa-8787-e4922b07ab03.jpg)

    - 고객이 주문을 취소할 수 있다 (ok) 
    - 주문이 취소되면 결제가 취소된다 (ok) 
    - 고객이 주문상태를 중간중간 조회한다 (View-green sticker 의 추가로 ok) 


### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/117247400/209891418-7e09bd47-78ba-4cea-a029-0d8546c7c97d.jpg)
       
    - 마이크로서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
       ①   : 고객 주문시 결제처리 
             결제가 완료되지 않은 주문은 주문처리를 시작하지 않는다는 사칙에 따라 ACID 트랜잭션 적용
             주문 시 결제처리에 대해서는 Request-Response 방식을 수행함
       ②,③ : Kafka를 이용하여 각 마이크로서비스 간에 Pub-Sub 모델로 서비스 연동
       ④   : shipping 처리 지연시 주문취소를 잠시 후에 하도록 유도하기 위해 Circuit breaker-fallback 모델 사용
       ⑤   : 주문 상태 조회 
             주문 상태 및 배송상태 조회는 타 마이크로서비스 원본 접근 없이 CQRS-Materialized View로 상시 조회가능 하도록 함 


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8086 이다)

```
cd order
mvn spring-boot:run

cd pay
mvn spring-boot:run 

cd shop
mvn spring-boot:run  

cd shipping
mvn spring-boot:run

cd delivery
mvn spring-boot:run

cd viewPage
mvn spring-boot:run

```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. 도메인 기반으로 비즈니스 목적별로 분류하여 서비스들을 구성하도록 하였다. (예시는 order 마이크로 서비스) 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 최대한 어플리케이션 또는 그 안의 모듈간의 의존성은 최소화하고, 
  응집성은 최대화하도록 설계하였다.

```
@Entity
@Table(name="Order_table")
@Data

public class Order  {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    
    private Long id;
    
    private String customerId;
    
    private Long itemNo;
    
    private Long qty;
    
    private Date createDate;
    
    private String orderStatus;
    
    private String deliveryStatus;
    
    private String customerAddress;
    
    private String customerName;
    
    private String phoneNumber;
    
    private Date updateDate;
}

```
- 적용 후 REST API 의 테스트
```
# order 서비스의 주문처리
http localhost:8081/orders itemNo=1001

# order 서비스의 취소처리
http PUT :8081/orders/1/cancel

# 주문 상태 확인
http localhost:8081/orders/1

# View Page 확인
http localhost:8086/statusViews

```

## CQRS

고객(Customer)의 주문 상태/배송/결제 정보에 대한 Status를 조회할 수 있도록 CQRS로 구현하였다.
- order, payment, shop, delivery, shipping 개별 Aggregate Status를 통합 조회하여 성능 Issue를 사전에 예방할 수 있다. 
- 비동기식으로 처리되어 발생된 이벤트 기반 Kafka를 통해 수신 처리되어 별도 Table에 관리한다.
- Table 모델링(statusView)

![image](https://user-images.githubusercontent.com/117247400/209908194-78527ae4-a404-4217-9d9a-54a594ed99fc.png)

- viewPage를 통해 구현 (Order, Shipping, Delivery 등 이벤트 발생 시, Pub/Sub 기반으로 별도 statusView 테이블에 저장)
- 실제로 view 페이지를 조회해 보면 모든 order에 대한 전반적인 아이템번호, 주문상태 등의 정보를 종합적으로 알 수 있다.

![image](https://user-images.githubusercontent.com/13111333/209935374-2aaabe7a-0929-4b6e-90c2-fe141a493d2c.png)


## Correlation

고객이 주문취소(cancel) 요청을 했을 경우, 결제 완료된 요청건에 대해 주문상태가 취소로 변경되었음을 확인하고 결제제 취소 처리를 한다. 만약 이미 배대지배송(shipping)이 진행중인 주문건이면, 주문취소(cancel)을 할 수 없도록 동기식 구현을 적용하였다.

- 주문이 취소되는 시점에 ShippingService를 조회하여 그 조건에 따라 분기처리하였다. 주문취소가 정상적으로 동작하는 경우에는 주문상태(orderStatus)를 "Canceled"로 변경한다.
```
    public void cancel(){

        // 이미 shipping이 된 이후에는 취소할 수 없다.
        try{
            
            malltail.external.Shipping shipping =
            OrderApplication.applicationContext.getBean(malltail.external.ShippingService.class)
            .getShipping(getId());

            throw new RuntimeException("Cannot cancel!");
        } catch (Exception e){
            // shipping이 존재하지 않는 경우 
            
            // Order와 delivery 상태를 변경한다. 
            setOrderStatus("Canceled");
        }

        OrderCanceled orderCanceled = new OrderCanceled(this);
        orderCanceled.publishAfterCommit();
    }
```

- 결제제 (pay)의 PolicyHandler에 주문이 취소될때의 호출되는 함수 wheneverOrderCanceled_CancelPayment를 구현한다.
```
@Service
@Transactional
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

    @StreamListener(value=KafkaProcessor.INPUT, condition="headers['type']=='OrderCanceled'")
    public void wheneverOrderCanceled_CancelPayment(@Payload OrderCanceled orderCanceled){

        OrderCanceled event = orderCanceled;
        System.out.println("\n\n##### listener CancelPayment : " + orderCanceled + "\n\n");   

        // Sample Logic //
        Payment.cancelPayment(event);
    }

}

```

- 주문취소(cancel) 시 결제(pay) 정보를 업데이트 한다. 
```
    public static void cancelPayment(OrderCanceled orderCanceled){

        repository().findById(orderCanceled.getId()).ifPresent(payment->{
            
            payment.setPaystatus("Refunded");
            repository().save(payment);

         });
        
    }
```

## 폴리글랏 퍼시스턴스 및 프로그래밍 (Persistence Volume)

- 현재 구현중인 몰테일은 실사용을 위한 사이트로 오픈하기 전에 데모버전의 테스트성 사이트로 그에 적합한 인메모리 DB인 H2를 사용하여 필요한 DB의 기능을 활용하였고,
- 상점(shop) 서비스의 경우에는 상품의 재고량 파악 및 정보 영구저장을 위해 Persistence Volume인 mySql DB를 적용하였다.
- 몰테일은 order, payment, shop, delivery, shipping 별 독립적인 서비스를 java로 구현하고 배포하였다.
- 서비스 간 처리되는 order-shop, shop-shipping, shipping-delivery 이벤트는 Kafka를 이용하여 비동기식으로 동작한다.

- 상점(shop) 서비스에 mysql 사용을 위한 라이브러리 추가 및 data 설정
```
<pom.xml>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>	
```
```
<application.yml>
  jpa:
    hibernate:
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
        format_sql: true
        dialect: org.hibernate.dialect.MySQL57Dialect
  datasource:
    url: jdbc:mysql://${_DATASOURCE_ADDRESS:10.100.49.58:3306}/${_DATASOURCE_TABLESPACE:shop_management}
    username: ${_DATASOURCE_USERNAME:root}
    password: ${_DATASOURCE_PASSWORD:mysql123}
    driverClassName: com.mysql.cj.jdbc.Driver
```

- mySql 서비스와 Deployment 생성하였고, 접속 비밀번호는 보안이 중요한 데이터이므로 Secret으로 설정하여 사용하였다.

![image](https://user-images.githubusercontent.com/13111333/210038488-fff57444-e5d0-45bf-a293-ae97d741c571.png)

![image](https://user-images.githubusercontent.com/13111333/210038935-dc540c33-701d-4347-bc5e-2ebbc213c573.png)

```
apiVersion: v1
kind: Pod
metadata:
  name: mysql
  labels:
    name: lbl-k8s-mysql
spec:
  containers:
  - name: mysql
    image: mysql:latest
    env:
    - name: MYSQL_ROOT_PASSWORD
      valueFrom:
        secretKeyRef:
          name: mysql-pass
          key: password
    ports:
    - name: mysql
      containerPort: 3306
      protocol: TCP
    volumeMounts:
    - name: k8s-mysql-storage
      mountPath: /var/lib/mysql
  volumes:
  - name: k8s-mysql-storage
    emptyDir: {}
```

- 주문완료 후 상점에 생성된 데이터 조회
```
kubectl exec mysql -it -- bash

mysql> select id, orderNo, deliveryStatus from ShopManagement_table;
+----+---------+----------------+
| id | orderNo | deliveryStatus |
+----+---------+----------------+
|  1 |       1 | Ready          |
|  2 |       2 | Ready          |
|  3 |       3 | Ready          |
|  4 |       4 | Ready          |
|  5 |       5 | Ready          |
|  6 |      11 | Ready          |
|  7 |       9 | Ready          |
|  8 |       7 | Ready          |
|  9 |      15 | Ready          |
| 10 |      14 | Ready          |
+----+---------+----------------+
10 rows in set (0.00 sec)
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 주문(order)와 제제(pay) 간의 호출은 동기식 일관성을 유지하는 트랙잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현
```
@FeignClient(name = "pay", url = "${api.url.pay}")
public interface PaymentService {
    @RequestMapping(method = RequestMethod.POST, path = "/payments")
    public void pay(@RequestBody Payment payment);
}
```

- 주문이 완료되는 시점에(@onPostPersist), 필요한 값을 설정하여 결제제를 처리한다.
```
    @PostPersist
    public void onPostPersist(){

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        malltail.external.Payment payment = new malltail.external.Payment();
        payment.setOrderNo(getId());
        payment.setItemNo(getItemNo());
        payment.setPaystatus("Paid");

        // mappings goes here
        OrderApplication.applicationContext
            .getBean(malltail.external.PaymentService.class)
            .pay(payment);

        Ordered ordered = new Ordered(this);
        ordered.publishAfterCommit();
    }

```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인하였다.


1. 결제 (pay) 서비스를 잠시 중지한 경우 : Fail

![image](https://user-images.githubusercontent.com/13111333/209922103-a527c390-f5b1-47de-9a9a-9884856253b9.png)


2. 결제 (pay) 서비스를 실행한 경우 : Success

- 주문내역 (order) 생성됨

![image](https://user-images.githubusercontent.com/13111333/209922597-941e8518-e62a-4bac-9389-396e656ef4c8.png)

- 결제 (pay) 생성됨

![image](https://user-images.githubusercontent.com/13111333/209922652-539e4f0b-9584-46fc-9432-f79b3e104d69.png)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

주문(order)과 결제(pay)가 모두 완료되면, 해외직구를 대행하는 상점(shop)인 몰테일에 이를 알려주는 행위를 비동기식으로 처리하여, 
상점(shop) 시스템의 처리를 위하여 주문과 결제가 블로킹되지 않도록 처리한다.

- 결제제가 완료되면, 주문이 정상적으로 처리되었다는 이벤트를 Kafka로 송출하고 (Publish),
  이를 상점(shop)의 PolicyHandler를 통해 수신하도록 (Subscribe) 구현하였다.
```
    @StreamListener(value=KafkaProcessor.INPUT, condition="headers['type']=='OrderPaid'")
    public void wheneverOrderPaid_OrderInfoUpdate(@Payload OrderPaid orderPaid){

        OrderPaid event = orderPaid;
        System.out.println("\n\n##### listener OrderInfoUpdate : " + orderPaid + "\n\n");

        // Sample Logic //
        ShopManagement.orderInfoUpdate(event);
        
    }
```

- 상점의 도에인 영역의 구현부에서(ShopManagement) 상점의 정보를 생성하고, 배송상태(deliveryStatus)를 "Ready"상태로 반영하였다.
```
    public static void orderInfoUpdate(OrderPaid orderPaid){

        ShopManagement shopManagement = new ShopManagement();
        shopManagement.setOrderNo(orderPaid.getOrderNo());
        shopManagement.setItemNo(orderPaid.getItemNo());
        shopManagement.setDeliveryStatus("Ready");

        repository().save(shopManagement);        
    }
```

- 이와같이 구현한 비동기식 호출에서는 상점(shop) 시스템에 장애나 지연이 발생하여도, 주문/결제와 완전히 분리되어있으므로 정상적으로 동작한다.

- 상점(shop) 서비스를 잠시 중지 후 신규 주문(order) 요청 시, 주문(order)과 결제제(pay) 정상수행됨
http :8081/orders itemNo=2001

![image](https://user-images.githubusercontent.com/13111333/209934079-122ccbfc-e0f0-4482-9ef9-092fb1e0713e.png)

![image](https://user-images.githubusercontent.com/13111333/209934154-2b07cda7-37ae-4f52-9c7f-98ee86365b19.png)

- 상점(shop) 서비스를 재기동한 후 확인

![image](https://user-images.githubusercontent.com/13111333/209934747-4e9dc2d4-d255-449b-a709-665b4e2eda50.png)


# 운영

## Deploy


각 서비스들은 별개의 source repository 에 구성되었고, CI/CD 방식이 아닌 서비스들을 개별적으로 반영하는 방식으로 구현하였다.

1. 마이크로 서비스별 이미지 docker image build 및 push

```
docker build -t rktmaudtn/order:v# . 
docker pysh rktmaudtn/order:v#
```

2. aws eks 서비스 내 deploy 및 service 생성

```
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```

```
##############################
deployment.yaml 예시
##############################

apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  labels:
    app: order
spec:
  replicas: 1
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: rktmaudtn/order:v2
          ports:
            - containerPort: 8080

```

3. SVC 정보

```
NAME                          TYPE           CLUSTER-IP       EXTERNAL-IP                                                               PORT(S)                      AGE
gateway                       LoadBalancer   10.100.54.179    a3a5c914c8909446fa3847d0198dc212-1810891788.eu-west-3.elb.amazonaws.com   8080:31521/TCP               155m
kubernetes                    ClusterIP      10.100.0.1       <none>                                                                    443/TCP                      178m
my-kafka                      ClusterIP      10.100.1.49      <none>                                                                    9092/TCP                     153m
my-kafka-headless             ClusterIP      None             <none>                                                                    9092/TCP,9093/TCP            153m
my-kafka-zookeeper            ClusterIP      10.100.54.55     <none>                                                                    2181/TCP,2888/TCP,3888/TCP   153m
my-kafka-zookeeper-headless   ClusterIP      None             <none>                                                                    2181/TCP,2888/TCP,3888/TCP   153m
mysql                         ClusterIP      10.100.49.58     <none>                                                                    3306/TCP                     97m
order                         ClusterIP      10.100.179.173   <none>                                                                    8080/TCP                     85m
pay                           ClusterIP      10.100.90.93     <none>                                                                    8080/TCP                     115s
shipping                      ClusterIP      10.100.27.206    <none>                                                                    8080/TCP                     84m
shop                          ClusterIP      10.100.144.162   <none>                                                                    8080/TCP                     76m
viewpage                      ClusterIP      10.100.70.172    <none>                                                                    8080/TCP                     84m
```


## 동기식 호출 / 서킷 브레이킹 / 장애격리 / 모니터링

- Istio를 설치하여 주문 요청이 과도할 경우 Envoy 사이드카를 생성하는 pod들에 자동적으로 주입하여 서킷 브레이킹 기능이 동작하도록 구현하였다. 그런데 부하를 가했을 때 크게 효과가 있음을 체감하지 못했다. 결재 서비스쪽에 서킷브레이커를 설정하여 장애가 격리 됨을 확인하였다. 
- 시나리오는 주문요청완료되면 동기화되어 결재로 연결(RESTful Request/Response 방식)되어 처리되도록 구현되어 있고, 주문 요청이 과도할 경우 결재서비스의 CB를 통하여 장애격리.
- 통합 모니터링 툴을 이용하여 서비스들의 이상 현상들을 모니터링하도록 함 ( kiali /prometheus/ grafana )

- [istio 설치]

![image](https://user-images.githubusercontent.com/117247400/210026883-4658b4a8-3f23-4f59-a849-dab95dbf98a8.png)

- [통합모니터링툴 설치]
 
![image](https://user-images.githubusercontent.com/117247400/210026926-4b5d20dd-8ac5-454e-8858-20935d340b4b.png)

- [istio-injection 상태 enable 확인] 

 명령어 : $ kubectl label namespace default istio-injection=enabled

![image](https://user-images.githubusercontent.com/117247400/210027287-d756b5f1-8d45-49ba-a0b9-768e9f025d5d.png)



- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml

feign:
  hystrix:
    enabled: true
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 500


```

- 피호출 서비스(결제:pay) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
```
# Payment.java (Entity)

    @PrePersist
    public void onPrePersist(){  //결제이력을 저장한 후 적당한 시간 끌기

        ...
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
$ siege -c10 -t40S -r10 --content-type "application/json" 'http://order:8080/orders POST {"itemNo": "1001"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     3.80 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.00 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.86 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.88 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.01 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.02 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.03 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.93 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.12 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.57 secs:     475 bytes ==> POST http://order:8080/orders

* 요청이 과도하여 CB를 동작함 요청을 차단

HTTP/1.1 500     1.29 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     1.23 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     1.42 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     2.08 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     1.29 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://order:8080/orders

* 요청을 어느정도 돌려보내고나니, 기존에 밀린 일들이 처리되었고, 회로를 닫아 요청을 다시 받기 시작

HTTP/1.1 201     4.08 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.80 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.09 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.94 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.96 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     3.98 secs:     475 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.02 secs:     475 bytes ==> POST http://order:8080/orders

* 다시 요청이 쌓이기 시작하여 건당 처리시간이 610 밀리를 살짝 넘기기 시작 => 회로 열기 => 요청 실패처리

HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://order:8080/orders  
HTTP/1.1 500     1.92 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://order:8080/orders

* 생각보다 빨리 상태 호전됨 - (건당 (쓰레드당) 처리시간이 610 밀리 미만으로 회복) => 요청 수락

HTTP/1.1 201     2.24 secs:     207 bytes ==> POST http://order:8080/orders  
HTTP/1.1 201     2.32 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.16 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.21 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.29 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.30 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.38 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.59 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.61 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.62 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     2.64 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.01 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.27 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.33 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.45 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.52 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.57 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.70 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://order:8080/orders

* 이후 이러한 패턴이 계속 반복되면서 시스템은 도미노 현상이나 자원 소모의 폭주 없이 잘 운영됨


HTTP/1.1 500     4.76 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     4.23 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.76 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.74 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 500     4.82 secs:     248 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.82 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.84 secs:     207 bytes ==> POST http://order:8080/orders
HTTP/1.1 201     4.66 secs:     207 bytes ==> POST http://order:8080/orders


:
:

Transactions:		        1025 hits
Availability:		       63.55 %
Elapsed time:		       59.78 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
Successful transactions:        1025
Failed transactions:	         588
Longest transaction:	        9.20
Shortest transaction:	        0.00

```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 63.55% 가 성공하였고, 46%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.


- 모니터링 툴을 이용하여 서비스들의 상태 추적 (kiali)

http://af831838952b14d99a6a49f7d91a4034-1469197019.eu-west-3.elb.amazonaws.com:20001/kiali/console/graph/namespaces/?traffic=grpc%2CgrpcRequest%2Chttp%2ChttpRequest%2Ctcp%2CtcpSent&graphType=versionedApp&duration=1800&refresh=900000&namespaces=istio-system%2Cdefault&idleNodes=true&layout=kiali-dagre&namespaceLayout=kiali-dagre&graphHide=healthy&edges=trafficDistribution%2Cthroughput%2CthroughputRequest%2CresponseTime%2Crt95&idleEdges=true&operationNodes=true&rank=true

![image](https://user-images.githubusercontent.com/117247400/210039885-2decd909-af04-41cf-a411-fbfa9059a3f9.png)


- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 5개까지 늘려준다:
![image](https://user-images.githubusercontent.com/117131393/210037953-2fd23b2f-dd37-464c-a649-f10163e85d0a.png)

- CB 에서 했던 방식대로 POST방식으로 부하를 걸어준다.
![image](https://user-images.githubusercontent.com/117131393/210037833-9d7465ab-0e88-47a0-931f-83409a444985.png)

- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
![image](https://user-images.githubusercontent.com/117131393/210038209-7937a74f-1ae9-4f88-bd3e-542f27c2bbf3.png)

- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
![image](https://user-images.githubusercontent.com/117131393/210038141-cfae076c-9556-4a8c-af61-67b3a6de9909.png)


## 셀프힐링

order 서비스의 deployment.yaml 파일에 아래와 같이 livenessProbe 설정 한다.

         livenessProbe:
            httpGet:
              path: '/actuator/health123'   <<- 실제 없는 값으로 설정
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5

```
gitpod /workspace/malltail/order/kubernetes (feature) $ kubectl get pod
NAME                        READY   STATUS             RESTARTS   AGE
gateway-7f95bf4c7-bpx9f     1/1     Running            0          155m
liveness-exec               1/2     CrashLoopBackOff   17         56m
my-kafka-0                  1/1     Running            1          152m
my-kafka-client             1/1     Running            0          152m
my-kafka-zookeeper-0        1/1     Running            0          152m
mysql                       1/1     Running            0          118m
order-58d7ddd647-ptfjg      2/2     Running            1          4m35s   <<- restart 된것을 확인함

```
## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.

root@siege:/# siege -c100 -t60S -v http://order:8080/orders --delay=1S

- 새버전으로의 배포 시작
```
root@siege:/# 
root@siege:/# siege -c100 -t60S -v http://order:8080/orders --delay=1S
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Lifting the server siege...
Transactions:                  11632 hits
Availability:                 88.00 %
Elapsed time:                  59.25 secs
Data transferred:               3.27 MB
Response time:                  0.01 secs
Transaction rate:             198.85 trans/sec
Throughput:                     0.06 MB/sec
Concurrency:                    1.32
Successful transactions:       11782
Failed transactions:               0
Longest transaction:            0.29
Shortest transaction:           0.00

```
배포기간중 Availability 가 평소 100%에서 80% 대로 떨어지는 것을 확인.
원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

```
# deployment.yaml 의 readiness probe 의 설정:

          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5

kubectl apply -f kubernetes/deployment.yaml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:

```
Lifting the server siege...
Transactions:                  11782 hits
Availability:                 100.00 %
Elapsed time:                  59.25 secs
Data transferred:               3.27 MB
Response time:                  0.01 secs
Transaction rate:             198.85 trans/sec
Throughput:                     0.06 MB/sec
Concurrency:                    1.32
Successful transactions:       11782
Failed transactions:               0
Longest transaction:            0.29
Shortest transaction:           0.00

```


배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.



