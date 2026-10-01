<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="commentInsertUrl" value="${param.insertUrl}" />
<c:url var="commentUpdateUrl" value="${param.updateUrl}" />
<c:url var="commentDeleteUrl" value="${param.deleteUrl}" />
<c:set var="commentItems" value="${requestScope[param.commentsAttribute]}" />
<c:set var="commentReplyMap" value="${requestScope[param.replyMapAttribute]}" />

<%-- 통계용 합계 계산 --%>
<c:set var="rootCount" value="${fn:length(commentItems)}" />
<c:set var="replyCount" value="0" />
<c:forEach var="c0" items="${commentItems}">
    <c:set var="replyCount" value="${replyCount + fn:length(commentReplyMap[c0.cmtId])}" />
</c:forEach>


<!-- TODO 현재는 1depth 답글만 지원 -->
<!-- 댓글 -->
<div class="mt-5 | mb-3">
    <div class="mb-3 | justify-content-between | d-flex">
        <h5>댓글 목록</h5>
        <div>
            <span class="badge | text-bg-secondary | ms-2">전체 ${rootCount + replyCount}</span>
            <span class="badge | text-bg-primary | ms-1">댓글 ${rootCount}</span>
            <span class="badge | text-bg-info | text-white | ms-1">답글 ${replyCount}</span>
        </div>
    </div>

    <!-- 목록 -->
    <div class="table-responsive">
        <table class="table | table-hover | align-middle | mb-0">
            <thead class="table-light">
                <tr>
                    <th scope="col" style="width: 80px;">번호</th>
                    <th scope="col" style="width: 80px;">유형</th>
                    <th scope="col" style="width: 110px;">작성자</th>
                    <th scope="col">내용</th>
                    <th scope="col" style="width: 150px;">작성일</th>
                    <th scope="col" class="text-end" style="width: 190px;">관리</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="comment" items="${commentItems}">
                    <c:set var="replies" value="${commentReplyMap[comment.cmtId]}"/>

                    <!-- 댓글 행 -->
                    <tr class="js-cmt-row" data-type="comment"
                        data-search="<c:out value='${fn:toLowerCase(comment.cmt)}'/> ${comment.uid}">
                        <td class="text-body-secondary">${comment.cmtId}</td>
                        <td><span class="badge | text-bg-primary">댓글</span></td>
                        <td>사용자 ${comment.uid}</td>
                        <td>
                            <div class="text-break" style="white-space: pre-wrap;"><c:out value="${comment.cmt}"/></div>
                            <c:if test="${not empty replies}">
                                <div class="small | text-body-secondary | mt-1">답글 ${fn:length(replies)}개</div>
                            </c:if>
                        </td>
                        <td class="text-body-secondary | small">
                            <fmt:formatDate value="${comment.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex | gap-1 | justify-content-end">
                                <button class="btn | btn-sm | btn-outline-primary" type="button"
                                        data-reply-toggle="reply-form-${comment.cmtId}">답글</button>
                                <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                        data-comment-edit-toggle="edit-comment-${comment.cmtId}">수정</button>
                                <c:choose>
                                    <c:when test="${not empty replies}">
                                        <c:set var="deleteConfirmText" value="이 댓글과 달린 답글 ${fn:length(replies)}개가 모두 삭제됩니다. 삭제하시겠습니까?" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="deleteConfirmText" value="댓글을 삭제하시겠습니까?" />
                                    </c:otherwise>
                                </c:choose>
                                <form action="${commentDeleteUrl}" method="post" class="js-confirm-form | d-inline"
                                      data-confirm-text="${deleteConfirmText}" data-success-text="삭제가 완료되었습니다.">
                                    <input type="hidden" name="cmtId" value="${comment.cmtId}">
                                    <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                    <button class="btn | btn-sm | btn-outline-danger" type="submit">삭제</button>
                                </form>
                            </div>
                        </td>
                    </tr>

                    <!-- 댓글 수정 폼 (기본 숨김) -->
                    <tr class="d-none | table-active" id="edit-comment-${comment.cmtId}">
                        <td colspan="6">
                            <form action="${commentUpdateUrl}" method="post" accept-charset="UTF-8"
                                  class="js-update-form" data-success-text="수정이 완료되었습니다.">
                                <input type="hidden" name="cmtId" value="${comment.cmtId}">
                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                <input type="hidden" name="uid" value="${comment.uid}">
                                <input type="hidden" name="status" value="Y">
                                <label class="form-label | small | fw-semibold">댓글 수정 (#${comment.cmtId})</label>
                                <textarea class="form-control | mb-2" name="cmt" rows="2" required><c:out value="${comment.cmt}"/></textarea>
                                <div class="d-flex | justify-content-end | gap-2">
                                    <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                            data-comment-edit-toggle="edit-comment-${comment.cmtId}">취소</button>
                                    <button class="btn | btn-sm | btn-primary" type="submit">저장</button>
                                </div>
                            </form>
                        </td>
                    </tr>

                    <!-- 답글 목록 (항상 표시) -->
                    <c:forEach var="reply" items="${replies}">
                        <tr class="js-cmt-row | table-light" data-type="reply"
                            data-search="<c:out value='${fn:toLowerCase(reply.cmt)}'/> ${reply.uid}">
                            <td class="text-body-secondary">${reply.cmtId}</td>
                            <td><span class="badge | text-bg-info | text-white">답글</span></td>
                            <td>사용자 ${reply.uid}</td>
                            <td>
                                <div class="d-flex | gap-2">
                                    <span class="text-body-secondary">↳</span>
                                    <div class="text-break" style="white-space: pre-wrap;"><c:out value="${reply.cmt}"/></div>
                                </div>
                                <div class="small | text-body-secondary | ps-4 | mt-1">원 댓글 #${comment.cmtId}</div>
                            </td>
                            <td class="text-body-secondary | small">
                                <fmt:formatDate value="${reply.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                            </td>
                            <td class="text-end">
                                <div class="d-inline-flex | gap-1 | justify-content-end">
                                    <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                            data-comment-edit-toggle="edit-reply-${reply.cmtId}">수정</button>
                                    <form action="${commentDeleteUrl}" method="post" class="js-confirm-form | d-inline"
                                          data-confirm-text="답글을 삭제하시겠습니까?" data-success-text="삭제가 완료되었습니다.">
                                        <input type="hidden" name="cmtId" value="${reply.cmtId}">
                                        <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                        <button class="btn | btn-sm | btn-outline-danger" type="submit">삭제</button>
                                    </form>
                                </div>
                            </td>
                        </tr>

                        <!-- 답글 수정 폼 (기본 숨김) -->
                        <tr class="d-none | table-active" id="edit-reply-${reply.cmtId}">
                            <td colspan="6">
                                <form action="${commentUpdateUrl}" method="post" accept-charset="UTF-8"
                                      class="js-update-form" data-success-text="수정이 완료되었습니다.">
                                    <input type="hidden" name="cmtId" value="${reply.cmtId}">
                                    <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                    <input type="hidden" name="uid" value="${reply.uid}">
                                    <input type="hidden" name="status" value="Y">
                                    <label class="form-label | small | fw-semibold">답글 수정 (#${reply.cmtId})</label>
                                    <textarea class="form-control | mb-2" name="cmt" rows="2" required><c:out value="${reply.cmt}"/></textarea>
                                    <div class="d-flex | justify-content-end | gap-2">
                                        <button class="btn | btn-sm | btn-outline-secondary" type="button"
                                                data-comment-edit-toggle="edit-reply-${reply.cmtId}">취소</button>
                                        <button class="btn | btn-sm | btn-primary" type="submit">저장</button>
                                    </div>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>

                    <!-- 답글 입력 폼 (기본 숨김, 답글 버튼으로 토글) -->
                    <tr class="d-none | table-active | js-comment-compose" id="reply-form-${comment.cmtId}">
                        <td colspan="6">
                            <form action="${commentInsertUrl}" method="post" accept-charset="UTF-8">
                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                <input type="hidden" name="uid" value="1">
                                <input type="hidden" name="parentCmtId" value="${comment.cmtId}">
                                <input type="hidden" name="status" value="Y">

                                <label class="form-label | small | fw-semibold">댓글 #${comment.cmtId}에 답글 등록</label>
                                <div class="input-group | input-group-sm">
                                    <input type="text" class="form-control" name="cmt" placeholder="답글을 입력해주세요." required>
                                    <button type="button" class="btn | btn-outline-secondary"
                                            data-reply-toggle="reply-form-${comment.cmtId}">취소</button>
                                    <button type="submit" class="btn | btn-primary">등록</button>
                                </div>
                            </form>
                        </td>
                    </tr>
                </c:forEach>

                <!-- 댓글 입력 폼 (기본 표시) -->
                <tr class="table-active | js-comment-compose" id="new-comment-form">
                    <td colspan="6">
                        <form action="${commentInsertUrl}" method="post" accept-charset="UTF-8">
                            <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                            <input type="hidden" name="uid" value="1">
                            <input type="hidden" name="status" value="Y">
                            <label class="form-label | small | fw-semibold">댓글 작성</label>
                            <div class="input-group | input-group-sm">
                                <input type="text" class="form-control" name="cmt" placeholder="댓글을 입력해주세요." required>
                                <button type="submit" class="btn | btn-sm | btn-primary">댓글 등록</button>
                            </div>

                        </form>
                    </td>
                </tr>
                <!-- 데이터 없음 -->
                <c:if test="${empty commentItems}">
                    <tr>
                        <td colspan="6" class="text-center | text-body-secondary | py-5">등록된 댓글이 없습니다.</td>
                    </tr>
                </c:if>

                <!-- 필터 결과 없음 -->
                <tr class="d-none | js-cmt-empty">
                    <td colspan="6" class="text-center | text-body-secondary | py-5">검색 결과가 없습니다.</td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<!-- 답글/수정 박스 토글 (CoreUI JS 의존 없음, 중복 include 시 1회만 바인딩) -->
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
            if (btn) {
                var composeForms = box.closest('tbody').querySelectorAll('.js-comment-compose');
                var returnToComment = targetId === 'new-comment-form' || !box.classList.contains('d-none');
                composeForms.forEach(function (form) {
                    form.classList.toggle('d-none', returnToComment
                        ? form.id !== 'new-comment-form'
                        : form.id !== targetId);
                });
            } else {
                var isOpen = box.classList.toggle('d-none') === false;
            }

            var isOpen = !box.classList.contains('d-none');
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
                var successUrl = form.getAttribute('data-success-url');
                if (successUrl) {
                    location.href = successUrl;
                } else {
                    location.reload();
                }
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
            var submitUpdate = function () {
                submitFormAjax(updateForm, function () { showDone(updateForm, '수정이 완료되었습니다.'); });
            };
            var confirmText = updateForm.getAttribute('data-confirm-text');
            if (confirmText) {
                CmpDialog.open({
                    title: '확인',
                    text: confirmText,
                    okText: '수정',
                    cancelText: '취소',
                    okButton: submitUpdate
                });
            } else {
                submitUpdate();
            }
        }
    });
})();
</script>
