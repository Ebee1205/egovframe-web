<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="commentInsertUrl" value="${param.insertUrl}" />
<c:set var="commentItems" value="${requestScope[param.commentsAttribute]}" />
<c:set var="commentReplyMap" value="${requestScope[param.replyMapAttribute]}" />

<!-- 댓글 -->
<div class="container-fluid | p-3">
    <div class="card">
        <div class="card-header">
            <strong>댓글</strong>
        </div>

        <div class="card-body">
            <form action="${commentInsertUrl}" method="post">
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
                <div class="list-group-item | py-3">
                    <div class="d-flex | justify-content-between | align-items-center | mb-2">
                        <div>
                            <strong>사용자 ${comment.uid}</strong>
                            <span class="text-body-secondary | ms-2">
                                <fmt:formatDate value="${comment.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                            </span>
                        </div>

                        <div>
                            <button class="btn | btn-sm | btn-outline-secondary" type="button" data-coreui-toggle="collapse" data-coreui-target="#reply-${comment.cmtId}">
                                답글
                            </button>
                        </div>
                    </div>

                    <div class="mb-2">
                        <c:out value="${comment.cmt}"/>
                    </div>

                    <div class="collapse | mt-3" id="reply-${comment.cmtId}">
                        <div class="border-start | ps-3">
                            <c:set var="replies" value="${commentReplyMap[comment.cmtId]}"/>

                            <c:forEach var="reply" items="${replies}">
                                <div class="card | mb-2">
                                    <div class="card-body | py-2">
                                        <div class="d-flex | justify-content-between | mb-1">
                                            <strong>사용자 ${reply.uid}</strong>
                                            <span class="text-body-secondary">
                                                <fmt:formatDate value="${reply.CDate}" pattern="yyyy-MM-dd HH:mm"/>
                                            </span>
                                        </div>

                                        <div>
                                            <c:out value="${reply.cmt}"/>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>

                            <form action="${commentInsertUrl}" method="post" class="mt-2">
                                <input type="hidden" name="<c:out value='${param.targetIdParam}'/>" value="<c:out value='${param.targetId}'/>">
                                <input type="hidden" name="uid" value="1">
                                <input type="hidden" name="parentCmtId" value="${comment.cmtId}">
                                <input type="hidden" name="status" value="Y">

                                <div class="input-group">
                                    <input type="text" class="form-control" name="cmt" placeholder="답글을 입력해주세요." required>
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
