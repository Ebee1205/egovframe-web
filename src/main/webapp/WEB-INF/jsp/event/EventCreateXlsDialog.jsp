<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="eventExcelInsertUrl" value="/event/file/template/insert.do"/>
<c:url var="eventExcelDetailUrl" value="/event/detail.do"/>
<div data-insert-url="<c:out value='${eventExcelInsertUrl}'/>" data-detail-url="<c:out value='${eventExcelDetailUrl}'/>" class="modal fade" id="eventCreateXlsModal" tabindex="-1" aria-labelledby="eventCreateXlsTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title | fs-5" id="eventCreateXlsTitle">이벤트 엑셀 업로드</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            
            <div class="modal-body">
                <p>‘폼에 반영’은 입력값만 채웁니다. ‘등록’은 선택한 엑셀 내용으로 이벤트를 바로 저장합니다.</p>
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
            
            <p id="eventExcelInsertStatus" class="px-3 mb-2" role="status" aria-live="polite"></p>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                <button type="button" id="btnApplyEventExcel" class="btn | btn-outline-secondary">폼에 반영</button>
                <button type="button" id="btnInsertEventExcel" class="btn | btn-primary">등록</button>
            </div>
        </div>
    </div>
</div>

<script>
(function () {
    'use strict';
    var modal = document.getElementById('eventCreateXlsModal');
    var insertButton = document.getElementById('btnInsertEventExcel');
    var status = document.getElementById('eventExcelInsertStatus');
    var saving = false;
    function showStatus(message, error) {
        status.textContent = message;
        status.classList.toggle('text-danger', !!error);
    }
    modal.addEventListener('hide.coreui.modal', function (event) {
        if (saving) event.preventDefault();
    });
    modal.addEventListener('hidden.coreui.modal', function () { showStatus('', false); });
    insertButton.addEventListener('click', async function () {
        var form = document.getElementById('eventCreateForm');
        if (!form || form.querySelector('[name="eid"]') || saving) return;
        var component = document.getElementById('eventExcelAttachment');
        if (component.getAttribute('aria-busy') === 'true') {
            showStatus('파일 처리가 완료될 때까지 기다려주세요.', true); return;
        }
        var content = component.getFileContent();
        var file = component.getFile();
        if (!file || !content || !content.event) {
            showStatus('이벤트 엑셀 파일을 선택하고 파싱 결과를 확인해주세요.', true); return;
        }
        if (typeof syncEventCreatedBy === 'function') syncEventCreatedBy();
        var author = form.querySelector('[name="createdBy"]');
        if (!author || !/^[1-9]\d*$/.test(author.value)) {
            showStatus('등록할 사용자를 선택해주세요.', true); return;
        }
        var body = new FormData();
        body.append('files', file);
        body.append('createdBy', author.value);
        // 기존 폼에 CSRF 토큰이 있는 경우 그대로 전달한다.
        var csrf = form.querySelector('input[name="_csrf"]');
        if (csrf) body.append(csrf.name, csrf.value);
        saving = true;
        var buttons = Array.from(modal.querySelectorAll('button'));
        var disabled = buttons.map(function (button) { return button.disabled; });
        component.setProcessing(true);
        buttons.forEach(function (button) { button.disabled = true; });
        insertButton.textContent = '등록 중...';
        showStatus('이벤트를 등록하고 있습니다.', false);
        var complete = false;
        try {
            var response = await fetch(modal.getAttribute('data-insert-url'), {
                method: 'POST', credentials: 'same-origin', body: body
            });
            var result;
            try { result = await response.json(); }
            catch (error) { throw new Error('응답을 확인하지 못했습니다. 등록 여부를 확인 후 다시 시도해주세요.'); }
            if (!response.ok || !result || !result.code || result.code.resultCode !== 'SUCCESS') {
                var messages = [];
                if (result && result.code && result.code.message) messages.push(result.code.message);
                var fields = result && result.error && result.error.fields || [];
                fields.forEach(function (field) {
                    if (field.message && messages.indexOf(field.message) < 0) messages.push(field.message);
                });
                throw new Error(messages.join(' ') || '이벤트를 등록하지 못했습니다.');
            }
            var eid = result.data && result.data.eid;
            if (!/^[1-9]\d*$/.test(String(eid))) throw new Error('등록 결과의 이벤트 ID를 확인할 수 없습니다. 목록에서 등록 여부를 확인해주세요.');
            complete = true;
            showStatus('등록되었습니다. 상세 화면으로 이동합니다.', false);
            window.location.assign(modal.getAttribute('data-detail-url') + '?eid=' + encodeURIComponent(eid));
        } catch (error) {
            showStatus(error.message || '이벤트 등록 요청에 실패했습니다. 등록 여부를 확인해주세요.', true);
        } finally {
            if (!complete) {
                saving = false;
                component.setProcessing(false);
                buttons.forEach(function (button, index) { button.disabled = disabled[index]; });
                insertButton.textContent = '등록';
            }
        }
    });
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
