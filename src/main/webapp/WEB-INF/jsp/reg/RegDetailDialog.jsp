<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- RegCreateDialog에서 등록한 validateRegDetailVO와 ErrMessageMapper를 함께 사용한다. --%>

<div class="modal | fade" id="regDetailModal" tabindex="-1" aria-labelledby="regDetailTitle" aria-hidden="true"
     data-detail-url="<c:url value='/region/detail.do'/>">
    <div class="modal-dialog | modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title | fs-5" id="regDetailTitle">행정구역 상세</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            <form id="regDetailForm" name="regDetailVO" action="<c:url value='/region/update.do'/>" method="post" novalidate>
                <input type="hidden" id="regDetailRid" name="rid">
                <div class="modal-body">
                    <p id="regDetailLoading" class="text-body-secondary | d-none" role="status">행정구역 정보를 불러오는 중입니다.</p>
                    <fieldset id="regDetailFields" disabled>
                        <div class="mb-3">
                            <label for="regDetailParentRegId" class="form-label">상위 행정구역</label>
                            <select id="regDetailParentRegId" name="parentRegId" class="form-select" aria-describedby="regDetailParentError">
                                <option value="">최상위 행정구역</option>
                                <c:forEach var="rootReg" items="${parentRegs}">
                                    <option value="${rootReg.rid}"><c:out value="${rootReg.name}"/> (<c:out value="${rootReg.code}"/>)</option>
                                </c:forEach>
                            </select>
                            <div id="regDetailParentError" data-error-for="parentRegId" class="invalid-feedback" style="white-space: pre-line;"></div>
                        </div>
                        <div class="mb-3">
                            <label for="regDetailName" class="form-label">행정구역명</label>
                            <input id="regDetailName" name="name" type="text" class="form-control" maxlength="100" autocomplete="off" aria-describedby="regDetailNameError">
                            <div id="regDetailNameError" data-error-for="name" class="invalid-feedback" style="white-space: pre-line;"></div>
                        </div>
                        <div class="mb-3">
                            <label for="regDetailValue" class="form-label">코드</label>
                            <input id="regDetailValue" name="code" type="text" class="form-control" maxlength="20" autocomplete="off" aria-describedby="regDetailValueError">
                            <div id="regDetailValueError" data-error-for="code" class="invalid-feedback" style="white-space: pre-line;"></div>
                        </div>
                        <div class="mb-3">
                            <label for="regDetailGeom" class="form-label">영역 (EWKT)</label>
                            <textarea id="regDetailGeom" name="geom" class="form-control" rows="3" aria-describedby="regDetailGeomError"></textarea>
                            <div id="regDetailGeomError" data-error-for="geom" class="invalid-feedback" style="white-space: pre-line;"></div>
                        </div>
                        <div>
                            <label for="regDetailStatus" class="form-label">상태</label>
                            <select id="regDetailStatus" name="status" class="form-select" aria-describedby="regDetailStatusError">
                                <option value="Y">사용</option>
                                <option value="N">미사용</option>
                            </select>
                            <div id="regDetailStatusError" data-error-for="status" class="invalid-feedback" style="white-space: pre-line;"></div>
                        </div>
                    </fieldset>
                    <div id="regDetailError" class="text-danger mt-3 d-none" role="alert" aria-live="polite" style="white-space: pre-line;"></div>
                </div>
                <div class="modal-footer">
                    <button id="regDetailDelete" type="button" class="btn | btn-outline-danger | me-auto" disabled>
                        <span id="regDetailDeleteSpinner" class="spinner-border | spinner-border-sm | d-none" aria-hidden="true"></span>
                        <span id="regDetailDeleteLabel">삭제</span>
                    </button>
                    <button type="button" class="btn | btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                    <button id="regDetailSubmit" type="submit" class="btn | btn-primary" disabled>
                        <span id="regDetailSubmitSpinner" class="spinner-border | spinner-border-sm | d-none" aria-hidden="true"></span>
                        <span id="regDetailSubmitLabel">수정</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
