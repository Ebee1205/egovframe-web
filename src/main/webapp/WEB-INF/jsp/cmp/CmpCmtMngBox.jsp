<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="commentInsertUrl" value="${param.insertUrl}" />
<c:url var="commentUpdateUrl" value="${param.updateUrl}" />
<c:url var="commentDeleteUrl" value="${param.deleteUrl}" />
<c:set var="commentItems" value="${requestScope[param.commentsAttribute]}" />
<c:set var="commentReplyMap" value="${requestScope[param.replyMapAttribute]}" />

<!-- TODO 현재는 1depth 답글만 지원 -->
<!-- 댓글 -->
<div class="container-fluid | p-3">
    <div class="card">
        <div class="card-header">
            <strong>댓글</strong>
        </div>

        <div class="card-body">
            <form action="${commentInsertUrl}" method="post" accept-charset="UTF-8">
                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                <input type="hidden" name="uid" value="1">
                <input type="hidden" name="status" value="Y">

                <div class="mb-2">
                    <textarea class="form-control" name="cmt" rows="3" placeholder="댓글을 입력해주세요." required></textarea>
                </div>

                <div class="d-flex | justify-content-end">
                    <button type="submit" class="btn | btn-primary">댓글 등록</button>
                </div>
            </form>
        </div>

        <div class="list-group | list-group-flush">
            <c:forEach var="comment" items="${commentItems}">
                <c:set var="replies" value="${commentReplyMap[comment.cmtId]}"/>

                <div class="list-group-item | py-3">
                    <div class="d-flex | justify-content-between | align-items-center | mb-2">
                        <div>
                            <strong>사용자 ${comment.uid}</strong>
                            <span class="text-body-secondary | ms-2">
                                <fmt:formatDate value="${comment.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                            </span>
                        </div>

                        <div>
                            <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                    data-reply-toggle="reply-form-${comment.cmtId}">
                                답글<c:if test="${not empty replies}"> ${fn:length(replies)}</c:if>
                            </button>
                        </div>
                    </div>

                    <div class="mb-2" style="white-space: pre-wrap; word-break: break-word;"><c:out value="${comment.cmt}"/></div>

                    <div class="d-flex | justify-content-end | gap-2">
                        <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                data-comment-edit-toggle="edit-comment-${comment.cmtId}">수정</button>
                                                <c:if test="${empty replies}">
                                                        <form action="${commentDeleteUrl}" method="post"
                                                                    class="js-confirm-form" data-confirm-text="댓글을 삭제하시겠습니까?" data-success-text="삭제가 완료되었습니다.">
                                                                <input type="hidden" name="cmtId" value="${comment.cmtId}">
                                                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                                                <button class="btn | btn-sm | btn-outline-danger" type="submit">삭제</button>
                                                        </form>
                                                </c:if>
                    </div>

                    <div class="d-none | mt-2" id="edit-comment-${comment.cmtId}">
                        <form action="${commentUpdateUrl}" method="post" accept-charset="UTF-8"
                              class="js-update-form" data-success-text="수정이 완료되었습니다.">
                            <input type="hidden" name="cmtId" value="${comment.cmtId}">
                            <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                            <input type="hidden" name="uid" value="${comment.uid}">
                            <input type="hidden" name="status" value="Y">
                            <textarea class="form-control | mb-2" name="cmt" rows="2" required><c:out value="${comment.cmt}"/></textarea>
                            <div class="d-flex | justify-content-end | gap-2">
                                <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                        data-comment-edit-toggle="edit-comment-${comment.cmtId}">취소</button>
                                <button class="btn | btn-sm | btn-primary" type="submit">저장</button>
                            </div>
                        </form>
                    </div>

                    <!-- 답글 목록 (항상 표시) -->
                    <c:if test="${not empty replies}">
                        <div class="ps-3 | mt-3">
                            <c:forEach var="reply" items="${replies}">
                                <div class="card | mb-2">
                                    <div class="card-body | py-2">
                                        <div class="d-flex | justify-content-between | mb-1">
                                            <strong>사용자 ${reply.uid}</strong>
                                            <span class="text-body-secondary">
                                                <fmt:formatDate value="${reply.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                                            </span>
                                        </div>

                                        <div style="white-space: pre-wrap; word-break: break-word;">
                                            <c:out value="${reply.cmt}"/>
                                        </div>

                                        <div class="d-flex | justify-content-end | gap-2 | mt-2">
                                            <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                                    data-comment-edit-toggle="edit-reply-${reply.cmtId}">수정</button>
                                            <form action="${commentDeleteUrl}" method="post"
                                                  class="js-confirm-form" data-confirm-text="답글을 삭제하시겠습니까?" data-success-text="삭제가 완료되었습니다.">
                                                <input type="hidden" name="cmtId" value="${reply.cmtId}">
                                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                                <button class="btn | btn-sm | btn-outline-danger" type="submit">삭제</button>
                                            </form>
                                        </div>

                                        <div class="d-none | mt-2" id="edit-reply-${reply.cmtId}">
                                            <form action="${commentUpdateUrl}" method="post" accept-charset="UTF-8"
                                                  class="js-update-form" data-success-text="수정이 완료되었습니다.">
                                                <input type="hidden" name="cmtId" value="${reply.cmtId}">
                                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                                <input type="hidden" name="uid" value="${reply.uid}">
                                                <input type="hidden" name="status" value="Y">
                                                <textarea class="form-control | mb-2" name="cmt" rows="2" required><c:out value="${reply.cmt}"/></textarea>
                                                <div class="d-flex | justify-content-end | gap-2">
                                                    <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                                            data-comment-edit-toggle="edit-reply-${reply.cmtId}">취소</button>
                                                    <button class="btn | btn-sm | btn-primary" type="submit">저장</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>

                    <!-- 답글 입력 (기본 숨김, 답글 버튼으로 토글) -->
                    <div class="d-none | mt-3" id="reply-form-${comment.cmtId}">
                        <div class="border-start | ps-3">
                            <form action="${commentInsertUrl}" method="post" accept-charset="UTF-8">
                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                <input type="hidden" name="uid" value="1">
                                <input type="hidden" name="parentCmtId" value="${comment.cmtId}">
                                <input type="hidden" name="status" value="Y">

                                <div class="input-group">
                                    <input type="text" class="form-control" name="cmt" placeholder="답글을 입력해주세요." required>
                                    <button type="button" class="btn | btn-outline-secondary"
                                            data-reply-toggle="reply-form-${comment.cmtId}">취소</button>
                                    <button type="submit" class="btn | btn-outline-primary">등록</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>

            <c:if test="${empty commentItems}">
                <div class="list-group-item | text-center | text-body-secondary | py-4">
                    등록된 댓글이 없습니다.
                </div>
            </c:if>
        </div>
    </div>
