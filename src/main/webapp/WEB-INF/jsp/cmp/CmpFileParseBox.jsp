<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  단일 파일 파싱 컴포넌트: 선택 즉시 업로드하고 data[0].fileContent를 반환한다.
  props: id, label, description, accept, maxSize, uploadUrl
  API: await element.uploadFile(file), element.getFileContent(), element.reset()
  이벤트: file-input-box:change -> detail.fileContent (재선택/첨부 취소 시 null로 초기화)
  파일 ID, 다운로드 및 삭제 API는 사용하지 않는다.
--%>
<c:set var="fibId" value="${empty param.id ? 'fileParseBox' : param.id}" />
<c:url var="fibUploadUrl" value="${empty param.uploadUrl ? '/event/file/template/upload.do' : param.uploadUrl}" />

<div id="<c:out value='${fibId}'/>" class="file-input-box file-parse-box krds-file-upload"
    data-upload-url="<c:out value='${fibUploadUrl}'/>"
    data-max-size="<c:out value='${empty param.maxSize ? 0 : param.maxSize}'/>"
    aria-busy="false" role="group" aria-labelledby="<c:out value='${fibId}'/>Label">
    <label id="<c:out value='${fibId}'/>Label" for="<c:out value='${fibId}'/>Input"
        class="form-label | fw-semibold | upload-label"><c:out value="${empty param.label ? '파일 업로드' : param.label}"/></label>
    <p id="<c:out value='${fibId}'/>Help" class="form-text upload-help">
        <c:out value="${empty param.description ? '파일을 선택하면 자동으로 업로드하여 내용을 읽습니다.' : param.description}"/>
        <br>파일 형식: <c:out value="${empty param.accept ? '제한 없음' : param.accept}"/>
        <span data-role="limit"></span> · 한 번에 1개 파일을 선택할 수 있습니다.
    </p>
    <div class="file-upload" data-role="dropzone">
        <p class="txt">파일을 여기에 끌어다 놓거나,<br>파일 선택 버튼을 눌러 선택해주세요.</p>
        
        <input type="file" class="upload-picker" data-role="picker" id="<c:out value='${fibId}'/>Input"
            accept="<c:out value='${param.accept}'/>"
            aria-describedby="<c:out value='${fibId}'/>Help <c:out value='${fibId}'/>Status">
        <div class="upload-actions">
            <button type="button" class="btn | btn-secondary" data-role="select"
                aria-controls="<c:out value='${fibId}'/>Input" aria-busy="false">
                <span data-role="select-loading" hidden><span class="spinner-border spinner-border-sm" aria-hidden="true"></span><span class="visually-hidden">파일 처리 중</span></span>
                <span data-role="select-label">파일 선택</span>
            </button>
        </div>
        <p data-role="status" id="<c:out value='${fibId}'/>Status" class="form-text | upload-status"
            role="status" aria-live="polite" aria-atomic="true"></p>
        
	    <div data-role="file-list" hidden>
	        <div data-role="current" class="file-item | border | rounded | bg-body | d-inline-flex | align-items-center | gap-2"
	            role="group" aria-label="선택된 파일" aria-busy="false">
	            <div class="file-name">
	                <span data-role="name" id="<c:out value='${fibId}'/>FileName" class="link-body-emphasis"></span>
	                <span data-role="size" class="file-size"></span>
	            </div>
	            <div class="file-btn">
	                <span data-role="file-loading" hidden><span class="spinner-border spinner-border-sm text-secondary" aria-hidden="true"></span><span class="visually-hidden">파일 처리 중</span></span>
	                <button data-role="remove" type="button" class="btn-close | flex-shrink-0"
	                    aria-label="첨부 취소" aria-describedby="<c:out value='${fibId}'/>FileName"></button>
	            </div>
	        </div>
	    </div>
	    
    </div>
    
    
