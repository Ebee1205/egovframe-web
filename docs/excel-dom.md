# 공통 XML 양식을 사용하는 엑셀 입출력

```text
classpath:excel/*_Excel.xml
            |
     ExcelTemplate.load()
            |
      불변 Definition
         /       \
ExcelFileWriter   ExcelFileParser (구현: ExcelFileParserUtil)
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
data.put("event", eventVO);
data.put("statusName", "진행중");

byte[] bytes = excelFileWriter.writeMapped(data, template, excelStyle);
Map<String, Object> parsed = excelFileParser.parseMapped(upload, template);
```

경로를 받는 기존 `writeXml`/`downloadXml`도 내부에서 ExcelTemplate을 사용한다.
단일 업로드는 `excelFileParser.parseXml(upload, classpathXml)`로도 처리할 수 있다.

## 업무 호출

- GET `/event/file/template/download.do`: EventDetail_Excel.xml로 빈 양식을 생성한다.
  과거 로컬 XLSX 고정 경로를 대체한다.
- POST `/event/file/template/upload.do`: 같은 XML로 업로드 양식의 시트명·고정 문구·병합 범위를 확인하고 속성을 읽는다.
  기존 `fileContent.sheets` 미리보기를 유지하며 `fileContent.data`에 매핑 결과를 추가한다.
- GET `/event/file/detail/dom.do?eid=1`: 저장된 이벤트를 같은 양식으로 내려준다.
- 기존 범용 `parse(files, description)`은 XML 검증 없이 셀 미리보기만 반환한다.
- SAX 시험용 경로는 이번 변경에 포함하지 않는다.

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