</div>

<!-- 답글 박스 토글 (CoreUI JS 의존 없음, 중복 include 시 1회만 바인딩) -->
<script>
(function () {
    if (window.__cmtThreadBound) return;
    window.__cmtThreadBound = true;

    document.addEventListener('click', function (e) {
        var btn = e.target.closest('[data-reply-toggle]');
        var editBtn = e.target.closest('[data-comment-edit-toggle]');
        var toggle = btn || editBtn;
        if (!toggle) return;
        e.preventDefault();

        var targetId = btn
            ? btn.getAttribute('data-reply-toggle')
            : editBtn.getAttribute('data-comment-edit-toggle');
        var box = document.getElementById(targetId);
        if (box) {
            var isOpen = box.classList.toggle('d-none') === false;
            if (isOpen) {
                var input = box.querySelector('input[name="cmt"], textarea');
                if (input) input.focus();
            }
        }
    });
})();
</script>

<!-- 댓글/답글 삭제·수정 확인 및 완료 다이얼로그 (CmpDialog, 중복 include 시 1회만 바인딩) -->
<script>
(function () {
    if (window.__cmtConfirmBound) return;
    window.__cmtConfirmBound = true;

    function submitFormAjax(form, onSuccess) {
        // multipart/form-data로 전송되면 멀티파트 리졸버가 없어 서버에서 파라미터가 비어 바인딩된다.
        // 기존 폼 전송과 동일하게 application/x-www-form-urlencoded로 전송한다.
        var body = new URLSearchParams(new FormData(form));

        fetch(form.action, {
            method: form.method || 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
            body: body
        })
            .then(function (res) {
                if (res.ok) {
                    onSuccess();
                } else {
                    CmpDialog.open({ title: '오류', text: '처리 중 오류가 발생했습니다.', isOneButton: true });
                }
            })
            .catch(function () {
                CmpDialog.open({ title: '오류', text: '네트워크 오류가 발생했습니다.', isOneButton: true });
            });
    }

    function showDone(form, defaultText) {
        CmpDialog.open({
            title: '완료',
            text: form.getAttribute('data-success-text') || defaultText,
            isOneButton: true,
            okText: '확인',
            okButton: function () {
                location.reload();
            }
        });
    }

    document.addEventListener('submit', function (e) {
        var confirmForm = e.target.closest('.js-confirm-form');
        var updateForm = e.target.closest('.js-update-form');

        if (confirmForm) {
            e.preventDefault();
            CmpDialog.open({
                title: '확인',
                text: confirmForm.getAttribute('data-confirm-text') || '삭제하시겠습니까?',
                okText: '삭제',
                cancelText: '취소',
                okButton: function () {
                    submitFormAjax(confirmForm, function () { showDone(confirmForm, '삭제가 완료되었습니다.'); });
                }
            });
            return;
        }

        if (updateForm) {
            e.preventDefault();
            submitFormAjax(updateForm, function () { showDone(updateForm, '수정이 완료되었습니다.'); });
        }
    });
})();
</script>