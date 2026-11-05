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

1. 프로젝트 루트 경로에 `.env` 파일을 생성.
2. 아래 예시를 참고하여 본인 환경에 맞는 DB 정보를 입력해주세요.

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
CREATE DATABASE cafe;
```

3. 테이블 생성: 콘솔 우상단의 schema에서 cafe 선택 후 SQL 실행
```sql
CREATE TABLE category (
    id    INT    NOT NULL AUTO_INCREMENT,
    name    VARCHAR(30)    NULL,
    PRIMARY KEY (id)
);

CREATE TABLE orders (
    id    int    NOT NULL AUTO_INCREMENT,
    email    varchar(100)    NULL,
    created_at    datetime    NULL,
    status    varchar(16)    NULL,
    address    varchar(200)    NULL,
    postcode    varchar(5)    NULL,
    memo    varchar(200)    NULL,
    shipping_date    datetime    NULL,
    PRIMARY KEY (id)
);

CREATE TABLE menu (
    id    int    NOT NULL AUTO_INCREMENT,
    description    varchar(300)    NULL,
    name    varchar(100)    NULL,
    price    INT    NULL,
    available    boolean    NULL,
    category_id    INT    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE order_items (
    id    int    NOT NULL AUTO_INCREMENT,
    quantity    int    NULL,
    menu_name    varchar(100)    NULL,
    unitPrice    int    NULL,
    menu_id    int    NOT NULL,
    order_id    int    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (menu_id) REFERENCES menu(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

**초기 데이터 등록 절차**
- 작성 예정

**http 경로·변수·실행 순서**
- 작성 예정

**시각 경계값 확인 방법**
- 작성 예정


**기술 스택**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.8.2-02303A?style=for-the-badge&logo=gradle&logoColor=white)


## 진행도


| **번호** | **상태**  | **기능**     | **메서드** | **비고 / 미해결 오류** |
| ------ | ------- | ---------- | ------- | --------------- |
| A01    | `⏳ 대기중` | 카테고리 목록    | GET     | -               |
| A02    | `⏳ 대기중` | 카테고리 상세    | GET     | -               |
| A03    | `⏳ 대기중` | 카테고리 등록    | POST    | -               |
| A04    | `⏳ 대기중` | 카테고리 수정    | PUT     | -               |
| A05    | `⏳ 대기중` | 카테고리 삭제    | DELETE  | -               |
| A06    | `⏳ 대기중` | 전체 메뉴      | GET     | -               |
| A07    | `⏳ 대기중` | 메뉴 상세      | GET     | -               |
| A08    | `⏳ 대기중` | 메뉴 등록      | POST    | -               |
| A09    | `⏳ 대기중` | 메뉴 일반 수정   | PUT     | -               |
| A10    | `⏳ 대기중` | 메뉴 삭제      | DELETE  | -               |
| A11    | `✅ 완료` | 주문 접수      | POST    | -               |
| A12    | `⏳ 대기중` | 이메일별 주문    | GET     | -               |
| A13    | `✅ 완료` | 주문 상세      | GET     | -               |
| A14    | `⏳ 대기중` | 배송 묶음      | GET     | -               |
| A15    | `⏳ 대기중` | 카테고리별 메뉴   | GET     | -               |
| A16    | `✅ 완료` | 주문 취소      | POST    | -               |
| A17    | `⏳ 대기중` | 접수일별 주문 통계 | GET     | -               |
| A18    | `⏳ 대기중` | 접수일별 메뉴 통계 | GET     | -               |
| A19    | `⏳ 대기중` | 판매 중 메뉴    | GET     | -               |
| A20    | `⏳ 대기중` | 메뉴명 검색     | GET     | -               |
| A21    | `⏳ 대기중` | 판매 여부 변경   | PUT     | -               |
| A22    | `✅ 완료` | 주문 메모 수정   | PUT     | -               |
| A23    | `✅ 완료` | 재주문        | POST    | -               |
| A24    | `⏳ 대기중` | 접수일별 고객 통계 | GET     | -               |
- `⏳ 대기중` (아직 시작 안 함)
- `🔄 작업중` (현재 구현 중)
- `✅ 완료` (구현 및 테스트 완료)
