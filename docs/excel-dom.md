# XML DOM 엑셀 다운로드

`src/main/resources/excel/event_detail_excel.xml`은 원래 빈 파일이었다. 새 구조는
`EventDetail.jsp`의 ExcelJS 다운로드 레이아웃을 기준으로 정의했다.

## 호출

```java
Map<String, Object> data = new HashMap<>();
data.put("event", eventVO);
data.put("statusName", "진행중");
byte[] bytes = excelFileWriter.writeXml(data,
        "classpath:excel/event_detail_excel.xml", new DefaultExcelStyle());
excelFileWriter.downloadXml(response, "이벤트_상세.xlsx", data,
        "excel/event_detail_excel.xml", null);
```

- GET `/event/file/template/dom.do`: 값이 비어 있는 XML 양식 다운로드.
- GET `/event/file/detail/dom.do?eid=1`: DB의 이벤트와 상태 이름을 조회해 다운로드.
- 상세 JSP의 `엑셀 DOM 다운로드` 버튼에서 두 번째 경로를 호출한다.
- 기존 `write(List, ExcelTemplate[, ExcelStyle])`, `writeTemplate`과 브라우저 ExcelJS 다운로드는 유지한다.
- `/event/file/template/sax.do`는 기존 시험용 자리이며 이번 DOM 구현 범위에 포함하지 않았다.

## XML 구조 v1

루트는 `<excel version="1">`, 그 아래 한 개 이상의 `<sheet name="...">`을 둔다.
시트 자식은 순서대로 처리한다.

```xml
<excel version="1">
  <sheet name="상세">
    <column index="1" width="20"/>
    <cell range="A1" text="제목"/>
    <cell range="B1:E1" property="title" align="center"/>
    <repeat items="files">
      <cell range="A2:E2" property="name" height="24"/>
    </repeat>
    <cell range="A2:E2" text="끝"/>
  </sheet>
</excel>
```

- `column.index`는 1부터 시작하며 `width` 단위는 글자 수다. 생략 시 `ExcelStyle.getColumnWidth`를 적용한다.
- `cell.range`는 A1 형식 단일 셀 또는 병합 범위다. 범위 전체에 테두리와 스타일을 적용한다.
- `text`(고정 문자열), `property`(Map 키 또는 JavaBean 속성 경로) 중 하나를 지정한다.
- `style`은 `header`, `body`를 지원한다. 기본값은 text이면 header, property이면 body다.
- `format`은 Excel 표시 형식, `align`은 POI 수평 정렬 값, `height`는 행 높이(pt, 1~409)다.
- 숫자·논리값·Date·LocalDate·LocalDateTime을 원래 셀 형식으로 기록한다. 문자열은 수식으로 실행하지 않는다.
- `repeat`는 Iterable의 항목마다 내부 셀을 한 행씩 이동시켜 출력한다. 내부 셀 범위는 같은 행에 있어야 한다.
  뒤에 나오는 셀도 항목 수만큼 이동하므로 반복 시작 행에 적은 후속 셀이 마지막 행 뒤로 밀린다.
- null 루트 데이터는 빈 양식으로 처리한다. null 중간 값은 빈 셀이다. 존재하지 않는 Map 키/Bean 속성은 오류로 보고한다.
- classpath 스트림으로 읽으므로 배포 WAR에서 동작하며 로컬 절대 파일 경로에 의존하지 않는다.
  XML DTD/외부 엔티티는 허용하지 않는다.

DOM 방식은 첨부 **파일명**을 반복 행으로 출력한다. 기존 브라우저 ExcelJS 방식의 첨부 이미지 삽입은 기존 버튼에서 제공한다.
첨부가 없어도 XML의 첨부파일 제목 행은 유지한다. 내용 행은 줄바꿈이 가능한 120pt 높이로 출력한다.

## 검증

`mvn -o test`로 컴파일한 뒤 다음 독립 검증을 실행한다(Windows, Maven 테스트 의존성 classpath 필요).

```text
java -cp "target/test-classes;target/classes;<테스트 의존성 classpath>" com.cmm.service.excel.ExcelDomWriterCheck
```

검증은 빈 양식, Bean/Map 매핑, 첨부 반복/후속 행 이동, 날짜/숫자/논리 셀, 사용자 스타일,
병합 테두리, 기존 API 호환성, 다운로드 헤더/본문 및 잘못된 XML·외부 엔티티 차단을 확인한다.

실행 결과: Maven 오프라인 test 및 package 빌드 성공. 독립 검증 37개 항목 통과.
실제 DB·웹서버를 사용한 브라우저 다운로드는 별도 확인이 필요하다.
