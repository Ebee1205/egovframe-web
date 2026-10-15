<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  파일 입력 컴포넌트 (선택 -> 업로드 -> 파일 ID 반환/제거/되돌리기)

  props (jsp:param)
    id          : 컴포넌트 식별자 접두사 (기본 fileInputBox, 한 페이지에 여러 개 사용 시 서로 다르게 지정)
    name        : 파일 ID를 담는 hidden input 이름 (기본 fileId)
    fileId      : 현재 첨부된 파일 ID
    fileName    : 현재 첨부된 파일 원본명
    description : 업로드 도움말 문구
    fileSize    : 단일 첨부파일 크기(byte)
    label       : 라벨 문구 (기본 첨부파일)
    accept      : 선택 가능한 확장자 (input accept 값)
    maxSize     : 최대 파일 크기(byte), 0 이하이면 검사 생략
    uploadUrl   : 업로드 URL (기본 /cmm/file/upload.do)
    deleteUrl   : 삭제 URL (기본 /cmm/file/delete.do)
    downloadUrl : 다운로드 URL (기본 /cmm/file/download.do)
    multiple    : true이면 eventFiles 목록과 여러 파일을 처리한다.

  동작
    - 컴포넌트를 감싸는 form 안에 두면 hidden input(name)으로 파일 ID가 함께 전송된다.
    - 업로드/처리 중이거나 선택만 하고 업로드하지 않은 상태에서는 form 제출을 막는다.
    - 값이 바뀌면 컴포넌트 루트에서 'file-input-box:change' 이벤트(detail.fileId)가 발생한다.
--%>
<c:set var="fibId" value="${empty param.id ? 'fileInputBox' : param.id}" />
<c:set var="fibName" value="${empty param.name ? 'fileId' : param.name}" />
<c:set var="fibMultiple" value="${param.multiple eq 'true'}" />
<c:url var="fibUploadUrl" value="${empty param.uploadUrl ? '/cmm/file/upload.do' : param.uploadUrl}" />
<c:url var="fibDeleteUrl" value="${empty param.deleteUrl ? '/cmm/file/delete.do' : param.deleteUrl}" />
<c:url var="fibDownloadUrl" value="${empty param.downloadUrl ? '/cmm/file/download.do' : param.downloadUrl}">
    <c:if test="${not empty param.fileId}">
        <c:param name="fileId" value="${param.fileId}"/>
    </c:if>
</c:url>

