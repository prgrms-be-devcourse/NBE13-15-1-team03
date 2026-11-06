# NBE13-15-1-team03
백엔드 13기 15회차 team03 의 첫번째 팀 프로젝트 입니다.


🔨 **팀원 별 담당작업**

정예나

    - API A01~A05, A15
    - 초기 데이터 구성
    - category 도메인

변광준

    - API A06~A10, A19~A21
    - menu 도메인

표한결

    - API A11, A13, A16, A22, A23
    - order 도메인

허우솔

    - API A12, A14, A17, A18, A24
    - 초기 인프라 구성 및 설정

**⚙️ 데이터베이스 설정 방법**

1. src/main/resources/application.yaml이 .env 값을 읽습니다
2. 프로젝트 루트 경로에 `.env` 파일을 생성.
3. 아래 예시를 참고하여 본인 환경에 맞는 DB 정보를 입력해주세요.

```
DB_HOST: jdbc:mysql://127.0.0.1:3306/user_database_name?allowPublicKeyRetrieval=true&sslMode=DISABLED  
DB_USERNAME: username
DB_PASSWORD: user_password
```

**새 DB 준비**

1. 접속 확인 (kdt-mysql 컨테이너가 있을 경우)
   - IntelliJ 오른쪽 사이드바에서 데이터베이스 창 열기
   - 창 상단에 데이터 소스에서 MySQL 선택
   - 연결 정보 입력: 호스트 localhost / 포트 3306 / 사용자 DB_USERNAME / 비밀번호 DB_PASSWORD
   - 하단에 "누락된 드라이버 파일 다운로드"가 뜨면 눌러서 설치
   - "연결 테스트" 눌러서 성공이 뜨면 확인

   <br>
   
2. DB 만들기: SQL 콘솔에서 다음 실행

```sql
DROP DATABASE IF EXISTS cafe;
CREATE DATABASE cafe;
```

3. 테이블 생성: 콘솔 우상단의 schema에서 cafe 선택 후 SQL 실행
```sql
use cafe;

CREATE TABLE IF NOT EXISTS category
(
   id   INT AUTO_INCREMENT NOT NULL,
   name VARCHAR(30) COLLATE utf8mb4_bin  NOT NULL,
   CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS menu
(
   id            INT AUTO_INCREMENT NOT NULL,
   category_id   INT                 NOT NULL,
   `description` VARCHAR(300)       NOT NULL,
   name          VARCHAR(100)  COLLATE utf8mb4_bin NOT NULL,
   price         INT                NOT NULL,
   available     BIT(1)             NOT NULL,
   CONSTRAINT pk_menu PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS orders
(
   id            BIGINT AUTO_INCREMENT NOT NULL,
   email         VARCHAR(100) COLLATE utf8mb4_bin  NOT NULL,
   created_at    datetime(6)           NOT NULL,
   status        VARCHAR(16)           NOT NULL,
   address       VARCHAR(200)          NOT NULL,
   postcode      VARCHAR(5)            NOT NULL,
   memo          VARCHAR(200)          NULL,
   shipping_date date                  NOT NULL,
   cancelled_at  datetime(6)           NULL,
   CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS order_items
(
   id         BIGINT AUTO_INCREMENT NOT NULL,
   quantity   INT                   NOT NULL,
   menu_name  VARCHAR(100)          NOT NULL,
   unit_price INT                   NOT NULL,
   order_id   BIGINT                NOT NULL,
   menu_id    INT                   NOT NULL,
   CONSTRAINT pk_order_items PRIMARY KEY (id)
);

ALTER TABLE category
   ADD CONSTRAINT uc_category_name UNIQUE (name);

ALTER TABLE menu
   ADD CONSTRAINT FK_MENU_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE order_items
   ADD CONSTRAINT FK_ORDER_ITEMS_ON_MENU FOREIGN KEY (menu_id) REFERENCES menu (id);

ALTER TABLE order_items
   ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);
```

**초기 데이터 등록 절차**

초기 데이터는 앱이 시작될 때 `DataInitializer`(`src/main/java/com/kdt/cafe/config/DataInitializer.java`)가 자동으로 등록합니다. SQL을 따로 실행할 필요는 없습니다.

1. 위 "새 DB 준비"대로 DB와 테이블을 만듭니다.
2. 백엔드를 실행합니다. 실행이 끝나면 아래 데이터가 등록되어 있습니다.

