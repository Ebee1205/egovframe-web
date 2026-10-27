<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="modal fade" id="extraXlsModal" tabindex="-1" aria-labelledby="extraXlsTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
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
                
                <div id="extraExcelPreview" class="mt-4" aria-live="polite"></div>
                
                <details id="extraExcelJson" class="mt-3" hidden>
                    <summary>파싱 JSON 확인</summary>
                    <pre class="border rounded p-3 mt-2" style="white-space: pre-wrap;"></pre>
                </details>
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
    var preview = document.getElementById('extraExcelPreview');
    var json = document.getElementById('extraExcelJson');
    box.addEventListener('file-input-box:change', function (event) {
        var content = event.detail.fileContent;
        preview.textContent = '';
        json.hidden = !content;
        json.querySelector('pre').textContent = content ? JSON.stringify(content, null, 2) : '';
        if (!content) return;
        content.sheets.forEach(function (sheet) {
            var section = document.createElement('section');
            section.className = 'mb-4';
            var heading = document.createElement('h3');
            heading.className = 'fs-6';
            heading.textContent = sheet.sheetName;
            section.appendChild(heading);
            if (!sheet.rows.length) {
                var empty = document.createElement('p');
                empty.textContent = '표시할 셀 텍스트가 없습니다.';
                section.appendChild(empty);
            } else {
                var scroll = document.createElement('div');
                scroll.className = 'table-responsive';
                var table = document.createElement('table');
                table.className = 'table table-bordered table-sm';
                table.setAttribute('aria-label', sheet.sheetName);
                var body = document.createElement('tbody');
                sheet.rows.forEach(function (row) {
                    var tr = document.createElement('tr');
                    var number = document.createElement('th');
                    number.scope = 'row';
                    number.textContent = row.rowNumber;
                    tr.appendChild(number);
                    row.cells.forEach(function (text) {
                        var cell = document.createElement('td');
                        cell.style.whiteSpace = 'pre-wrap';
                        cell.style.minWidth = '8rem';
                        cell.textContent = text;
                        tr.appendChild(cell);
                    });
                    body.appendChild(tr);
                });
                table.appendChild(body);
                scroll.appendChild(table);
                section.appendChild(scroll);
            }
            preview.appendChild(section);
        });
    });
})();
</script>