<div id="<c:out value='${fibId}'/>" class="file-input-box krds-file-upload"
    data-upload-url="<c:out value='${fibUploadUrl}'/>"
    data-delete-url="<c:out value='${fibDeleteUrl}'/>"
    data-max-size="<c:out value='${empty param.maxSize ? 0 : param.maxSize}'/>"
    data-multiple="<c:out value='${fibMultiple}'/>"
    data-field-name="<c:out value='${fibName}'/>" aria-busy="false"
    role="group" aria-labelledby="<c:out value='${fibId}'/>Label">

    <label id="<c:out value='${fibId}'/>Label" for="<c:out value='${fibId}'/>Input" class="form-label | fw-semibold | upload-label"><c:out value="${empty param.label ? '첨부파일' : param.label}"/></label>

    <p id="<c:out value='${fibId}'/>Help" class="form-text upload-help">
        <c:out value="${empty param.description ? '선택한 파일은 한 번에 자동 업로드됩니다. X 버튼으로 첨부를 취소할 수 있습니다. 변경은 저장 시 반영됩니다.' : param.description}"/>
        <br>파일 형식: <c:out value="${empty param.accept ? '제한 없음' : param.accept}"/>
        <span data-role="limit"></span>
        <c:if test="${not fibMultiple}"> · 한 번에 1개 파일을 첨부할 수 있습니다.</c:if>
    </p>
    <div class="file-upload" data-role="dropzone">
        <p class="txt">첨부할 파일을 여기에 끌어다 놓거나,<br>파일 선택 버튼을 눌러 파일을 직접 선택해주세요.</p>
        
        <input type="file" class="upload-picker" data-role="picker" id="<c:out value='${fibId}'/>Input"
            ${fibMultiple ? 'multiple="multiple"' : ''} accept="<c:out value='${param.accept}'/>"
            aria-describedby="<c:out value='${fibId}'/>Help <c:out value='${fibId}'/>Status">
        <div class="upload-actions">
            <button type="button" class="btn | btn-secondary" data-role="select" aria-controls="<c:out value='${fibId}'/>Input">파일 선택</button>
        </div>
        
        <ul data-role="selection" class=" upload-selection | d-flex | flex-wrap | gap-2" role="list" aria-label="업로드 대기 파일 목록" hidden></ul>
        
        <template data-role="file-template">

            <li data-role="file" data-temp="true"
                class="file-item | border | rounded | bg-body-tertiary | d-inline-flex | align-items-center | gap-2">

                <input type="hidden" data-role="id" name="<c:out value='${fibName}'/>">

                <div class="file-name">
                    <a data-role="download" class="link-body-emphasis | text-decoration-none" download>
                        <span data-role="name"></span>
                    </a>
                    <span class="file-size" data-role="size"></span>
                </div>
                <div class="file-btn">
                    <button data-role="remove" type="button" class="btn-close | flex-shrink-0"
                        aria-label="첨부 취소"></button>
                </div>
            </li>

        </template>
        <p data-role="status" id="<c:out value='${fibId}'/>Status" class="form-text | upload-status" role="status" aria-live="polite" aria-atomic="true"></p>
    </div>
    
    <div class="file-list">
        <div class="upload-top"><span>첨부파일 <span data-role="count">0</span>개</span></div>
        <c:choose>
            <c:when test="${fibMultiple}">
                <p data-role="empty" class="upload-empty" ${empty eventFiles ? '' : 'hidden'}>첨부된 파일이 없습니다.</p>
                <ul data-role="list" class="upload-list | d-flex | flex-wrap | gap-2" role="list" aria-label="첨부파일 목록">
                    <c:forEach var="fibFile" items="${eventFiles}" varStatus="fibIndex">
                        <c:url var="fibItemDownloadUrl" value="${empty param.downloadUrl ? '/cmm/file/download.do' : param.downloadUrl}">
                            <c:param name="fileId" value="${fibFile.fileId}"/>
                        </c:url>
                        <li data-role="file" data-original="true"
                            data-file-id="<c:out value='${fibFile.fileId}'/>"
                            data-file-name="<c:out value='${fibFile.orignlFileNm}'/>"
                            data-download-url="<c:out value='${fibItemDownloadUrl}'/>" class="file-item border | rounded bg-body | d-inline-flex | align-items-center | gap-2">

                            <input type="hidden" data-role="id" name="<c:out value='${fibName}'/>" value="<c:out value='${fibFile.fileId}'/>">

                            <div class="file-name">
                                <a data-role="download" class="link-body-emphasis | text-decoration-none" href="<c:out value='${fibItemDownloadUrl}'/>" aria-describedby="<c:out value='${fibId}'/>File<c:out value='${fibIndex.index}'/>" download><span data-role="name" id="<c:out value='${fibId}'/>File<c:out value='${fibIndex.index}'/>"><c:out value="${fibFile.orignlFileNm}"/></span></a>
                                <span class="file-size" data-role="size" data-size="<c:out value='${fibFile.fileSize}'/>"></span>
                            </div>

                            <div class="file-btn">
                                <button data-role="remove" type="button" class="btn-close | flex-shrink-0" aria-label="첨부 취소" aria-describedby="<c:out value='${fibId}'/>File<c:out value='${fibIndex.index}'/>"></button>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
            </c:when>
            
            <c:otherwise>
                <input type="hidden" data-role="id" name="<c:out value='${fibName}'/>" value="<c:out value='${param.fileId}'/>">
                <div data-role="current" class="file-item | border | rounded | bg-body | d-inline-flex | align-items-center | gap-2" ${empty param.fileId ? 'hidden' : ''} role="group" aria-label="첨부파일">
                    <div class="file-name">
                        <a data-role="download" class="link-body-emphasis | text-decoration-none" href="<c:out value='${fibDownloadUrl}'/>" aria-describedby="<c:out value='${fibId}'/>FileName" download><span data-role="name" id="<c:out value='${fibId}'/>FileName"><c:out value="${empty param.fileName ? '첨부파일' : param.fileName}"/></span></a>
                        <span data-role="size" class="file-size" data-size="<c:out value='${param.fileSize}'/>"></span>
                    </div>

                    <div class="file-btn">

                        <button data-role="remove" type="button" class="btn-close | flex-shrink-0" aria-label="첨부 취소" aria-describedby="<c:out value='${fibId}'/>FileName"></button>
                    </div>
                </div>
                <p data-role="empty" class="upload-empty" ${empty param.fileId ? '' : 'hidden'}>첨부된 파일이 없습니다.</p>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<script>
