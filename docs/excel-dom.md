# 공통 XML 양식을 사용하는 엑셀 입출력

```text
classpath:excel/*_Excel.xml
            |
     ExcelTemplate.load()
            |
      불변 Definition
         /       \
ExcelFileWriter   ExcelFileParser (DOM/SAX 구현)
       |                      |
    XLSX 생성            업로드 XLSX/XLS → 중첩 Map
       |
 FileMngUtil.download()
```

XML 파싱과 매핑 검증은 `ExcelTemplate`만 담당한다.
`ExcelDomFileWriter`는 해석된 양식으로 셀을 출력하며 XML DOM 객체를 사용하지 않는다.
Writer/Parser는 생성 시 같은 Template 컴포넌트를 주입받는다. 이미 읽은 Definition 하나를
양쪽에 전달하면 XML을 다시 읽지 않고 동일한 규칙을 공유할 수 있다.

## 사용

```java
ExcelTemplate.Definition template = excelTemplate.load(
        "classpath:excel/EventDetail_Excel.xml");

Map<String, Object> data = new HashMap<>();
Map<String, Object> event = new HashMap<>();
event.put("title", eventVO.getTitle());
event.put("regName", eventVO.getRegName());
event.put("address", eventVO.getAddress());
event.put("SDate", eventVO.getSDate());
event.put("EDate", eventVO.getEDate());
event.put("dsc", eventVO.getDsc());
data.put("event", event);
event.put("statusName", "진행중");
event.put("categoryName", "창작마켓");

byte[] bytes = excelFileWriter.writeMapped(data, template, excelStyle);
Map<String, Object> parsed = excelFileParser.parseMapped(upload, template);
```

경로를 받는 기존 `writeXml`/`downloadXml`도 내부에서 ExcelTemplate을 사용한다.
단일 업로드는 `excelFileParser.parseXml(upload, classpathXml)`로도 처리할 수 있다.

## 업무 호출

- GET `/event/file/template/download.do`: `D:/document/files/template/event_detail_template.xlsx`를
  변경 없이 `이벤트_등록_양식.xlsx`로 내려준다. 상세 엑셀 생성은 별도로 공통 XML을 사용한다.
- POST `/event/file/template/upload.do`: 같은 XML로 업로드 양식의 시트명·고정 문구·병합 범위를 확인하고 속성을 읽는다.
  기존 `fileContent.sheets` 미리보기를 유지하며 `fileContent.data`에 매핑 결과를 추가한다.
- GET `/event/file/detail/dom.do?eid=1`: 저장된 이벤트를 같은 양식으로 내려준다.
- 기존 범용 `parse(files, description)`은 XML 검증 없이 셀 미리보기만 반환한다.
- POST `/event/file/template/upload/sax.do`: 같은 XML 양식을 SAX 방식으로 읽는다(.xlsx 전용).

## 매핑 결과

`property="event.title"`은 `{event: {title: ...}}`로 반환한다.
`repeat items="event.attachedFiles"`는 `{event: {attachedFiles: [{orignlFileNm: ...}, ...]}}`가 된다.
숫자는 Double, 논리값은 Boolean, 날짜 형식 숫자 셀은 Date, 빈 값은 null이다.
수식은 실행하지 않고 파일에 저장된 계산 결과만 읽는다. 결과를 EventVO에 저장하거나
코드 이름을 코드 값으로 바꾸는 일은 업무 서비스의 책임이다.

## XML v1

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

- `column.index`는 1부터 시작한다. `width`는 글자 수이며 생략하면 ExcelStyle의 열 너비를 사용한다.
- `cell.range`는 단일 셀 또는 병합 범위이다. `text`와 `property` 중 하나를 지정한다.
- `style`: header/body, `format`: Excel 표시 형식, `align`: 수평 정렬, `height`: 행 높이(pt).
- repeat 내부 셀은 동일한 한 행이어야 한다. 항목마다 한 행씩 증가하며 후속 셀도 항목 수만큼 이동한다.
- 역매핑 시 repeat 뒤의 고정 text와 병합 범위를 찾아 개수를 결정한다.
  고정 text가 없거나 경계가 여러 곳이면 추측하지 않고 오류를 반환한다.
- XML 해석 시 잘못된 범위·스타일·중복 셀 등을 검증하며 DTD와 외부 엔티티를 차단한다.
- 첨부파일은 파일명만 매핑한다. 첨부 이미지 삽입은 기존 브라우저 ExcelJS 다운로드를 사용한다.

## 검증

`mvn -o test`로 컴파일한 뒤 별도 회귀 검증을 실행한다.

```powershell
$repo = Join-Path $env:USERPROFILE '.m2/repository'
$cp = "target/test-classes;target/classes;target/egovframe-web-event-1.0.0/WEB-INF/lib/*;" +
      "$repo/org/springframework/spring-test/5.3.37/spring-test-5.3.37.jar;" +
      "$repo/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar"
java -cp $cp com.cmm.service.excel.ExcelDomWriterCheck
```

의존성 폴더가 없으면 먼저 `mvn -o package`를 실행한다. 회귀 검증은 동일 Definition의
생성/역매핑/재생성, 빈 반복·첨부 이름과 경계 문구 충돌, 자료형·스타일,
잘못된 XML/양식, 컨트롤러 입출력을 확인한다. 실제 DB 및 브라우저 다운로드는 별도 확인 대상이다.

## 브라우저 다운로드와 업로드 오류

