<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  재사용 파일 입력 컴포넌트 (선택 -> 업로드 -> 파일 ID 반환/제거/되돌리기)

  props (jsp:param)
    id          : 컴포넌트 식별자 접두사 (기본 fileInputBox, 한 페이지에 여러 개 사용 시 서로 다르게 지정)
    name        : 파일 ID를 담는 hidden input 이름 (기본 fileId)
    fileId      : 현재 첨부된 파일 ID
    fileName    : 현재 첨부된 파일 원본명
    label       : 라벨 문구 (기본 첨부파일)
    accept      : 선택 가능한 확장자 (input accept 값)
    maxSize     : 최대 파일 크기(byte), 0 이하이면 검사 생략
    uploadUrl   : 업로드 URL (기본 /cmm/file/upload.do)
    deleteUrl   : 삭제 URL (기본 /cmm/file/delete.do)
    downloadUrl : 다운로드 URL (기본 /cmm/file/download.do)

  동작
    - 컴포넌트를 감싸는 form 안에 두면 hidden input(name)으로 파일 ID가 함께 전송된다.
    - 업로드/처리 중이거나 선택만 하고 업로드하지 않은 상태에서는 form 제출을 막는다.
    - 값이 바뀌면 컴포넌트 루트에서 'file-input-box:change' 이벤트(detail.fileId)가 발생한다.
--%>
<c:set var="fibId" value="${empty param.id ? 'fileInputBox' : param.id}" />
<c:set var="fibName" value="${empty param.name ? 'fileId' : param.name}" />
<c:url var="fibUploadUrl" value="${empty param.uploadUrl ? '/cmm/file/upload.do' : param.uploadUrl}" />
<c:url var="fibDeleteUrl" value="${empty param.deleteUrl ? '/cmm/file/delete.do' : param.deleteUrl}" />
<c:url var="fibDownloadUrl" value="${empty param.downloadUrl ? '/cmm/file/download.do' : param.downloadUrl}">
    <c:if test="${not empty param.fileId}">
        <c:param name="fileId" value="${param.fileId}"/>
    </c:if>
</c:url>

<div id="<c:out value='${fibId}'/>" class="file-input-box"
    data-upload-url="<c:out value='${fibUploadUrl}'/>"
    data-delete-url="<c:out value='${fibDeleteUrl}'/>"
    data-max-size="<c:out value='${empty param.maxSize ? 0 : param.maxSize}'/>" aria-busy="false">
    <input type="hidden" data-role="id" name="<c:out value='${fibName}'/>" value="<c:out value='${param.fileId}'/>">
    <label for="<c:out value='${fibId}'/>Input" class="form-label"><c:out value="${empty param.label ? '첨부파일' : param.label}"/></label>
    <div class="input-group">
        <input type="file" class="form-control" data-role="picker" id="<c:out value='${fibId}'/>Input"
            accept="<c:out value='${param.accept}'/>" aria-describedby="<c:out value='${fibId}'/>Status">
        <button type="button" class="btn btn-outline-secondary" data-role="upload" disabled>업로드</button>
    </div>

    <div data-role="current" class="mb-2 | file-content" ${empty param.fileId ? 'hidden' : ''}>
        <span data-role="name" class="me-2 | file-content-text"><c:out value="${empty param.fileName ? '첨부파일' : param.fileName}"/></span>
        <a data-role="download" class="btn | btn-sm | btn-outline-secondary"
            href="<c:out value='${fibDownloadUrl}'/>" download>다운로드</a>
        <button data-role="remove" type="button" class="btn-close | btn-sm" aria-label="첨부파일 제거"></button>
    </div>

    <p data-role="empty" class="text-body-secondary | mb-0" ${empty param.fileId ? '' : 'hidden'}>첨부된 파일이 없습니다.</p>
    <p data-role="status" id="<c:out value='${fibId}'/>Status" class="form-text | mb-0" role="status" aria-live="polite"></p>