(function () {
    'use strict';

    var box = document.currentScript.previousElementSibling;
    if (!box || !box.classList.contains('file-input-box')) { return; }

    function role(name) { return box.querySelector('[data-role="' + name + '"]'); }

    var form = box.closest('form');

    var commonPicker = role('picker');
    var selectButton = role('select');
    var pendingFiles = [];
    var activeBatch = null;
    var pendingSequence = 0;
    var dropzone = role('dropzone');
    var selection = role('selection');
    var count = role('count');
    var rowSequence = 0;

    function formatSize(value) {
        var bytes = Number(value);
        if (value == null || value === '' || !isFinite(bytes) || bytes < 0) { return ''; }
        if (bytes < 1024) { return bytes + ' B'; }
        if (bytes < 1048576) { return (bytes / 1024).toFixed(1) + ' KB'; }
        return (bytes / 1048576).toFixed(1) + ' MB';
    }
    var limit = Number(box.getAttribute('data-max-size'));
    if (limit > 0) { role('limit').textContent = ' · 파일당 최대 ' + formatSize(limit); }
    Array.from(box.querySelectorAll('[data-role="size"]')).forEach(function (size) {
        var formatted = formatSize(size.getAttribute('data-size'));
        size.textContent = formatted ? '(' + formatted + ')' : '';
    });
    function syncPicker() {
        var transfer = new DataTransfer();
        pendingFiles.forEach(function (entry) {
            if (!entry.cancelled) { transfer.items.add(entry.file); }
        });
        commonPicker.files = transfer.files;
    }
    function renderSelection() {
        selection.querySelectorAll('[data-pending="true"]').forEach(function (row) { row.remove(); });
        pendingFiles.filter(function (entry) { return !entry.cancelled && !entry.settled; }).forEach(function (entry) {
            var item = document.createElement('li');
            item.setAttribute('data-pending', 'true');
            item.className = 'file-item | border | rounded | bg-body | bg-body-tertiary | d-inline-flex | align-items-center | gap-2';
            var label = document.createElement('span');
            label.id = box.id + 'Pending' + entry.key;
            label.className = 'file-name';
            label.textContent = entry.file.name + ' (' + formatSize(entry.file.size) + ')';
            var cancel = document.createElement('button');
            cancel.type = 'button';
            cancel.className = 'btn-close | flex-shrink-0';
            cancel.setAttribute('data-role', 'cancel');
            cancel.setAttribute('data-key', String(entry.key));
            cancel.setAttribute('aria-label', '업로드 취소');
            cancel.setAttribute('aria-describedby', label.id);
            item.appendChild(label);
            
            if (activeBatch) {
                var spinner = document.createElement('span');
                spinner.className = 'spinner-border | spinner-border-sm | text-secondary';
                spinner.setAttribute('aria-hidden', 'true');
                item.appendChild(spinner);
            }
            
            item.appendChild(cancel);
            selection.appendChild(item);
        });
        
        selection.hidden = selection.children.length === 0;
        selectButton.disabled = commonPicker.disabled;
    }
    selection.addEventListener('click', function (event) {
        var cancel = event.target.closest('[data-role="cancel"]');
        if (!cancel) { return; }
        var entry = pendingFiles.find(function (item) { return String(item.key) === cancel.getAttribute('data-key'); });
        if (!entry) { return; }
        entry.cancelled = true;
        if (!activeBatch) {
            pendingFiles = pendingFiles.filter(function (item) { return !item.cancelled; });
            syncPicker();
        }
        renderSelection();
        role('status').textContent = entry.file.name + ' 첨부를 취소했습니다.';
        if (!selection.hidden) { selection.querySelector('[data-role="cancel"]').focus(); }
        else { selectButton.focus(); }
    });
    function accepts(file) {
        var types = commonPicker.accept.toLowerCase().split(',').map(function (type) { return type.trim(); }).filter(Boolean);
        return !types.length || types.some(function (type) {
            if (type.charAt(0) === '.') { return file.name.toLowerCase().endsWith(type); }
            if (type.endsWith('/*')) { return file.type.toLowerCase().indexOf(type.slice(0, -1)) === 0; }
            return file.type.toLowerCase() === type;
        });
    }
    selectButton.addEventListener('click', function () { commonPicker.click(); });
    function rejectSelection(message) {
        // 허용되지 않은 파일을 input과 대기 목록 모두에서 제거한다.
        commonPicker.value = '';
        pendingFiles = [];
        renderSelection();
        var status = role('status');
        status.textContent = message;
        status.classList.add('text-danger');
        status.classList.remove('text-body-secondary');
    }
    commonPicker.addEventListener('change', function () {
        if (commonPicker.disabled) { return; }
        var rejectedNames = Array.from(commonPicker.files).filter(function (file) {
            return !accepts(file);
        }).map(function (file) { return file.name; });
        if (rejectedNames.length) {
            rejectSelection('허용되지 않는 파일 형식입니다: ' + rejectedNames.join(', '));
            return;
        }
        pendingFiles = Array.from(commonPicker.files).map(function (file) {
            return {key: ++pendingSequence, file: file, cancelled: false};
        });
        renderSelection();
    });
    var dragDepth = 0;
    dropzone.addEventListener('dragenter', function (event) {
        event.preventDefault();
        dragDepth++;
        if (!commonPicker.disabled) { dropzone.classList.add('is-dragover'); }
    });
    dropzone.addEventListener('dragover', function (event) {
        event.preventDefault();
        if (event.dataTransfer) { event.dataTransfer.dropEffect = commonPicker.disabled ? 'none' : 'copy'; }
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
        if (commonPicker.disabled || !event.dataTransfer || !event.dataTransfer.files.length) { return; }
        if (!commonPicker.multiple && event.dataTransfer.files.length > 1) {
            role('status').textContent = '한 번에 1개 파일만 선택해주세요.';
            role('status').classList.add('text-danger');
            return;
        }
        commonPicker.files = event.dataTransfer.files;
        commonPicker.dispatchEvent(new Event('change', {bubbles: true}));
    });

    if (box.getAttribute('data-multiple') === 'true') {
        var list = role('list');
        var pickerMultiple = role('picker');
        var emptyMultiple = role('empty');
        var statusMultiple = role('status');
        var maxSizeMultiple = Number(box.getAttribute('data-max-size'));
        var busyMultiple = false;
        var lockedMultiple = [];

        function tellMultiple(message, isError) {
            statusMultiple.textContent = message || '';
            statusMultiple.classList.toggle('text-danger', !!isError);
            statusMultiple.classList.toggle('text-body-secondary', !isError);
        }

        function renderMultiple() {
            renderSelection();
            var fileRows = list.querySelectorAll('[data-role="file"]');
            var tempRows = selection.querySelectorAll('[data-temp="true"]');
            count.textContent = fileRows.length;
            emptyMultiple.hidden = fileRows.length > 0;
            emptyMultiple.classList.toggle('d-none', fileRows.length > 0);
            Array.prototype.forEach.call(Array.from(fileRows).concat(Array.from(tempRows)), function (row) {
                row.querySelector('[data-role="remove"]').disabled = busyMultiple;
            });
        }

        function setBusyMultiple(value) {
            busyMultiple = value;
            box.setAttribute('aria-busy', String(value));
            pickerMultiple.disabled = value;
            if (value) {
                var selector = 'button[type="submit"]';
                var submitButtons = form
                    ? Array.from(form.querySelectorAll(selector))
                        .concat(form.id ? Array.from(document.querySelectorAll(selector + '[form="' + form.id + '"]')) : [])
                    : [];
                lockedMultiple = submitButtons.map(function (button) {
                    var wasDisabled = button.disabled;
                    button.disabled = true;
                    return {button: button, wasDisabled: wasDisabled};
                });
            } else {
                lockedMultiple.forEach(function (item) { item.button.disabled = item.wasDisabled; });
                lockedMultiple = [];
            }
            renderMultiple();
        }

        function messageMultiple(result, fallback) {
            var messages = [];
            if (result && result.code && result.code.message) { messages.push(result.code.message); }
            var fields = result && result.error && result.error.fields || [];
            fields.forEach(function (field) {
                if (field.message && messages.indexOf(field.message) < 0) { messages.push(field.message); }
            });
            return messages.length ? messages.join(' ') : fallback;
        }

        async function requestMultiple(url, body) {
            var response = await fetch(url, {method: 'POST', body: body});
            var result;
            try { result = await response.json(); }
            catch (parseError) { throw new Error('서버 응답을 확인할 수 없습니다. 잠시 후 다시 시도해주세요.'); }
            if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS') {
                throw new Error(messageMultiple(result, '파일 처리에 실패했습니다.'));
            }
            return result.data;
        }

        function createFileRow(file) {
            var template = role('file-template');
            var row = template.content.firstElementChild.cloneNode(true);
            row.setAttribute('data-file-id', String(file.fileId));
            row.setAttribute('data-file-name', file.originalName);
            row.setAttribute('data-download-url', file.downloadUrl);
            row.querySelector('[data-role="id"]').value = String(file.fileId);
            var fileName = row.querySelector('[data-role="name"]');
            fileName.id = box.id + 'UploadedFile' + (++rowSequence);
            fileName.textContent = file.originalName;
            var downloadLink = row.querySelector('[data-role="download"]');
            downloadLink.href = file.downloadUrl;
            downloadLink.setAttribute('aria-describedby', fileName.id);
            var formattedSize = formatSize(file.fileSize);
            row.querySelector('[data-role="size"]').textContent = formattedSize ? '(' + formattedSize + ')' : '';
            row.querySelector('[data-role="remove"]').setAttribute('aria-describedby', fileName.id);
            selection.appendChild(row);
            renderMultiple();
        }

        async function removeFileRow(row) {
            if (busyMultiple) { return; }
            if (row.getAttribute('data-original') !== 'true') {
                setBusyMultiple(true);
                try {
                    var body = new URLSearchParams();
                    body.set('fileId', row.getAttribute('data-file-id'));
                    await requestMultiple(box.getAttribute('data-delete-url'), body);
                } catch (error) {
                    tellMultiple(error.message || '임시 첨부 파일을 정리하지 못했습니다.', true);
                    setBusyMultiple(false);
                    return;
                }
                setBusyMultiple(false);
            }
            row.remove();
            renderMultiple();
            tellMultiple('첨부를 제거했습니다. 저장 버튼을 눌러주세요.');
        }

        pickerMultiple.addEventListener('change', function () {
            renderMultiple();
            if (pickerMultiple.files.length) { uploadMultipleFiles(); }
        });
        async function uploadMultipleFiles() {
            if (busyMultiple || !pickerMultiple.files.length) { return; }
            var selectedFiles = Array.from(pickerMultiple.files);
            for (var i = 0; i < selectedFiles.length; i++) {
                if (!accepts(selectedFiles[i])) {
                    rejectSelection('허용되지 않는 파일 형식입니다: ' + selectedFiles[i].name);
                    return;
                }
                if (selectedFiles[i].size === 0 || (maxSizeMultiple > 0 && selectedFiles[i].size > maxSizeMultiple)) {
                    tellMultiple(selectedFiles[i].size === 0
                        ? '빈 파일은 업로드할 수 없습니다.' : '파일 크기 제한을 초과했습니다.', true);
                    return;
                }
            }
            activeBatch = pendingFiles.slice();
            setBusyMultiple(true);
            tellMultiple('선택한 파일을 한 번에 업로드하고 있습니다.');
            try {
                var body = new FormData();
                selectedFiles.forEach(function (file) { body.append('files', file); });
                var data = await requestMultiple(box.getAttribute('data-upload-url'), body);
                if (!Array.isArray(data) || data.length !== selectedFiles.length
                        || data.some(function (file) {
                            return !file || !/^[1-9]\d*$/.test(String(file.fileId)) || !file.downloadUrl;
                        })) {
                    throw new Error('업로드한 파일 정보를 확인할 수 없습니다.');
                }
                var cleanupFailed = false;
                for (var index = 0; index < data.length; index++) {
                    if (activeBatch[index].cancelled) {
                        try {
                            var cleanupBody = new URLSearchParams();
                            cleanupBody.set('fileId', String(data[index].fileId));
                            await requestMultiple(box.getAttribute('data-delete-url'), cleanupBody);
                        } catch (cleanupError) {
                            createFileRow(data[index]);
                            cleanupFailed = true;
                        }
                    } else { createFileRow(data[index]); }
                    activeBatch[index].settled = true;
                    renderSelection();
                }
                pendingFiles = [];
                pickerMultiple.value = '';
                tellMultiple(cleanupFailed
                    ? '일부 첨부 취소에 실패했습니다. 파일 옆 X 버튼으로 다시 취소해주세요.'
                    : '파일 처리가 완료되었습니다. 하단의 저장 버튼을 눌러 첨부를 반영해주세요.', cleanupFailed);
            } catch (error) {
                pendingFiles = pendingFiles.filter(function (entry) { return !entry.cancelled; });
                syncPicker();
                tellMultiple(error.message || '파일 업로드에 실패했습니다. 파일 선택 버튼으로 다시 선택하거나 X 버튼으로 취소해주세요.', true);
            } finally {
                activeBatch = null;
                setBusyMultiple(false);
            }
        }

        function handleAttachmentRemove(event) {
            var button = event.target.closest('[data-role="remove"]');
            if (!button || busyMultiple) { return; }
            var row = button.closest('[data-role="file"]');
            if (!row) { return; }
            if (row.getAttribute('data-original') !== 'true') {
                removeFileRow(row);
                return;
            }
            if (window.CmpDialog) {
                CmpDialog.open({
                    title: '첨부 제거',
                    text: '이 글에서 첨부를 제거하시겠습니까? 저장 버튼을 누르면 반영됩니다.',
                    okText: '제거',
                    cancelText: '취소',
                    okButton: function () {
                        CmpDialog.close();
                        removeFileRow(row);
                    }
                });
            } else if (window.confirm('첨부를 제거하시겠습니까?')) {
                removeFileRow(row);
            }
        }
        // 현재 화면의 첨부를 비우며 새로 업로드한 임시 파일만 서버에서 정리한다.
        box.reset = async function () {
            if (busyMultiple) { throw new Error('첨부파일 처리가 끝난 후 초기화해주세요.'); }
            setBusyMultiple(true);
            try {
                var rows = Array.from(list.querySelectorAll('[data-role="file"]'))
                    .concat(Array.from(selection.querySelectorAll('[data-role="file"]')));
                for (var row of rows) {
                    if (row.getAttribute('data-original') !== 'true') {
                        var body = new URLSearchParams();
                        body.set('fileId', row.getAttribute('data-file-id'));
                        await requestMultiple(box.getAttribute('data-delete-url'), body);
                    }
                    row.remove();
                }
                pendingFiles = [];
                pickerMultiple.value = '';
                tellMultiple('');
            } finally {
                setBusyMultiple(false);
            }
        };

        list.addEventListener('click', handleAttachmentRemove);
        selection.addEventListener('click', handleAttachmentRemove);

        if (form) {
            form.addEventListener('submit', function (event) {
                if (busyMultiple || pickerMultiple.files.length) {
                    event.preventDefault();
                    event.stopImmediatePropagation();
                    tellMultiple(busyMultiple ? '파일 처리가 끝난 후 저장해주세요.' : '업로드하지 못한 파일을 파일 선택 버튼으로 다시 선택하거나 X 버튼으로 취소해주세요.', true);
                }
            }, true);
        }
        renderMultiple();
        return;
    }

    var idInput = role('id');
    var picker = role('picker');
    var removeButton = role('remove');
    var undoButton = role('undo');
    var currentRow = role('current');
    var emptyRow = role('empty');
    var name = role('name');
    var download = role('download');
    var status = role('status');
    // The undo button is optional; validate the required elements before binding any listener.
    if (!idInput || !picker || !removeButton || !currentRow || !emptyRow || !name || !download || !status) {
        if (status) { status.textContent = '첨부파일 화면 구성을 확인해주세요.'; }
        console.error('CmpFileInputBox: required elements are missing.');
        return;
    }
    var maxSize = Number(box.getAttribute('data-max-size'));
    var original = idInput.value ? {
        fileId: idInput.value,
        originalName: name.textContent,
        fileSize: role('size').getAttribute('data-size'),
        downloadUrl: download.getAttribute('href')
    } : null;
    var savedRowParent = currentRow.parentNode;
    var savedRowNext = currentRow.nextSibling;
    var singleTempRow = null;
    var current = original;
    var busy = false;
    var lockedButtons = [];

    function tell(message, isError) {
        status.textContent = message || '';
        status.classList.toggle('text-danger', !!isError);
        status.classList.toggle('text-body-secondary', !isError);
    }

    function render() {
        renderSelection();
        var temporary = isDraft(current);
        if (temporary) {
            if (!singleTempRow) {
                singleTempRow = document.createElement('li');
                singleTempRow.setAttribute('data-temp', 'true');
                singleTempRow.appendChild(currentRow);
                selection.appendChild(singleTempRow);
            }
        } else if (singleTempRow) {
            savedRowParent.insertBefore(currentRow, savedRowNext);
            singleTempRow.remove();
            singleTempRow = null;
        }
        selection.hidden = selection.children.length === 0;
        count.textContent = current && !temporary ? '1' : '0';
        var currentSize = current ? formatSize(current.fileSize) : '';
        role('size').textContent = currentSize ? '(' + currentSize + ')' : '';
        var previousValue = idInput.value;
        idInput.value = current ? String(current.fileId) : '';
        currentRow.hidden = !current;
        currentRow.classList.toggle('d-none', !current);
        emptyRow.hidden = !!current && !temporary;
        emptyRow.classList.toggle('d-none', !!current && !temporary);
        name.textContent = current ? current.originalName : '';
        if (current) { download.setAttribute('href', current.downloadUrl); }
        else { download.removeAttribute('href'); }
        removeButton.disabled = busy || !current;
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
        render();
        if (picker.files.length) { uploadSingleFile(); }
    });

    async function uploadSingleFile() {
        if (busy || !picker.files.length) { return; }
        var file = picker.files[0];
        if (!accepts(file)) {
            rejectSelection('허용되지 않는 파일 형식입니다: ' + file.name);
            return;
        }
        if (file.size === 0 || (maxSize > 0 && file.size > maxSize)) {
            tell(file.size === 0 ? '빈 파일은 업로드할 수 없습니다.' : '파일 크기 제한을 초과했습니다.', true);
            return;
        }
        var previous = current;
        activeBatch = pendingFiles.slice();
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
            if (activeBatch[0].cancelled) {
                // The batch request already reached the server; remove only its new draft.
                try { await deleteDraft(data[0]); }
                catch (cancelError) {
                    current = data[0];
                    pendingFiles = [];
                    picker.value = '';
                    tell('첨부 취소에 실패했습니다. 파일 옆 X 버튼으로 다시 취소해주세요.', true);
                    return;
                }
                pendingFiles = [];
                picker.value = '';
                tell('첨부를 취소했습니다.');
                return;
            }
            current = data[0];
            pendingFiles = [];
            picker.value = '';
            render();
            tell('업로드했습니다. 하단의 저장 버튼을 눌러 첨부를 반영해주세요.');
            try { await deleteDraft(previous); }
            catch (cleanupError) {
                tell('업로드했습니다. 저장 버튼을 눌러주세요. 이전 임시 파일 정리는 실패했습니다.', true);
            }
        } catch (error) {
            pendingFiles = pendingFiles.filter(function (entry) { return !entry.cancelled; });
            syncPicker();
            tell(error.message || '파일 업로드에 실패했습니다. 파일 선택 버튼으로 다시 선택하거나 X 버튼으로 취소해주세요.', true);
        } finally {
            activeBatch = null;
            setBusy(false);
        }
    }

    removeButton.addEventListener('click', function () {
        if (busy || !current) { return; }
        var removeCurrent = async function () {
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
        };
        if (isDraft(current)) { removeCurrent(); }
        else { confirmRemove(removeCurrent); }
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
                tell(busy ? '파일 처리가 끝난 후 저장해주세요.' : '업로드하지 못한 파일을 파일 선택 버튼으로 다시 선택하거나 X 버튼으로 취소해주세요.', true);
            }
        }, true);
    }

    render();
})();
</script>
