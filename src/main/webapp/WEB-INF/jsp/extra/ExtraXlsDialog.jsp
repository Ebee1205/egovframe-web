<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="modal fade" id="extraXlsModal" tabindex="-1" aria-labelledby="extraXlsTitle" aria-hidden="true">
    <div class="modal-dialog | modal-fullscreen | modal-dialog-centered | modal-dialog-scrollable">
        <div class="modal-content">
            
            <div class="modal-header">
                <h2 class="modal-title | fs-5" id="extraXlsTitle">일괄 업로드</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            
            <div class="modal-body">
                <p id="extraExcelHelp" class="text-body-secondary">엑셀 파일을 선택하면 각 시트의 셀 텍스트를 표시합니다. 파일과 파싱 결과는 저장하지 않습니다.</p>
                
                <div class="mt-4">
                    <jsp:include page="/WEB-INF/jsp/cmp/CmpFileParseBox.jsp">
                        <jsp:param name="id" value="extraExcelAttachment" />
                        <jsp:param name="uploadUrl" value="/extra/file/upload.do" />
                        <jsp:param name="name" value="fileIds" />
                        <jsp:param name="multiple" value="false" />
                        <jsp:param name="label" value="엑셀 파일 업로드" />
                        <jsp:param name="description" value="파일을 선택하면 자동으로 업로드됩니다." />
                        <jsp:param name="accept" value=".xlsx" />
                        <jsp:param name="maxSize" value="${fileMaxSize}" />
                    </jsp:include>
                </div>
            </div>
            
            
            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info">
                    <p class="table-info-text">총 <span id="extraExcelTotalCount">0</span>건</p>
                </div>

                <!-- 테이블 본문 -->
                <div class="table-responsive">
                    <table id="extraExcelTable" class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center">
                            <tr>
                                <th scope="col">시트</th>
                                <th scope="col" style="width: 45px;">NO</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td class="text-center | text-body-secondary | py-4" colspan="2">조회된 데이터가 없습니다.</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                <button type="button" class="btn btn-primary" disabled>등록</button>
            </div>
        </div>
    </div>
</div>
<script>
(function () {
    'use strict';
    var box = document.getElementById('extraExcelAttachment');
    var table = document.getElementById('extraExcelTable');
    var count = document.getElementById('extraExcelTotalCount');
    function columnName(index) {
        var name = '';
        for (index++; index > 0; index = Math.floor((index - 1) / 26)) {
            name = String.fromCharCode(65 + (index - 1) % 26) + name;
        }
        return name;
    }
    box.addEventListener('file-input-box:change', function (event) {
        var content = event.detail.fileContent;
        var sheets = content ? content.sheets : [];
        var columns = 0;
        var total = 0;
        sheets.forEach(function (sheet) {
            total += sheet.rows.length;
            sheet.rows.forEach(function (row) { columns = Math.max(columns, row.cells.length); });
        });
        count.textContent = total;
        var header = table.tHead.rows[0];
        while (header.cells.length > 2) header.deleteCell(2);
        for (var column = 0; column < columns; column++) {
            var th = document.createElement('th');
            th.scope = 'col';
            th.textContent = columnName(column);
            header.appendChild(th);
        }
        var body = table.tBodies[0];
        body.textContent = '';
        sheets.forEach(function (sheet) {
            sheet.rows.forEach(function (row) {
                var tr = document.createElement('tr');
                var name = document.createElement('td');
                name.textContent = sheet.sheetName;
                tr.appendChild(name);
                var number = document.createElement('td');
                number.className = 'text-center';
                number.textContent = row.rowNumber;
                tr.appendChild(number);
                for (var index = 0; index < columns; index++) {
                    var cell = document.createElement('td');
                    cell.style.whiteSpace = 'pre-wrap';
                    cell.textContent = row.cells[index] == null ? '' : row.cells[index];
                    tr.appendChild(cell);
                }
                body.appendChild(tr);
            });
        });
        if (!total) {
            var empty = body.insertRow().insertCell();
            empty.className = 'text-center | text-body-secondary | py-4';
            empty.colSpan = columns + 2;
            empty.textContent = '조회된 데이터가 없습니다.';
        }
    });
})();
</script>
