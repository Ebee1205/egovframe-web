<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="validator" uri="http://www.springmodules.org/tags/commons-validator"%>

<script src="<c:url value='/validator.do'/>"></script>
<validator:javascript formName="codeDetailVO" method="validateCodeDetailVO"
                      staticJavascript="false" dynamicJavascript="true" xhtml="true" cdata="false"/>

<div class="modal fade" id="codeCreateModal" tabindex="-1" aria-labelledby="codeCreateTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="codeCreateTitle">신규 코드 생성</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            <form id="codeCreateForm" name="codeDetailVO" action="<c:url value='/code/insert.do'/>" method="post" novalidate>
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="codeParentCid" class="form-label">부모 코드</label>
                        <select id="codeParentCid" name="parentCid" class="form-select">
                            <option value="">최상위 코드</option>
                            <c:forEach var="rootCode" items="${rootCodes}">
                                <option value="${rootCode.cid}"><c:out value="${rootCode.name}"/> (<c:out value="${rootCode.code}"/>)</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="codeName" class="form-label">코드명</label>
                        <input id="codeName" name="name" type="text" class="form-control" maxlength="100" autocomplete="off" aria-describedby="codeNameError">
                        <div id="codeNameError" class="invalid-feedback" style="white-space: pre-line;"></div>
                    </div>
                    <div>
                        <label for="codeValue" class="form-label">코드</label>
                        <input id="codeValue" name="code" type="text" class="form-control" maxlength="50" autocomplete="off" aria-describedby="codeValueError">
                        <div id="codeValueError" class="invalid-feedback" style="white-space: pre-line;"></div>
                    </div>
                    <div id="codeCreateError" class="text-danger mt-3 d-none" role="alert"
                         aria-live="polite" style="white-space: pre-line;"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">취소</button>
                    <button id="codeCreateSubmit" type="submit" class="btn btn-primary">
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
    var modalElement = document.getElementById('codeCreateModal');
    var form = document.getElementById('codeCreateForm');
    var submitButton = document.getElementById('codeCreateSubmit');
    var spinner = submitButton.querySelector('.spinner-border');
    var buttonLabel = submitButton.querySelector('.button-label');
    var nameInput = document.getElementById('codeName');
    var codeInput = document.getElementById('codeValue');
    var formError = document.getElementById('codeCreateError');
    var modal = coreui.Modal.getOrCreateInstance(modalElement);
    var isSubmitting = false;

    document.getElementById('openCodeCreate').addEventListener('click', function () {
        modal.show();
    });

    function setFieldError(input, message) {
        input.classList.toggle('is-invalid', !!message);
        document.getElementById(input.id + 'Error').textContent = message || '';
    }

    function setFormError(message) {
        formError.textContent = message || '';
        formError.classList.toggle('d-none', !message);
    }

    function clearErrors() {
        setFieldError(nameInput, '');
        setFieldError(codeInput, '');
        setFormError('');
    }

    // 필드명을 알 수 없는 메시지(예: 부모 코드)는 "코드명"/"코드" 접두사로 구분해 해당 입력란 하단에 표시한다.
    function showClientMessages(messages) {
        clearErrors();
        messages.forEach(function (message) {
            if (message.indexOf('코드명') === 0) {
                setFieldError(nameInput, message);
            } else if (message.indexOf('코드') === 0) {
                setFieldError(codeInput, message);
            } else {
                setFormError(message);
            }
        });
    }

    // 서버 응답은 "필드명:메시지" 줄 단위 목록으로 내려오며, 해당 입력란 하단에 매핑해 표시한다.
    function showServerErrors(responseText) {
        clearErrors();
        responseText.split('\n').forEach(function (line) {
            var separatorIndex = line.indexOf(':');
            if (separatorIndex < 0) {
                setFormError(line);
                return;
            }
            var field = line.substring(0, separatorIndex);
            var message = line.substring(separatorIndex + 1);
            if (field === 'name') {
                setFieldError(nameInput, message);
            } else if (field === 'code') {
                setFieldError(codeInput, message);
            } else {
                setFormError(message);
            }
        });
    }

    function validateForm() {
        nameInput.value = nameInput.value.trim();
        codeInput.value = codeInput.value.trim();
        clearErrors();
        return validateCodeDetailVO(form);
    }

    form.onValidationErrors = function (messages) {
        showClientMessages(messages);
    };

    form.addEventListener('input', function () { clearErrors(); });

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        if (isSubmitting || !validateForm()) {
            return;
        }

        isSubmitting = true;
        submitButton.disabled = true;
        spinner.classList.remove('d-none');
        buttonLabel.textContent = '생성 중';

        var requestBody = new URLSearchParams(new FormData(form));
        requestBody.set('name', nameInput.value.trim());
        requestBody.set('code', codeInput.value.trim());

        try {
            var response = await fetch(form.action, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: requestBody
            });
            var message = await response.text();

            if (!response.ok) {
                showServerErrors(message || '코드 생성에 실패했습니다.');
                return;
            }

            window.location.reload();
        } catch (error) {
            setFormError(error.message || '코드 생성 중 오류가 발생했습니다.');
        } finally {
            isSubmitting = false;
            submitButton.disabled = false;
            spinner.classList.add('d-none');
            buttonLabel.textContent = '생성';
        }
    });
})();
</script>