상세 화면의 기존 `엑셀 다운로드`도 `/event/file/detail/dom.do`로 생성한 엑셀을
ExcelJS로 읽은 후 첨부 이미지만 추가한다. 셀·병합·반복 행을 JSP에 별도로 정의하지 않는다.
첨부파일 영역이 XML에 정의된 경우에만 해당 파일을 처리한다. 현재 샘플 양식은 8행으로 첨부파일 영역이 없다.
이전 브라우저 양식은 카테고리 행이 없고 빈 첨부 영역을 생략했으므로 새 양식과 다르다.
과거에 받은 파일은 수정된 버튼으로 다시 내려받은 뒤 업로드해야 한다.

양식 불일치는 HTTP 400 / EXCEL_PARSE_FAILED JSON으로 반환한다.
메시지 리소스가 누락되어도 기본 메시지로 응답하며, 원래 파싱 실패 원인은
각 파서의 WARN 로그와 예외 cause에 보존한다.

실제 ExcelJS 4.4.0을 사용한 브라우저 함수 검증:

```text
java -cp <테스트 classpath> com.cmm.service.excel.ExcelDomWriterCheck
node src/test/js/ExcelDownloadCheck.js <exceljs.min.js의 절대 경로>
java -cp <테스트 classpath> com.cmm.service.excel.ExcelDomWriterCheck --browser
```

첫 명령이 기본 엑셀 파일을 생성하고, 두 번째가 JSP의 다운로드 함수를 실행하며,
마지막 명령이 생성된 파일을 실제 XML Parser로 읽어 값·반복 행·이미지를 확인한다.

## sample.xlsx 기준 양식

`D:/document/files/template/sample.xlsx`를 기준으로 제목, 카테고리 이름, 지역 이름, 주소, 시작/종료일, 내용, 상태 이름을 8행에 배치한다.
열 너비는 12/14/20/14/20자, 기본 행 높이는 16.5pt, 내용 행은 120pt다.
제목 배경은 #D9D9D9이며 병합 영역은 샘플과 같다. 날짜는 Excel 날짜 셀로 기록한다.
XML의 `sheet.defaultRowHeight`와 `cell.vertical`로 행 높이와 수직 정렬을 지정할 수 있다.
이벤트용 `EventExcelStyle`은 샘플의 글꼴·배경·테두리를 적용한다.
카테고리는 `event.categoryName`, 지역은 `event.regName`, 상태는 `event.statusName`으로 읽는다.

실제 샘플 검증: `java -cp <테스트 classpath> com.cmm.service.excel.ExcelDomWriterCheck --sample D:/document/files/template/sample.xlsx`
생성한 확인용 파일은 `target/sample-layout-preview.xlsx`, 빈 양식은 `target/sample-layout-blank.xlsx`다.

## DOM/SAX 파서 선택

- `ExcelDomFileParser`: 기존 ExcelFileParserUtil의 새 이름. `WorkbookFactory`로 .xls/.xlsx를 읽으며 기본 구현(@Primary)이다.
- `ExcelSaxFileParser`: `XSSFReader`와 SAX 핸들러로 .xlsx의 시트 XML을 읽는다. 전체 `XSSFWorkbook`은 만들지 않는다.
- 두 클래스는 공통 추상 클래스 `ExcelFileParser`를 상속하며 단일 파일/여러 파일, 범용 미리보기/XML 매핑 API가 같다.
- `ExcelFileParser`가 업로드 제한, 파일 메타데이터, 응답 및 오류 처리를 공통으로 제공한다.
- XML 매핑과 시트명·고정 문구·병합·반복 행 검사도 `ExcelFileParser` 내부에서 공통으로 처리한다.
- `.xls`는 DOM 파서를 사용한다. SAX로 업로드하면 HTTP 400 / EXCEL_SAX_EXTENSION_NOT_ALLOWED를 반환한다.
- 기존 POST `/event/file/template/upload.do`는 DOM, 새 POST `/event/file/template/upload/sax.do`는 SAX를 호출한다.
- GET `/event/file/detail/sax.do`는 기존 다운로드 시험용 자리다. 이번 추가는 업로드를 읽는 Parser이며 SAX Writer를 구현하지 않는다.

```java
@Autowired
@Qualifier("excelSaxFileParser")
private ExcelFileParser excelSaxFileParser;

ExcelTemplate.Definition template = excelTemplate.load("excel/EventDetail_Excel.xml");
Map<String, Object> data = excelSaxFileParser.parseMapped(file, template);
```

`parseMapped`는 매핑에 필요한 셀(반복 영역은 필요한 열)을 수집한다.
여러 파일용 `parseXml` 또는 범용 `parse`는 기존 응답의 `sheets.rows`를 제공하기 위해
전체 미리보기 데이터를 수집한다. 이 결과 데이터, ZIP 및 공유 문자열 테이블도 메모리를
사용하므로 SAX라는 이유만으로 전체 처리의 메모리 사용량이 일정해지는 것은 아니다.
수식은 실행하지 않고 저장된 결과만 읽으며 날짜는 DOM과 같은 Date로 반환한다.

추가 검증:

```text
java -cp <테스트 classpath> com.cmm.service.excel.impl.ExcelSaxParserCheck D:/document/files/template/sample.xlsx D:/document/files/template/event_detail_template.xlsx
```

자료형·공유/인라인 문자열·1904 날짜·저장된 수식 결과·병합·반복·오류 응답·
동시 호출·Spring 기본 구현 선택을 비교한다. 별도 DB 저장이나 영구 파일 저장은 하지 않는다.