<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>엑셀 등록 테스트</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>" />
    <link href="<c:url value='/resources/css/base.css'/>" rel="stylesheet" type="text/css">
    <link href="<c:url value='/resources/css/form.css'/>" rel="stylesheet" type="text/css">
    <link href="<c:url value='/resources/css/table.css'/>" rel="stylesheet" type="text/css">
</head>
<body>
    <noscript>자바스크립트를 지원하지 않는 브라우저에서는 일부 기능을 사용하실 수 없습니다.</noscript>
    <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpLeftnav.jsp" />

    <div class="wrapper | bg-body | main-content">
        <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpHeader.jsp" />
        <div class="p-3 | align-items-center">
            <c:set var="path" scope="request" value="홈/엑셀 등록 테스트"/>
            <c:set var="title" scope="request" value="엑셀 등록 테스트"/>
            <c:set var="dcs" scope="request" value="목업 데이터 15건으로 목록과 일괄 업로드 화면을 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <div class="table-container">
                <div class="table-info">
                    <p class="table-info-text">총 15건</p>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn | btn-outline-success" disabled>엑셀 다운로드</button>
                        <button type="button" class="btn | btn-outline-secondary"
                                data-coreui-toggle="modal" data-coreui-target="#extraXlsModal">일괄 등록</button>
                    </div>
                </div>
                <jsp:include page="/WEB-INF/jsp/extra/ExtraMockTable.jsp" />
            </div>
        </div>
    </div>
    
                    <!-- 테이블 페이징 -->
                <c:if test="${filterVO.totalCnt > 0 and filterVO.pageSize > 0}">
                    <nav aria-label="이벤트 목록 페이징">
                        <ul class="pagination | justify-content-center">
                            <c:forEach var="i" begin="1" end="${totalPage}">
                                <li class="page-item ${i == filterVO.pageIndex ? 'active' : ''}">
                                    <c:url var="eventPageUrl" value="/event/list.do">
                                        <c:param name="pageIndex" value="${i}"/>
                                        <c:param name="title" value="${filterVO.title}"/>
                                        <c:param name="ctg" value="${filterVO.ctg}"/>
                                        <c:param name="reg_id" value="${filterVO.reg_id}"/>
                                        <c:param name="parentRegId" value="${filterVO.parentRegId}"/>
                                    </c:url>
                                    <a class="page-link"
                                        href="<c:out value='${eventPageUrl}'/>">
                                        <c:out value="${i}"/>
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </nav>
                </c:if>
    

    <jsp:include page="/WEB-INF/jsp/extra/ExtraXlsDialog.jsp" />
    <script src="<c:url value='/resources/coreui-5.8.0-dist/js/coreui.bundle.min.js'/>"></script>
</body>
</html>
