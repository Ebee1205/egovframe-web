<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%--
    공용 확인/취소 다이얼로그 (CoreUI Modal 기반)

    포함 (페이지당 1회):
        <jsp:include page="/WEB-INF/jsp/cmp/CmpYNDialog.jsp">
            <jsp:param name="modalId" value="cmpYNDialog" /> <!-- 생략 시 'cmpYNDialog' -->
        </jsp:include>

    사용법 (JS):
        CmpDialog.open({
            title: '삭제 완료',
            text: '스냅이 삭제되었습니다.',
            okText: '확인',
            isOneButton: true,       // true면 취소(X 포함) 버튼이 사라지고 확인 버튼만 표시
            okButton: function () {
                CmpDialog.close();
                location.href = '/snaps';
            }
        });
--%>
<c:set var="modalId" value="${empty param.modalId ? 'cmpYNDialog' : param.modalId}" />
<script src="<c:url value='/resources/coreui-5.8.0-dist/js/coreui.bundle.min.js'/>"></script>

<!-- Dialog -->
<div class="modal | fade" id="${modalId}" tabindex="-1" aria-hidden="true" style="--cui-modal-zindex: 1060;">
    <div class="modal-dialog | modal-sm | modal-dialog-centered">
        <div class="modal-content" style="border-radius: 12px;">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title | w-100 text-center" data-dialog-title></h5>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="Close" 
                    data-dialog-close>
                </button>
            </div>
            <div class="modal-body | text-center | text-body-secondary" data-dialog-text></div>
            <div class="modal-footer | border-0 | justify-content-center | gap-2">
                <button type="button" class="btn | btn-outline-secondary | flex-fill" 
                    data-coreui-dismiss="modal" 
                    data-dialog-cancel
                >취소</button>
                <button type="button" class="btn | btn-primary | flex-fill" 
                    data-dialog-ok
                >확인</button>
            </div>
        </div>
    </div>
</div>
<!--// Dialog -->

<script>
(function () {
    // 동일 페이지에 여러 번 include 되어도 한 번만 정의
    if (window.CmpDialog) return;

    window.CmpDialog = {
        open: function (options) {
            options = options || {};
            var el = document.getElementById(options.modalId || 'cmpYNDialog');
            if (!el) return;

            var isOneButton = !!options.isOneButton;
            var titleEl = el.querySelector('[data-dialog-title]');
            var textEl = el.querySelector('[data-dialog-text]');
            var okBtn = el.querySelector('[data-dialog-ok]');
            var cancelBtn = el.querySelector('[data-dialog-cancel]');
            var closeBtn = el.querySelector('[data-dialog-close]');

            titleEl.textContent = options.title || '';
            textEl.innerHTML = options.text || '';
            okBtn.textContent = options.okText || '확인';
            cancelBtn.textContent = options.cancelText || '취소';

            // isOneButton 이 true 면 취소/닫기 버튼을 숨기고 확인 버튼만 표시
            cancelBtn.classList.toggle('d-none', isOneButton);
            closeBtn.classList.toggle('d-none', isOneButton);
            okBtn.classList.toggle('flex-fill', !isOneButton);
            okBtn.style.width = isOneButton ? '100%' : '';

            okBtn.onclick = function () {
                if (typeof options.okButton === 'function') options.okButton();
            };

            // show()가 새로 만든 배경만 조정하여 다른 모달의 배경은 유지한다.
            var existingBackdrops = new Set(document.querySelectorAll('.modal-backdrop'));
            coreui.Modal.getOrCreateInstance(el).show();
            document.querySelectorAll('.modal-backdrop').forEach(function (backdrop) {
                if (!existingBackdrops.has(backdrop)) {
                    backdrop.style.setProperty('--cui-backdrop-zindex', '1056');
                }
            });
        },

        close: function (modalId) {
            var el = document.getElementById(modalId || 'cmpYNDialog');
            if (!el) return;
            var instance = coreui.Modal.getInstance(el);
            if (instance) instance.hide();
        }
    };
})();
</script>
