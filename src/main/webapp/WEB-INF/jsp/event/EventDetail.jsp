<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
            <c:set var="title" scope="request" value="이벤트 제목~~~"/>
            <c:set var="dcs" scope="request" value="등록사용자~~~"/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />
        </div>

        <div class="p-3 | align-items-center">
            <form class="form-container">
                <div>
                    <label for="exampleFormControlInput1" class="form-label">이벤트 제목</label>
                    <input type="text" class="form-control" id="exampleFormControlInput1" placeholder="이벤트 제목을 입력하세요">
                </div>
        
                <div>
                    <label for="exampleFormControlTextarea1" class="form-label">이벤트 카테고리</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="카테고리 선택"
                        id="ctg" name="ctg">
                        <option value="">전체</option>
                        <c:forEach var="eventCtg" items="${eventCtgs}">
                            <option value="${eventCtg.code}" ${eventCtg.code == filterVO.ctg ? 'selected' : ''}>
                                <c:out value="${eventCtg.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
        
                <div class="row">
                    <div class="col">
                        <label for="exampleFormControlInput1" class="form-label">이벤트 시작일</label>
                        <input type="text" class="form-control" id="exampleFormControlInput1" placeholder="이벤트 시작일을 입력하세요">
                    </div>
                    <div class="col">
                        <label for="exampleFormControlInput1" class="form-label">이벤트 종료일</label>
                        <input type="text" class="form-control" id="exampleFormControlInput1" placeholder="이벤트 종료일을 입력하세요">
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
                    <label for="exampleFormControlInput1" class="form-label">이벤트 주소</label>
                    <input type="text" class="form-control" id="exampleFormControlInput1" placeholder="이벤트 주소를 입력하세요">
                </div>
        
                <div>
                    <label for="exampleFormControlTextarea1" class="form-label">이벤트 내용</label>
                    <textarea class="form-control" id="exampleFormControlTextarea1" rows="3"></textarea>
                </div>
            </form>
        </div>

        <!-- 폼 하단 버튼박스 -->
        <div class="form-info">
            <div>
                <p class="form-info-text">최초 등록일: <c:out value="${filterVO.cDate}"/></p>
                <p class="form-info-text">최종 수정일: <c:out value="${filterVO.uDate}"/></p>
            </div>
            <div>
                <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">수정</a>
                <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">삭제</a>
            </div>
        </div>
    </div>

</body>
</html>