| 구분 | 데이터 |
| --- | --- |
| 카테고리 | 싱글오리진, 블렌드 |
| 메뉴 (모두 싱글오리진, `available=true`) | Columbia Nariñó 5000원 / Brazil Serra Do Caparaó 7000원 / Columbia Quindío (White Wine Extended Fermentation) 9000원 / Ethiopia Sidamo 11000원 |

3. 확인: `GET http://127.0.0.1:8080/api/v1/categories`, `GET http://127.0.0.1:8080/api/v1/menus`

- 앱을 다시 실행해도 중복 등록되지 않습니다. 같은 이름의 카테고리·메뉴가 이미 있으면 건너뜁니다.
- ID는 DB가 생성합니다. 빈 DB에서 처음 실행하면 카테고리 1·2, 메뉴 1~4입니다.

**실행 방법**
백엔드: 활성 프로필 local로 CafeApplication 실행, 주소 http://127.0.0.1:8080
web: web 폴더에서 java -m jdk.httpserver -b 127.0.0.1 -p 8000, http://127.0.0.1:8000 접속, 연결 설정 → 실제 API 연결 → http://127.0.0.1:8080

**http 경로·변수·실행 순서**

- 경로: `requests/api-check.http` (IntelliJ HTTP Client로 실행)
- 구성: 필수 API 정상 요청 24개(A01~A24) + 오류 요청 6개(400 2개 / 404 1개 / 409 3개)

변수 (파일 상단에서 설정)

| 변수 | 기본값 | 의미 | 값을 넣는 시점 |
| --- | --- | --- | --- |
| `@baseUrl` | `http://127.0.0.1:8080` | API 서버 주소 | - |
| `@seedCategoryId` | 1 | 초기 카테고리 "싱글오리진" ID | A01 응답 확인 |
| `@seedMenuId1`, `@seedMenuId2` | 1, 2 | 초기 메뉴 Columbia Nariñó, Brazil Serra Do Caparaó ID | A06 응답 확인 |
| `@categoryId` | 3 | 새로 등록한 카테고리 ID | A03 실행 후 |
| `@menuId` | 5 | 새로 등록한 메뉴 ID | A08 실행 후 |
| `@orderId` | 1 | 새 주문 ID | A11 실행 후 |
| `@shippingDate` | - | 새 주문의 배송일 (`shippingDate`) | A11 실행 후 |
| `@orderDate` | - | 새 주문의 접수일 (`createdAt`의 날짜 부분) | A11 실행 후 |
| `@reorderId` | 2 | 재주문으로 생긴 주문 ID | A23 실행 후 |
| `@email` | `buyer@example.com` | 주문 이메일 | - |

실행 순서 (파일 위에서부터 차례로 실행)

1. 카테고리: A01 → A02 → A03 (→ `@categoryId`) → A04
2. 메뉴: A06 → A08 (→ `@menuId`) → A07 → A09 → A15 → A19 → A20 → A21 (판매 중지)
3. 주문: A11 (→ `@orderId`, `@shippingDate`, `@orderDate`) → A12 → A13 → A22
4. 배송 묶음·통계: A14 → A17 → A18 → A24
5. 재주문·취소: A23 (→ `@reorderId`) → A16
6. 오류 요청 6개: 400 이메일 형식 / 400 날짜 범위 / 404 없는 주문 / 409 `ADDRESS_MISMATCH` / 409 `MENU_UNAVAILABLE` / 409 `MENU_IN_USE`
7. 삭제: A10 → A05

- `→` 표시가 있는 요청은 실행 후 응답 값을 해당 변수에 직접 넣고 다음 요청으로 넘어갑니다. 변수를 고치지 않으면 다른 주문·메뉴에 요청이 적용됩니다.
- 배송일은 서울 시각 14:00 기준으로 정해지므로, A11부터 409 `ADDRESS_MISMATCH` 요청까지는 14:00을 사이에 두지 않고 이어서 실행합니다.

**시각 경계값 확인 방법**

실제 PC·서버 시계를 바꾸지 않고, 테스트로 확인합니다. 배송일 계산과 취소 마감 로직은 `Order` 엔티티에 있고 시각을 매개변수로 받기 때문에, 테스트에서 경계 시각을 직접 넣어 실제 로직을 실행합니다. 서비스는 현재 시각을 `LocalDateTime.now(Order.ZONE)`(서울)로 한 번 구해 초 단위로 자른 뒤 같은 메서드에 넘깁니다.

