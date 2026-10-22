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
                <p>엑셀 파일의 내용을 신규 등록 폼에 불러옵니다. 저장은 폼 하단의 등록 버튼으로 진행합니다.</p>
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
                <button type="button" id="btnApplyEventExcel" class="btn | btn-outline-secondary">폼에 반영</button>
            </div>
        </div>
    </div>
</div>

<script>
(function () {
    'use strict';
    var button = document.getElementById('btnApplyEventExcel');
    button.addEventListener('click', function () {
        var form = document.getElementById('eventCreateForm');
        if (!form || form.querySelector('[name="eid"]')) return;
        var component = document.getElementById('eventExcelAttachment');
        if (component.getAttribute('aria-busy') === 'true') {
            alert('파일 처리가 완료될 때까지 기다려주세요.'); return;
        }
        var content = component.getFileContent();
        var event = content && content.event;
        if (!event) { alert('이벤트 엑셀 파일을 선택하고 오류가 있다면 다시 선택해주세요.'); return; }
        function field(id) { return form.querySelector('#' + id); }
        function hasOption(select, value) {
            return Array.from(select.options).some(function (option) { return option.value && option.value === String(value); });
        }
        var parent = field('parentRegId');
        var child = field('reg_id');
        var isRoot = String(event.reg_id) === String(event.parentRegId);
        var validChild = isRoot || Array.from(child.options).some(function (option) {
            return option.value === String(event.reg_id) && option.dataset.parentId === String(event.parentRegId);
        });
        if (!hasOption(field('ctg'), event.ctg) || !hasOption(field('status'), event.status)
                || !hasOption(parent, event.parentRegId) || !validChild) {
            alert('카테고리, 상태 또는 지역이 변경되었습니다. 파일을 다시 선택해주세요.'); return;
        }
        // 서버 응답 전체를 바인딩하지 않고 신규 등록 입력 필드만 복사한다.
        ['title', 'ctg', 'status', 'sDate', 'eDate', 'dsc', 'address'].forEach(function (id) {
            var input = field(id);
            input.value = event[id] == null ? '' : String(event[id]);
            input.dispatchEvent(new Event('input', {bubbles: true}));
            input.dispatchEvent(new Event('change', {bubbles: true}));
        });
        parent.value = String(event.parentRegId);
        parent.dispatchEvent(new Event('change', {bubbles: true}));
        child.value = isRoot ? '' : String(event.reg_id);
        child.dispatchEvent(new Event('change', {bubbles: true}));
        field('eventRegionValue').value = String(event.reg_id);
        coreui.Modal.getOrCreateInstance(document.getElementById('eventCreateXlsModal')).hide();
    });
})();
</script>
