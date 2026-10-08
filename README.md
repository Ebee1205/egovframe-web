<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

<!-- [![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![project_license][license-shield]][license-url] -->



<!-- PROJECT LOGO -->
# eGovFrame Web

전자정부표준프레임워크(eGovFrame)를 기반으로 개발한 **지도 기반 장소·이벤트 관리 웹 서비스**입니다.

관리자(Admin)와 사용자(Client) 화면을 분리하여 장소, 이벤트, 사용자 및 공통코드 데이터를 관리하며, PostgreSQL/PostGIS를 활용한 공간정보 저장 및 지도 시각화 기능을 구현하는 프로젝트입니다.


<!-- 프로젝트 소개 -->
## 프로젝트 개요

### 개발 목적

- 전자정부표준프레임워크의 웹 애플리케이션 구조 및 개발 방식 학습
- Spring MVC, MyBatis 기반 CRUD 기능 구현
- PostgreSQL 및 PostGIS를 활용한 공간정보 데이터 관리
- 관리자 CMS와 사용자 서비스 화면의 분리
- 파일 업로드·다운로드 및 엑셀 데이터 처리 기능 구현

### 개발 환경

| 구분 | 기술 |
| --- | --- |
| Framework | ![eGovFrame 4.3.x](https://img.shields.io/badge/eGovFrame-4.3.x-2C7A3F?style=for-the-badge) ![Spring MVC](https://img.shields.io/badge/Spring%20MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| Language | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black) ![SQL](https://img.shields.io/badge/SQL-4479A1?style=for-the-badge) |
| View | ![JSP](https://img.shields.io/badge/JSP-6DB33F?style=for-the-badge) ![JSTL](https://img.shields.io/badge/JSTL-6DB33F?style=for-the-badge) |
| UI | ![CoreUI](https://img.shields.io/badge/CoreUI-321FDB?style=for-the-badge&logo=coreui&logoColor=white) ![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white) |
| Persistence | ![MyBatis](https://img.shields.io/badge/MyBatis-000000?style=for-the-badge) |
| Database | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) |
| Spatial Database | ![PostGIS](https://img.shields.io/badge/PostGIS-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) |
| WAS | ![Apache Tomcat](https://img.shields.io/badge/Apache%20Tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black) |
| Build | ![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white) |
| Version Control | ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white) |


<!-- ## 🎯 주요기능
![DoQ 이미지 설명](./DoQ-info1.png)

<p align="center">
  <a href="https://youtu.be/JgfBLpiZuZk?si=QO_9Vj0EXpAxNcJD&t=45">
    <!-- <img src="https://img.youtube.com/vi/CwZ7ngRwPFY/maxresdefault.jpg" alt="DoQ 시연 영상 썸네일" width="720" /> -->
  </a>
  <br />
  <a href="https://youtu.be/JgfBLpiZuZk?si=QO_9Vj0EXpAxNcJD&t=45"><strong>시연 영상 바로가기</strong></a>
</p>

### 서비스 주요 기능

✅  **중개형 AI 기반의 실시간 합의 도출 및 초안 설계**  
- 보수, 마감 기한 등 양측의 의견이 대립하는 지점에서 AI가 시장 평균 데이터와 프로젝트 성격을 고려한 중재안을 제안하여 합의를 유도한다. 
-  과업, 대금, 기한 등 계약에 필수적인 구성요소를 파악하고, 누락된 정보에 대해 질문하여 완결성 있는 계약 초안의 기틀을 마련한다. 

✅ **일상어 입력으로 전문 계약 조항 작성 기능**  
- “돈은 이달 말까지 지급하겠습니다.”와 같은 일상적인 합의 내용을 “제N조(대금 지급 방식): ‘갑’은 ‘을’에게 계약 금액을 202X년 X월 X일까지 지급하여야 한다”와 같은 표준 법률 문구로 치환한다.

✅ **계약서 저장 및 PDF 내보내기 기능**  
- 진행된 모든 계약 세션과 생성된 문서 이력을 저장하여, PDF 포맷으로 생성 및 출력이 가능한 형태로 제공한다.

<p align="right">(<a href="#readme-top">back to top</a>)</p> -->


## 🚀 프로젝트 시작하기

**1. 프로젝트 클론**

```bash
git clone <repository-url>
cd egovframe-web
```

**2. PostgreSQL 데이터베이스 생성**

```sql
CREATE DATABASE egovframe;
```

**3. PostGIS 확장 설치**

생성한 데이터베이스에 접속한 뒤 실행합니다.

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

**4. 데이터베이스 초기화**

프로젝트의 DDL 및 초기 데이터 SQL을 실행합니다.

```text
DDL 실행
    ↓
공통코드 및 지역 데이터 등록
    ↓
기본 사용자 및 이벤트 데이터 등록
```

**5. 데이터베이스 연결 설정**

프로젝트 내 DB 연결 설정 파일에서 다음 항목을 실제 환경에 맞게 수정합니다.

```properties
# Example
db.url=jdbc:postgresql://localhost:5432/egovframe
db.username=your_username
db.password=your_password
```

위 속성명은 예시이며, 실제 프로젝트의 DataSource 설정 방식을 따라야 합니다.

**6. 서버 실행**

Maven 의존성을 설치하고 전자정부프레임워크 개발 환경에서 Tomcat 서버를 실행합니다.

```bash
mvn clean package
```

프로젝트의 실제 WAR 배포 설정 및 Context Path에 따라 접속 주소가 달라질 수 있습니다.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->