</div>
<script>
(function () {
    'use strict';

    var box = document.currentScript.previousElementSibling;
    if (!box || !box.classList.contains('file-input-box')) { return; }

    function role(name) { return box.querySelector('[data-role="' + name + '"]'); }

    var form = box.closest('form');
    var idInput = role('id');
    var picker = role('picker');
    var uploadButton = role('upload');
    var removeButton = role('remove');
    var undoButton = role('undo');
    var currentRow = role('current');
    var emptyRow = role('empty');
    var name = role('name');
    var download = role('download');
    var status = role('status');
    // The undo button is optional; validate the required elements before binding any listener.
    if (!idInput || !picker || !uploadButton || !removeButton || !currentRow || !emptyRow || !name || !download || !status) {
        if (status) { status.textContent = '첨부파일 화면 구성을 확인해주세요.'; }
        if (uploadButton) { uploadButton.disabled = true; }
        console.error('CmpFileInputBox: required elements are missing.');
        return;
    }
    var maxSize = Number(box.getAttribute('data-max-size'));
    var original = idInput.value ? {
        fileId: idInput.value,
        originalName: name.textContent,
        downloadUrl: download.getAttribute('href')
    } : null;
    var current = original;
    var busy = false;
    var lockedButtons = [];

    function tell(message, isError) {
        status.textContent = message || '';
        status.classList.toggle('text-danger', !!isError);
        status.classList.toggle('text-body-secondary', !isError);
    }

    function render() {
        var previousValue = idInput.value;
        idInput.value = current ? String(current.fileId) : '';
        currentRow.hidden = !current;
        currentRow.classList.toggle('d-none', !current);
        emptyRow.hidden = !!current;
        emptyRow.classList.toggle('d-none', !!current);
        name.textContent = current ? current.originalName : '';
        if (current) { download.setAttribute('href', current.downloadUrl); }
        else { download.removeAttribute('href'); }
        removeButton.disabled = busy || !current;
        uploadButton.disabled = busy || picker.files.length === 0;
        if (undoButton) {
            undoButton.hidden = idInput.value === (original ? String(original.fileId) : '');
            undoButton.disabled = busy;
        }
        if (previousValue !== idInput.value) {
            box.dispatchEvent(new CustomEvent('file-input-box:change', {
                bubbles: true,
                detail: {fileId: idInput.value || null}
            }));
        }
    }

    function setBusy(value) {
        busy = value;
        box.setAttribute('aria-busy', String(value));
        picker.disabled = value;
        uploadButton.textContent = value ? '처리 중…' : '업로드';
        if (value) {
            var selector = 'button[type="submit"]';
            var submitButtons = form
                ? Array.from(form.querySelectorAll(selector))
                    .concat(form.id ? Array.from(document.querySelectorAll(selector + '[form="' + form.id + '"]')) : [])
                : [];
            lockedButtons = submitButtons.map(function (button) {
                var wasDisabled = button.disabled;
                button.disabled = true;
                return {button: button, wasDisabled: wasDisabled};
            });
        } else {
            lockedButtons.forEach(function (item) { item.button.disabled = item.wasDisabled; });
            lockedButtons = [];
        }
        render();
    }

    function message(result, fallback) {
        var messages = [];
        if (result && result.code && result.code.message) { messages.push(result.code.message); }
        var fields = result && result.error && result.error.fields || [];
        fields.forEach(function (field) {
            if (field.message && messages.indexOf(field.message) < 0) { messages.push(field.message); }
        });
        return messages.length ? messages.join(' ') : fallback;
    }

    async function request(url, body) {
        var response = await fetch(url, {method: 'POST', body: body});
        var result;
        try { result = await response.json(); }
        catch (parseError) { throw new Error('서버 응답을 확인할 수 없습니다. 잠시 후 다시 시도해주세요.'); }
        if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS') {
            throw new Error(message(result, '파일 처리에 실패했습니다.'));
        }
        return result.data;
    }

    // Only files uploaded on this page and never saved are eligible for cleanup.
    function isDraft(file) {
        return file && (!original || String(file.fileId) !== String(original.fileId));
    }

    async function deleteDraft(file) {
        if (!isDraft(file)) { return; }
        var body = new URLSearchParams();
        body.set('fileId', String(file.fileId));
        await request(box.getAttribute('data-delete-url'), body);
    }

    function confirmRemove(onOk) {
        if (window.CmpDialog) {
            CmpDialog.open({
                title: '첨부 제거',
                text: '이 글에서 첨부를 제거하시겠습니까? 저장 버튼을 누르면 반영됩니다.',
                okText: '제거',
                cancelText: '취소',
                okButton: function () {
                    CmpDialog.close();
                    onOk();
                }
            });
        } else if (window.confirm('첨부를 제거하시겠습니까?')) {
            onOk();
        }
    }

    picker.addEventListener('change', function () {
        tell(picker.files.length ? '파일을 선택했습니다. 업로드 버튼을 눌러주세요.' : '');
        render();
    });

    uploadButton.addEventListener('click', async function () {
        if (busy || !picker.files.length) { return; }
        var file = picker.files[0];
        if (file.size === 0 || (maxSize > 0 && file.size > maxSize)) {
            tell(file.size === 0 ? '빈 파일은 업로드할 수 없습니다.' : '파일 크기 제한을 초과했습니다.', true);
            return;
        }
        var previous = current;
        setBusy(true);
        tell('파일을 업로드하고 있습니다.');
        try {
            var body = new FormData();
            body.append('files', file);
            var data = await request(box.getAttribute('data-upload-url'), body);
            if (!Array.isArray(data) || data.length !== 1 || !data[0]
                    || !/^[1-9]\d*$/.test(String(data[0].fileId)) || !data[0].downloadUrl) {
                throw new Error('업로드한 파일 정보를 확인할 수 없습니다.');
            }
            current = data[0];
            picker.value = '';
            render();
            tell('업로드했습니다. 하단의 저장 버튼을 눌러 첨부를 반영해주세요.');
            try { await deleteDraft(previous); }
            catch (cleanupError) {
                tell('업로드했습니다. 저장 버튼을 눌러주세요. 이전 임시 파일 정리는 실패했습니다.', true);
            }
        } catch (error) {
            tell(error.message || '파일 업로드에 실패했습니다.', true);
        } finally {
            setBusy(false);
        }
    });

    removeButton.addEventListener('click', function () {
        if (busy || !current) { return; }
        confirmRemove(async function () {
            if (busy) { return; }
            setBusy(true);
            try {
                await deleteDraft(current);
                current = null;
                picker.value = '';
                tell('첨부를 제거했습니다. 저장 버튼을 눌러주세요.');
            } catch (error) {
                tell(error.message || '임시 첨부 파일을 정리하지 못했습니다.', true);
            } finally { setBusy(false); }
        });
    });

    if (undoButton) {
        undoButton.addEventListener('click', async function () {
            if (busy) { return; }
            setBusy(true);
            try {
                await deleteDraft(current);
                current = original;
                picker.value = '';
                tell('기존 첨부 상태로 되돌렸습니다.');
            } catch (error) {
                tell(error.message || '첨부 변경을 취소하지 못했습니다.', true);
            } finally { setBusy(false); }
        });
    }

    if (form) {
        // Capture phase runs before the shared AJAX confirmation/submission handler.
        form.addEventListener('submit', function (event) {
            if (busy || picker.files.length) {
                event.preventDefault();
                event.stopImmediatePropagation();
                tell(busy ? '파일 처리가 끝난 후 저장해주세요.' : '선택한 파일을 먼저 업로드해주세요.', true);
            }
        }, true);
    }

    render();
})();
</script>
