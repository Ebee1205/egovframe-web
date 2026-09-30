<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이벤트 상세</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>" />
    <link href="<c:url value='/resources/css/base.css'/>" rel="stylesheet" type="text/css">
    <link href="<c:url value='/resources/css/form.css'/>" rel="stylesheet" type="text/css">
</head>
<body>
    <noscript>자바스크립트를 지원하지 않는 브라우저에서는 일부 기능을 사용하실 수 없습니다.</noscript>
        

    <!-- Leftnav -->
    <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpLeftnav.jsp" />
    <!--// Leftnav -->

    <div class="wrapper | bg-body | main-content">
        <!-- Header -->
        <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpHeader.jsp" />
        <!--// Header -->
        
        <div class="p-3 | align-items-center">
            <c:set var="path" scope="request" value="홈/이벤트/상세"/>
            <c:set var="title" scope="request" value="${event.title}"/>
            <c:set var="dcs" scope="request" value="등록자: ${event.createdBy}"/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />
        </div>

        <div class="p-3 | align-items-center">
            <form class="form-container">
                <div>
                    <label for="title" class="form-label">이벤트 제목</label>
                    <input type="text" class="form-control" id="title" name="title" value="${event.title}">
                </div>
        
                <div>
                    <label for="ctg" class="form-label">이벤트 카테고리</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="카테고리 선택"
                        id="ctg" name="ctg">
                        <option value="">전체</option>
                        <c:forEach var="eventCtg" items="${eventCtgs}">
                            <option value="${eventCtg.code}" ${eventCtg.code == event.ctg ? 'selected' : ''}>
                                <c:out value="${eventCtg.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
        
                <div class="row">
                    <div class="col">
                        <label for="sDate" class="form-label">이벤트 시작일</label>
                        <input type="text" class="form-control" id="sDate" name="sDate" value="<fmt:formatDate value='${event.SDate}' pattern='yyyy-MM-dd HH:mm'/>">
                    </div>
                    <div class="col">
                        <label for="eDate" class="form-label">이벤트 종료일</label>
                        <input type="text" class="form-control" id="eDate" name="eDate" value="<fmt:formatDate value='${event.EDate}' pattern='yyyy-MM-dd HH:mm'/>">
                    </div>
                </div>
        
                <!-- <div class="mb-3">
                    <label for="exampleFormControlTextarea1" class="form-label">이벤트 지역</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="지역 선택"
                        id="region" name="region">
                        <option value="">전체</option>
                        <c:forEach var="eventRegion" items="${eventRegions}">
                            <option value="${eventRegion.code}" ${eventRegion.code == filterVO.region ? 'selected' : ''}>
                                <c:out value="${eventRegion.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div> -->
        
                <div>
                    <label for="address" class="form-label">이벤트 주소</label>
                    <input type="text" class="form-control" id="address" name="address" value="${event.address}">
                </div>
        
                <div>
                    <label for="dsc" class="form-label">이벤트 내용</label>
                    <textarea class="form-control" id="dsc" name="dsc" rows="3"><c:out value="${event.dsc}"/></textarea>
                </div>

                <div>
                    <label for="status" class="form-label">이벤트 상태</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="상태 선택"
                        id="status" name="status">
                        <option value="">전체</option>
                        <c:forEach var="eventStatus" items="${eventStatuses}">
                            <option value="${eventStatus.code}" ${eventStatus.code == event.status ? 'selected' : ''}>
                                <c:out value="${eventStatus.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

            </form>
        </div>

        <!-- 폼 하단 버튼박스 -->
        <div class="form-info">
            <div>
                <p class="form-info-text">최초 등록일: <fmt:formatDate value="${event.CDate}" pattern="yyyy-MM-dd HH:mm"/></p>
                <p class="form-info-text">최종 수정일: <fmt:formatDate value="${event.UDate}" pattern="yyyy-MM-dd HH:mm"/></p>
            </div>
            <div>
                <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">수정</a>
                <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">삭제</a>
            </div>
        </div>

        <!-- TODO 여기서부터는 서비스 페이지로 옮겨사용하기 -->
        <!-- 댓글 -->
        <div class="container-fluid | p-3">
            <div class="card">
                <div class="card-header">
                    <strong>댓글</strong>
                </div>

                <div class="card-body">
                    <form action="<c:url value='/event/cmt/insert.do'/>" method="post">
                        <input type="hidden" name="eid" value="${event.eid}">
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
                    <c:forEach var="comment" items="${comments}">
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
                                    <c:set var="replies" value="${replyMap[comment.cmtId]}"/>

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

                                    <form action="<c:url value='/event/cmt/insert.do'/>" method="post" class="mt-2">
                                        <input type="hidden" name="eid" value="${event.eid}">
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

                    <c:if test="${empty comments}">
                        <div class="list-group-item | text-center | text-body-secondary | py-4">
                            등록된 댓글이 없습니다.
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

    </div>


</body>
</html>
