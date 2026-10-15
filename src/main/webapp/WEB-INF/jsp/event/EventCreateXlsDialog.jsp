<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="modal fade" id="eventCreateXlsModal" tabindex="-1" aria-labelledby="eventCreateXlsTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title | fs-5" id="eventCreateXlsTitle">이벤트 엑셀 업로드</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            
            <div class="modal-body">
                <p>엑셀 파일을 선택해 이벤트를 등록합니다.</p>
                <a href="<c:url value='/event/file/template/download.do'/>" class="mt-2 | btn | btn-sm | btn-outline">
                    이벤트 등록 엑셀 양식 다운로드
                </a>
                
                <div class="mt-4">
                    <jsp:include page="/WEB-INF/jsp/cmp/CmpFileParseBox.jsp">
                        <jsp:param name="id" value="eventExcelAttachment" />
                        <jsp:param name="uploadUrl" value="/event/file/template/upload.do" />
                        <jsp:param name="name" value="fileIds" />
                        <jsp:param name="multiple" value="false" />
                        <jsp:param name="label" value="엑셀 파일 업로드" />
                        <jsp:param name="description" value="파일을 선택하면 자동으로 업로드됩니다." />
                        <jsp:param name="accept" value=".xls,.xlsx" />
                        <jsp:param name="maxSize" value="${fileMaxSize}" />
                    </jsp:include>
                </div>
                
            </div>
            
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                <button type="button" id="btnApplyEventExcel" class="btn btn-outline-secondary">
				    업로드
				</button>
            </div>
        </div>
    </div>
</div>

<script>
document.getElementById('btnApplyEventExcel').addEventListener('click', function () {
    const component = document.getElementById('eventExcelAttachment');
    if (component.getAttribute('aria-busy') === 'true') {
        alert('파일 처리가 완료될 때까지 기다려주세요.');
        return;
    }

    const content = component.getFileContent();
    if (!content) {
        alert('이벤트 엑셀 파일을 선택하고 오류가 있다면 다시 선택해주세요.');
        return;
    }

    const sheet = Array.isArray(content.sheets)
        ? content.sheets.find(sheet => sheet && sheet.sheetName === '이벤트 상세')
        : null;
    const rows = sheet && sheet.rows;
    if (!Array.isArray(rows) || rows.some(row => !row || !Array.isArray(row.cells))) {
        alert('템플릿을 확인해주세요.');
        return;
    }
    function cell(rowNumber, columnIndex) {
        const row = rows.find(row => row.rowNumber === rowNumber);
        return String(row?.cells?.[columnIndex] ?? '');
    }

    const headers = [
        [1, 0, '이벤트 제목'],
        [2, 0, '이벤트 지역'],
        [3, 0, '이벤트 주소'],
        [4, 0, '이벤트 기간'],
        [4, 1, '시작일'],
        [4, 3, '종료일'],
        [5, 0, '이벤트 내용'],
        [7, 0, '이벤트 상태']
    ];
    if (headers.some(([row, column, label]) => cell(row, column).trim() !== label)) {
        alert('템플릿을 확인해주세요.');
        return;
    }

    const values = {
        title: cell(1, 1),
        address: cell(3, 1),
        sDate: cell(4, 2),
        eDate: cell(4, 4),
        dsc: cell(6, 0)
    };
    const region = document.getElementById('parentRegId');
    const regionValue = cell(2, 1).trim();
    const status = document.getElementById('status');
    const statusValue = cell(7, 1).trim();
    function findOption(select, value) {
        return Array.from(select.options).find(option => option.value &&
            (option.value === value || option.textContent.trim() === value));
    }
    const regionOption = findOption(region, regionValue);
    const statusOption = findOption(status, statusValue);
    if (regionValue && !regionOption) {
        alert('엑셀의 이벤트 지역을 확인해주세요.');
        return;
    }
    if (statusValue && !statusOption) {
        alert('엑셀의 이벤트 상태를 확인해주세요.');
        return;
    }

    Object.entries(values).forEach(([id, value]) => {
        const field = document.getElementById(id);
        field.value = value;
        field.dispatchEvent(new Event('input', {bubbles: true}));
        field.dispatchEvent(new Event('change', {bubbles: true}));
    });
    region.value = regionOption ? regionOption.value : '';
    region.dispatchEvent(new Event('change', {bubbles: true}));
    status.value = statusOption ? statusOption.value : '';
    status.dispatchEvent(new Event('change', {bubbles: true}));

    // 닫힐 때 컴포넌트가 초기화되므로 폼에 값을 먼저 복사한다.
    coreui.Modal.getOrCreateInstance(document.getElementById('eventCreateXlsModal')).hide();
});

</script>