</div>
<script>
(function () {
    'use strict';
    var box = document.currentScript.previousElementSibling;
    if (!box || !box.classList.contains('file-parse-box')) { return; }
    function role(name) { return box.querySelector('[data-role="' + name + '"]'); }
    var picker = role('picker');
    var selectButton = role('select');
    var name = role('name');
    var fileList = role('file-list');
    var size = role('size');
    var removeButton = role('remove');
    var selectLabel = role('select-label');
    var selectLoading = role('select-loading');
    var fileLoading = role('file-loading');
    var currentRow = role('current');
    var status = role('status');
    var dropzone = role('dropzone');
    var form = box.closest('form');
    var modal = box.closest('.modal');
    var maxSize = Number(box.getAttribute('data-max-size'));
    var busy = false;
    var selectedFile = null;
    var fileContent = null;
    var dragDepth = 0;
    var requestVersion = 0;
    var activeRequest = null;

    function formatSize(bytes) {
        if (bytes < 1024) { return bytes + ' B'; }
        if (bytes < 1048576) { return (bytes / 1024).toFixed(1) + ' KB'; }
        return (bytes / 1048576).toFixed(1) + ' MB';
    }
    if (maxSize > 0) { role('limit').textContent = ' · 최대 ' + formatSize(maxSize); }
    function showError(text) {
        status.textContent = text;
        status.classList.add('text-danger');
    }
    function clearError() {
        status.textContent = '';
        status.classList.remove('text-danger');
    }
    function setBusy(value) {
        busy = value;
        box.setAttribute('aria-busy', String(value));
        picker.disabled = value;
        selectButton.disabled = value;
        removeButton.disabled = value;
        selectLoading.hidden = !value;
        fileLoading.hidden = !value;
        selectButton.setAttribute('aria-busy', String(value));
        currentRow.setAttribute('aria-busy', String(value));
    }
    function notify() {
        box.dispatchEvent(new CustomEvent('file-input-box:change', {
            bubbles: true,
            detail: {fileContent: fileContent}
        }));
    }
    function accepts(file) {
        var types = picker.accept.toLowerCase().split(',').map(function (type) { return type.trim(); }).filter(Boolean);
        return !types.length || types.some(function (type) {
            if (type.charAt(0) === '.') { return file.name.toLowerCase().endsWith(type); }
            if (type.endsWith('/*')) { return file.type.toLowerCase().indexOf(type.slice(0, -1)) === 0; }
            return file.type.toLowerCase() === type;
        });
    }
    function errorMessage(result) {
        var messages = [];
        if (result && result.code && result.code.message) { messages.push(result.code.message); }
        var fields = result && result.error && result.error.fields || [];
        fields.forEach(function (field) {
            if (field.message && messages.indexOf(field.message) < 0) { messages.push(field.message); }
        });
        return messages.join(' ') || '파일을 읽지 못했습니다. 다시 선택해주세요.';
    }

    // 직접 호출하면 Promise가 fileContent를 반환한다.
    box.uploadFile = async function (file) {
        if (busy) { throw new Error('파일 처리가 끝난 후 다시 선택해주세요.'); }
        if (!file) { throw new Error('파일을 선택해주세요.'); }
        var version = ++requestVersion;
        var controller = new AbortController();
        activeRequest = controller;
        clearError();
        selectedFile = file;
        fileContent = null;
        name.textContent = '';
        size.textContent = '';
        fileList.hidden = true;
        selectLabel.textContent = '파일 선택';
        picker.value = '';
        setBusy(true);
        try {
            notify();
            if (!accepts(file)) { throw new Error('허용되지 않는 파일 형식입니다.'); }
            if (file.size === 0) { throw new Error('빈 파일은 업로드할 수 없습니다.'); }
            if (maxSize > 0 && file.size > maxSize) { throw new Error('파일 크기 제한을 초과했습니다.'); }
            name.textContent = file.name;
            size.textContent = '(' + formatSize(file.size) + ')';
            fileList.hidden = false;
            selectLabel.textContent = '파일 재선택';
            var body = new FormData();
            body.append('files', file);
            var response = await fetch(box.getAttribute('data-upload-url'), {
                method: 'POST', body: body, signal: controller.signal
            });
            var result;
            try { result = await response.json(); }
            catch (parseError) { throw new Error('서버 응답을 확인할 수 없습니다. 다시 시도해주세요.'); }
            if (version !== requestVersion) { return null; }
            if (!response.ok || !result || !result.code || result.code.resultCode !== 'SUCCESS') {
                throw new Error(errorMessage(result));
            }
            var data = result.data;
            if (!Array.isArray(data) || data.length !== 1 || !data[0]
                    || !data[0].fileContent || !Array.isArray(data[0].fileContent.sheets)) {
                throw new Error('파일 파싱 결과를 확인할 수 없습니다.');
            }
            fileContent = data[0].fileContent;
            clearError();
        } catch (error) {
            if (version !== requestVersion) { return null; }
            showError(error.message || '파일을 읽지 못했습니다. 다시 선택해주세요.');
            throw error;
        } finally {
            if (version === requestVersion) {
                activeRequest = null;
                setBusy(false);
            }
        }
        var parsedContent = fileContent;
        notify();
        return parsedContent;
    };
    box.getFileContent = function () { return fileContent; };

    box.reset = function () {
        requestVersion++;
        if (activeRequest) {
            activeRequest.abort();
            activeRequest = null;
        }
        selectedFile = null;
        fileContent = null;
        picker.value = '';
        name.textContent = '';
        size.textContent = '';
        fileList.hidden = true;
        selectLabel.textContent = '파일 선택';
        dragDepth = 0;
        dropzone.classList.remove('is-dragover');
        clearError();
        setBusy(false);
        notify();
    };

    removeButton.addEventListener('click', function () {
        if (busy || !selectedFile) { return; }
        box.reset();
        selectButton.focus();
    });

    if (modal) {
        modal.addEventListener('hidden.coreui.modal', function (event) {
            if (event.target === modal) { box.reset(); }
        });
    }

    function choose(files) {
        if (busy || !files || !files.length) { return; }
        if (files.length !== 1) {
            picker.value = '';
            showError('한 번에 1개 파일만 선택해주세요.');
            return;
        }
        // 클릭/드롭 오류는 상태 영역에 표시한다. 직접 호출자는 Promise로도 받는다.
        box.uploadFile(files[0]).catch(function () {});
    }
    selectButton.addEventListener('click', function () { picker.click(); });
    picker.addEventListener('change', function () { choose(picker.files); });
    dropzone.addEventListener('dragenter', function (event) {
        event.preventDefault();
        dragDepth++;
        if (!busy) { dropzone.classList.add('is-dragover'); }
    });
    dropzone.addEventListener('dragover', function (event) {
        event.preventDefault();
        if (event.dataTransfer) { event.dataTransfer.dropEffect = busy ? 'none' : 'copy'; }
    });
    dropzone.addEventListener('dragleave', function (event) {
        event.preventDefault();
        dragDepth = Math.max(0, dragDepth - 1);
        if (!dragDepth) { dropzone.classList.remove('is-dragover'); }
    });
    dropzone.addEventListener('drop', function (event) {
        event.preventDefault();
        dragDepth = 0;
        dropzone.classList.remove('is-dragover');
        if (event.dataTransfer) { choose(event.dataTransfer.files); }
    });
    if (form) {
        form.addEventListener('submit', function (event) {
            if (busy || (selectedFile && fileContent === null)) {
                event.preventDefault();
                event.stopImmediatePropagation();
                showError(busy ? '파일 처리가 끝난 후 진행해주세요.' : '파일을 다시 선택하여 내용을 확인해주세요.');
            }
        }, true);
    }
})();
</script>