| 규칙 | 메서드 | 확인하는 경계값 | 테스트 |
| --- | --- | --- | --- |
| 배송일 계산 | `Order.calculateShippingDate(접수 시각)` | 13:59:59 → 당일 / 14:00:00·14:00:01 → 다음 날 / 12-31 14:00:00 → 다음 해 01-01 | `entity/order/OrderTest` |
| 취소 마감 | `order.cancel(현재 시각)` | 배송일 13:59:59 → 성공 / 14:00:00 → `ORDER_NOT_CANCELLABLE` | `entity/order/OrderTest` |
| 재취소 | `order.cancel(현재 시각)` 두 번 | 마감 이후 재취소도 성공, 최초 `cancelledAt`·주문 값 유지 | `entity/order/OrderTest` |

실행 명령

```bash
# 시각 경계값·재취소 테스트만 실행
./gradlew test --tests "com.kdt.cafe.entity.order.OrderTest"

# 전체 테스트 실행
./gradlew test
```


**기술 스택**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.8.2-02303A?style=for-the-badge&logo=gradle&logoColor=white)


## 진행도


| **번호** | **상태**  | **기능**     | **메서드** | **비고 / 미해결 오류** |
| ------ | ------- | ---------- | ------- | --------------- |
| A01    | `✅ 완료` | 카테고리 목록    | GET     | -               |
| A02    | `✅ 완료` | 카테고리 상세    | GET     | -               |
| A03    | `✅ 완료` | 카테고리 등록    | POST    | -               |
| A04    | `✅ 완료` | 카테고리 수정    | PUT     | -               |
| A05    | `✅ 완료` | 카테고리 삭제    | DELETE  | -               |
| A06    | `✅ 완료` | 전체 메뉴      | GET     | -               |
| A07    | `✅ 완료` | 메뉴 상세      | GET     | -               |
| A08    | `✅ 완료` | 메뉴 등록      | POST    | -               |
| A09    | `✅ 완료` | 메뉴 일반 수정   | PUT     | -               |
| A10    | `✅ 완료` | 메뉴 삭제      | DELETE  | -               |
| A11    | `✅ 완료` | 주문 접수      | POST    | -               |
| A12    | `✅ 완료` | 이메일별 주문    | GET     | -               |
| A13    | `✅ 완료` | 주문 상세      | GET     | -               |
| A14    | `✅ 완료` | 배송 묶음      | GET     | -               |
| A15    | `✅ 완료` | 카테고리별 메뉴   | GET     | -               |
| A16    | `✅ 완료` | 주문 취소      | POST    | -               |
| A17    | `✅ 완료` | 접수일별 주문 통계 | GET     | -               |
| A18    | `✅ 완료` | 접수일별 메뉴 통계 | GET     | -               |
| A19    | `✅ 완료` | 판매 중 메뉴    | GET     | -               |
| A20    | `✅ 완료` | 메뉴명 검색     | GET     | -               |
| A21    | `✅ 완료` | 판매 여부 변경   | PUT     | -               |
| A22    | `✅ 완료` | 주문 메모 수정   | PUT     | -               |
| A23    | `✅ 완료` | 재주문        | POST    | -               |
| A24    | `✅ 완료` | 접수일별 고객 통계 | GET     | -               |
- `⏳ 대기중` (아직 시작 안 함)
- `🔄 작업중` (현재 구현 중)
- `✅ 완료` (구현 및 테스트 완료)
- (비고의 - 는 미해결 오류 없음)


**평가 대상**
- 브랜치: `main`
- 커밋: (최종 제출 커밋 해시)

**선택 심화 코드·검증 방법**

| 담당 | 심화 | 코드 위치 | 검증 방법 |
| --- | --- | --- | --- |
| 정예나 | 업무 규칙 자동 테스트 | (작성 예정) | (작성 예정) |
| 변광준 | 별도 메뉴 페이징 API | (작성 예정) | (작성 예정) |
| 표한결 | Redis 메뉴 조회 캐시 | `config/CacheConfig.java`<br>`service/MenuService.java`<br>(`findAvailable` 캐시, `create`·`update`·`delete`·`changeAvailability` 무효화) | `redis-cli`에서 `GET`·`TTL availableMenus::all`로 키·값·남은 만료 시간 확인<br>SQL 로그로 캐시 유효 중 DB 재조회 없음 확인<br>등록·수정·삭제·판매 상태 변경 후 키 삭제 및 첫 조회 반영 확인<br>[상세 설명](#redis-메뉴-조회-캐시) |
| 허우솔 | 실제 DB 저장·롤백 테스트 | (작성 예정) | (작성 예정) |