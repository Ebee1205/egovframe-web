<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="validator" uri="http://www.springmodules.org/tags/commons-validator"%>

<script src="<c:url value='/resources/js/util/ErrMessageMapper.js'/>"></script>
<validator:javascript formName="regDetailVO" method="validateRegDetailVO"
                      staticJavascript="true" dynamicJavascript="true" xhtml="true" cdata="false"/>

<div class="modal fade" id="regCreateModal" tabindex="-1" aria-labelledby="regCreateTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="regCreateTitle">신규 행정구역 생성</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            <form id="regCreateForm" name="regDetailVO" action="<c:url value='/region/insert.do'/>" method="post" novalidate>
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="regParentRegId" class="form-label">상위 행정구역</label>
                        <select id="regParentRegId" name="parentRegId" class="form-select" aria-describedby="regParentRegIdError">
                            <option value="">최상위 행정구역</option>
                            <c:forEach var="rootReg" items="${parentRegs}">
                                <option value="${rootReg.reg_id}"><c:out value="${rootReg.name}"/> (<c:out value="${rootReg.code}"/>)</option>
                            </c:forEach>
                        </select>
                        <div id="regParentRegIdError" data-error-for="parentRegId" class="invalid-feedback" style="white-space: pre-line;"></div>
                    </div>
                    <div class="mb-3">
                        <label for="regName" class="form-label">행정구역명</label>
                        <input id="regName" name="name" type="text" class="form-control" maxlength="100" autocomplete="off" aria-describedby="regNameError">
                        <div id="regNameError" data-error-for="name" class="invalid-feedback" style="white-space: pre-line;"></div>
                    </div>
                    <div>
                        <label for="regValue" class="form-label">코드</label>
                        <input id="regValue" name="code" type="text" class="form-control" maxlength="20" autocomplete="off" aria-describedby="regValueError">
                        <div id="regValueError" data-error-for="code" class="invalid-feedback" style="white-space: pre-line;"></div>
                    </div>
                    <div id="regCreateError" class="text-danger mt-3 d-none" role="alert"
                         aria-live="polite" style="white-space: pre-line;"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">취소</button>
                    <button id="regCreateSubmit" type="submit" class="btn btn-primary">
                        <span class="spinner-border spinner-border-sm d-none" aria-hidden="true"></span>
                        <span class="button-label">생성</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
(function () {
    var modalElement = document.getElementById('regCreateModal');
    var form = document.getElementById('regCreateForm');
    var submitButton = document.getElementById('regCreateSubmit');
    var spinner = submitButton.querySelector('.spinner-border');
    var buttonLabel = submitButton.querySelector('.button-label');
    var nameInput = document.getElementById('regName');
    var regInput = document.getElementById('regValue');
    var formError = document.getElementById('regCreateError');
    var errorMapper = ErrMessageMapper.create(form, formError);
    var modal = coreui.Modal.getOrCreateInstance(modalElement);
    var isSubmitting = false;
    var isConfirmingCreate = false;
    var allowModalClose = false;
    var confirmDialogElement = document.getElementById('cmpYNDialog');

    document.getElementById('openRegCreate').addEventListener('click', function () {
        modal.show();
    });

    modalElement.addEventListener('hide.coreui.modal', function (event) {
        if (isSubmitting || isConfirmingCreate) {
            event.preventDefault();
            return;
        }

        if (!allowModalClose) {
            event.preventDefault();
            CmpDialog.open({
                title: '닫기 확인',
                text: '정말 닫을까요? 입력한 내용은 저장되지 않습니다.',
                okText: '닫기',
                cancelText: '계속 작성',
                okButton: function () {
                    allowModalClose = true;
                    CmpDialog.close();
                    modal.hide();
                    allowModalClose = false;
                }
            });
        }
    });

    if (confirmDialogElement) {
        confirmDialogElement.addEventListener('hidden.coreui.modal', function () {
            isConfirmingCreate = false;
        });
    }

    modalElement.addEventListener('hidden.coreui.modal', function () {
        form.reset();
        errorMapper.clear();
        submitButton.disabled = false;
        spinner.classList.add('d-none');
        buttonLabel.textContent = '생성';
    });

    function validateForm() {
        nameInput.value = nameInput.value.trim();
        regInput.value = regInput.value.trim();
        errorMapper.clear();
        return validateRegDetailVO(form);
    }

    form.onValidationErrors = errorMapper.showValidationErrors;

    form.addEventListener('input', errorMapper.clear);

    async function createReg() {
        try {
            isSubmitting = true;
            submitButton.disabled = true;
            spinner.classList.remove('d-none');
            buttonLabel.textContent = '생성 중';

            var requestBody = new URLSearchParams(new FormData(form));
            requestBody.set('name', nameInput.value.trim());
            requestBody.set('code', regInput.value.trim());
            var response = await fetch(form.action, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: requestBody
            });
            var result = await response.json();

            if (!response.ok) {
                errorMapper.showResponse(result);
                return;
            }

            window.location.reload();
        } catch (error) {
            errorMapper.setFormError(error.message || '코드 생성 중 오류가 발생했습니다.');
        } finally {
            isSubmitting = false;
            submitButton.disabled = false;
            spinner.classList.add('d-none');
            buttonLabel.textContent = '생성';
        }
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        if (isSubmitting || isConfirmingCreate) { return; }
        if (!validateForm()) { return; }

        isConfirmingCreate = true;
        CmpDialog.open({
            title: '생성 확인',
            text: '새 행정구역을 생성하시겠습니까?',
            okText: '생성',
            cancelText: '취소',
            okButton: function () {
                isConfirmingCreate = false;
                CmpDialog.close();
                createReg();
            }
        });
    });
})();
</script>