(function () {
    'use strict';

    var modalElement = document.getElementById('regDetailModal');
    var form = document.getElementById('regDetailForm');
    var fields = document.getElementById('regDetailFields');

    var idInput = document.getElementById('regDetailRid');
    var parentInput = document.getElementById('regDetailParentRegId');
    var nameInput = document.getElementById('regDetailName');
    var regInput = document.getElementById('regDetailValue');
    var geomInput = document.getElementById('regDetailGeom');
    var statusInput = document.getElementById('regDetailStatus');

    var submitButton = document.getElementById('regDetailSubmit');
    var submitSpinner = document.getElementById('regDetailSubmitSpinner');
    var submitLabel = document.getElementById('regDetailSubmitLabel');
    var deleteButton = document.getElementById('regDetailDelete');
    var deleteSpinner = document.getElementById('regDetailDeleteSpinner');
    var deleteLabel = document.getElementById('regDetailDeleteLabel');

    var loading = document.getElementById('regDetailLoading');
    
    var errorMapper = ErrMessageMapper.create(form, document.getElementById('regDetailError'), {
        popup: function (message) {
            openDialog(
                '오류',
                escapeHtml(message).replace(/\r?\n/g, '<br>'),
                function () {
                    CmpDialog.close();
                },
                true,
                '확인'
            );
        }
    });
    var modal = coreui.Modal.getOrCreateInstance(modalElement);
    var isSubmitting = false;
    var isLoading = false;
    var isConfirming = false;
    var allowModalClose = false;
    var initialValues = '';
    var loadVersion = 0;

    // ----- 공통 유틸 ----- //

    function escapeHtml(text) {
        return String(text).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function openDialog(title, text, okHandler, isOneButton, okText, cancelText) {
        CmpDialog.open({
            title: title,
            text: text,
            okButton: okHandler,
            isOneButton: !!isOneButton,
            okText: okText,
            cancelText: cancelText
        });
    }

    function getValues() {
        return JSON.stringify([parentInput.value, nameInput.value, regInput.value, geomInput.value, statusInput.value]);
    }

    async function requestJson(url, options) {
        var controller = new AbortController();
        var timer = setTimeout(function () { controller.abort(); }, 30000);
        try {
            options = options || {};
            options.signal = controller.signal;
            var response = await fetch(url, options);
            var result = await response.json();
            return {ok: response.ok && result.code && result.code.resultCode === 'SUCCESS', result: result};
        } catch (error) {
            throw new Error(error.name === 'AbortError' ? '응답 시간이 초과되었습니다. 목록을 새로고침해 처리 결과를 확인해주세요.' : '서버와 통신하지 못했습니다. 다시 시도해주세요.');
        } finally { clearTimeout(timer); }
    }

    function resetForm() {
        form.reset();
        idInput.value = '';
        initialValues = '';
        errorMapper.clear();
    }

    var loadedParentOption = null;

    function fillForm(data) {
        var parentId = data.parentRegId == null ? '' : String(data.parentRegId);
        idInput.value = String(data.rid);
        if (loadedParentOption) {
            loadedParentOption.remove();
            loadedParentOption = null;
        }
        Array.from(parentInput.options).forEach(function (option) { option.disabled = option.value === String(data.rid); });
        if (parentId && !Array.from(parentInput.options).some(function (option) { return option.value === parentId; })) {
            loadedParentOption = document.createElement('option');
            loadedParentOption.value = parentId;
            loadedParentOption.textContent = (data.parentName || '상위 행정구역') + ' (' + (data.parentCode || parentId) + ')';
            parentInput.appendChild(loadedParentOption);
        }
        parentInput.value = parentId;
        nameInput.value = data.name || '';
        regInput.value = data.code || '';
        geomInput.value = data.geom || '';
        statusInput.value = data.status;
        initialValues = getValues();
    }

    // ----- 요청 ----- //

    async function  fetchRegDetail(rid) {
        if (isSubmitting || isConfirming) { return; }
        var version = ++loadVersion;
        resetForm();
        isLoading = true;
        loading.classList.remove('d-none');
        submitButton.disabled = true;
        deleteButton.disabled = true;
        fields.disabled = true;
        modal.show();

        try {
            var response = await requestJson(modalElement.getAttribute('data-detail-url') + '?rid=' + encodeURIComponent(rid));
            if (version !== loadVersion) { return; }
            if (!response.ok) { errorMapper.showResponse(response.result); return; }
            fillForm(response.result.data);

        } catch (error) {
            if (version === loadVersion) { errorMapper.setFormError(error.message); }

        } finally {
            if (version === loadVersion) {
                isLoading = false;
                loading.classList.add('d-none');
                submitButton.disabled = !idInput.value;
                deleteButton.disabled = !idInput.value;
                fields.disabled = !idInput.value;
            }
        }
    }

    async function fetchRegUpdate() {
        // FormData is captured before disabling the fieldset, so all edit values are included.
        var body = new URLSearchParams(new FormData(form));
        isSubmitting = true;
        errorMapper.clear();
        submitButton.disabled = true;
        deleteButton.disabled = true;
        fields.disabled = true;
        submitSpinner.classList.remove('d-none');
        submitLabel.textContent = '처리 중';
        try {
            var response = await requestJson(form.action, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                body: body
            });
            if (!response.ok) { errorMapper.showResponse(response.result); return; }
            window.location.reload();
        } catch (error) {
            errorMapper.setFormError(error.message);
        } finally {
            isSubmitting = false;
            submitButton.disabled = false;
            deleteButton.disabled = false;
            fields.disabled = false;
            submitSpinner.classList.add('d-none');
            submitLabel.textContent = '수정';
        }
    }

    function fetchRegDelete(rid) {
        var deleteForm = document.getElementById('regDeleteForm');
        deleteForm.elements.rid.value = rid;
        isSubmitting = true;
        submitButton.disabled = true;
        deleteButton.disabled = true;
        fields.disabled = true;
        deleteSpinner.classList.remove('d-none');
        deleteLabel.textContent = '처리 중';
        deleteForm.submit();
    }

    // ----- 이벤트 핸들러 ----- //

    function handleClickBtn(action, value) {
        switch (action) {
            case 'edit':
                 fetchRegDetail(value);
                break;

            case 'delete': {
                var deleteId = value || idInput.value;
                if (isSubmitting || isLoading || isConfirming || !deleteId) { return; }
                isConfirming = true;
                openDialog(
                    '삭제 확인',
                    '이 행정구역을 삭제하시겠습니까?',
                    function () {
                        if (isSubmitting) { return; }
                        isConfirming = false;
                        CmpDialog.close();
                        fetchRegDelete(deleteId);
                    },
                    false,
                    '삭제',
                    '취소'
                );
                break;
            }

            case 'update':
                if (isSubmitting || isLoading || isConfirming || !idInput.value) { return; }
                isConfirming = true;
                openDialog(
                    '수정 확인',
                    '행정구역을 수정하시겠습니까?',
                    function () {
                        if (isSubmitting) { return; }
                        isConfirming = false;
                        CmpDialog.close();
                        fetchRegUpdate();
                    },
                    false,
                    '수정',
                    '취소'
                );
                break;

            case 'closeConfirm':
                openDialog(
                    '닫기 확인',
                    '입력한 변경 내용을 저장하지 않고 닫을까요?',
                    function () {
                        allowModalClose = true;
                        CmpDialog.close();
                        modal.hide();
                        allowModalClose = false;
                    },
                    false,
                    '닫기',
                    '계속 작성'
                );
                break;
        }
    }

    function handleSubmitForm(event) {
        event.preventDefault();
        if (isSubmitting || isLoading || isConfirming || !idInput.value) { return; }
        nameInput.value = nameInput.value.trim();
        regInput.value = regInput.value.trim();
        errorMapper.clear();
        if (!validateRegDetailVO(form)) { return; }
        handleClickBtn('update');
    }

    function handleInput() {
        errorMapper.clear();
    }

    function handleHideModal(event) {
        if (isSubmitting || isConfirming) { event.preventDefault(); return; }
        if (!allowModalClose && initialValues && getValues() !== initialValues) {
            event.preventDefault();
            handleClickBtn('closeConfirm');
        }
    }

    function handleHiddenModal() {
        ++loadVersion;
        isLoading = false;
        resetForm();
        submitButton.disabled = true;
        deleteButton.disabled = true;
        fields.disabled = true;
    }

    function handleHiddenConfirmDialog() {
        isConfirming = false;
    }

    function handleClickTable(event) {
        var button = event.target.closest('button[data-reg-action]');
        if (!button || !event.currentTarget.contains(button)) { return; }
        handleClickBtn(button.getAttribute('data-reg-action'), button.getAttribute('data-reg-id'));
    }

    // ----- 초기화 ----- //

    form.onValidationErrors = errorMapper.showValidationErrors;
    form.addEventListener('input', handleInput);
    form.addEventListener('change', handleInput);
    form.addEventListener('submit', handleSubmitForm);
    deleteButton.addEventListener('click', function () { handleClickBtn('delete'); });
    var confirmDialog = document.getElementById('cmpYNDialog');
    if (confirmDialog) { confirmDialog.addEventListener('hidden.coreui.modal', handleHiddenConfirmDialog); }
    modalElement.addEventListener('hide.coreui.modal', handleHideModal);
    modalElement.addEventListener('hidden.coreui.modal', handleHiddenModal);
    var table = document.getElementById('regListTable');
    if (table) { table.addEventListener('click', handleClickTable); }
})();
</script>
