<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="commentInsertUrl" value="${param.insertUrl}" />
<c:set var="commentItems" value="${requestScope[param.commentsAttribute]}" />
<c:set var="commentReplyMap" value="${requestScope[param.replyMapAttribute]}" />
<link href="<c:url value='/resources/css/post.css'/>" rel="stylesheet" type="text/css">

<!-- TODO 현재는 1depth 답글만 지원 -->
<!-- 댓글 -->
<div class="container-fluid | p-3">
    <div class="card">
        <div class="card-header | d-flex | justify-content-between | align-items-center">
            <strong>댓글</strong>
            <span class="text-body-secondary | small">${empty commentItems ? 0 : fn:length(commentItems)}개</span>
        </div>

        <!-- 댓글 작성 -->
        <div class="card-body | border-bottom">
            <form action="${commentInsertUrl}" method="post" accept-charset="UTF-8" class="d-flex | gap-3">
                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                <input type="hidden" name="uid" value="1">
                <input type="hidden" name="status" value="Y">

                <div class="avatar | avatar-md | bg-primary | text-white | flex-shrink-0">
                    <span class="avatar-status"></span>U1
                </div>

                <div class="flex-grow-1 | cmt-reply-box | px-2 | pt-1 | pb-2">
                    <textarea class="form-control | cmt-input" name="cmt" rows="2" placeholder="어떻게 생각하세요?" required></textarea>
                    <div class="d-flex | justify-content-end">
                        <button type="submit" class="btn | btn-primary | btn-sm | rounded-pill | px-3">댓글</button>
                    </div>
                </div>
            </form>
        </div>

        <!-- 댓글 목록 -->
        <div class="list-group | list-group-flush">
            <c:forEach var="comment" items="${commentItems}">
                <c:set var="replies" value="${commentReplyMap[comment.cmtId]}"/>

                <div class="list-group-item | py-3">
                    <div class="d-flex | gap-3 | cmt-thread">
                        <!-- 아바타 + 스레드 라인 -->
                        <div class="d-flex | flex-column | align-items-center | flex-shrink-0 | cmt-side">
                            <div class="avatar | avatar-md | bg-secondary | text-white">U${comment.uid}</div>
                            <c:if test="${not empty replies}">
                                <div class="cmt-thread-line"></div>
                            </c:if>
                        </div>

                        <div class="flex-grow-1 | min-w-0">
                            <!-- 헤더 -->
                            <div class="d-flex | align-items-center | gap-2">
                                <strong>사용자 ${comment.uid}</strong>
                                <span class="text-body-secondary | small">·</span>
                                <span class="text-body-secondary | small">
                                    <fmt:formatDate value="${comment.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                                </span>
                            </div>

                            <!-- 본문 -->
                            <div class="my-1" style="white-space: pre-wrap; word-break: break-word;"><c:out value="${comment.cmt}"/></div>

                            <!-- 액션 -->
                            <div class="d-flex | align-items-center | gap-1 | ms-n2">
                                <a class="cmt-action" role="button"
                                   data-coreui-toggle="collapse"
                                   data-coreui-target="#reply-form-${comment.cmtId}">
                                    💬 답글<c:if test="${not empty replies}"> ${fn:length(replies)}</c:if>
                                </a>
                            </div>

                            <!-- 답글 입력 (토글) -->
                            <div class="collapse | mt-2" id="reply-form-${comment.cmtId}">
                                <form action="${commentInsertUrl}" method="post" accept-charset="UTF-8" class="d-flex | gap-2">
                                    <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                    <input type="hidden" name="uid" value="1">
                                    <input type="hidden" name="parentCmtId" value="${comment.cmtId}">
                                    <input type="hidden" name="status" value="Y">

                                    <div class="flex-grow-1 | cmt-reply-box | px-2 | pt-1 | pb-2">
                                        <textarea class="form-control | cmt-input" name="cmt" rows="2" placeholder="사용자 ${comment.uid}님에게 답글 남기기" required></textarea>
                                        <div class="d-flex | justify-content-end | gap-2">
                                            <button type="button" class="btn | btn-sm | btn-ghost-secondary | rounded-pill"
                                                    data-coreui-toggle="collapse"
                                                    data-coreui-target="#reply-form-${comment.cmtId}">취소</button>
                                            <button type="submit" class="btn | btn-sm | btn-primary | rounded-pill | px-3">답글</button>
                                        </div>
                                    </div>
                                </form>
                            </div>

                            <!-- 답글 목록 (항상 표시) -->
                            <c:forEach var="reply" items="${replies}">
                                <div class="d-flex | gap-2 | mt-3">
                                    <div class="avatar | avatar-sm | bg-info | text-white | flex-shrink-0">U${reply.uid}</div>
                                    <div class="flex-grow-1 | min-w-0">
                                        <div class="d-flex | align-items-center | gap-2">
                                            <strong class="small">사용자 ${reply.uid}</strong>
                                            <span class="text-body-secondary | small">·</span>
                                            <span class="text-body-secondary | small">
                                                <fmt:formatDate value="${reply.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                                            </span>
                                        </div>
                                        <div style="white-space: pre-wrap; word-break: break-word;"><c:out value="${reply.cmt}"/></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </c:forEach>

            <c:if test="${empty commentItems}">
                <div class="list-group-item | text-center | text-body-secondary | py-5">
                    아직 댓글이 없습니다. 첫 댓글을 남겨보세요.
                </div>
            </c:if>
        </div>
    </div>
</div>
