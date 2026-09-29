<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>공통코드 목록</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>" />
    <link href="<c:url value='/resources/css/base.css'/>" rel="stylesheet" type="text/css">
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
        
        <div class="container-lg">
            <c:set var="path" scope="request" value="홈/목록"/>
            <c:set var="title" scope="request" value="공통코드 목록"/>
            <c:set var="dcs" scope="request" value="등록된 전체 코드와 루트 코드를 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <div class="col-12 col-md-5 col-lg-4">
                <label for="rootCode" class="form-label">루트 코드</label>
                <select class="form-select" id="rootCode" name="rootCode">
                    <option value="">루트 코드를 선택하세요</option>
                    <c:forEach var="rootCode" items="${rootCodes}">
                        <option value="${rootCode.cid}">
                            <c:out value="${rootCode.code}"/> - <c:out value="${rootCode.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        
            <div class="table-responsive">
                <table class="table table-light table-hover table-bordered align-middle">
                    <thead>
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">상위 코드</th>
                            <th scope="col">상위 코드명</th>
                            <th scope="col">코드</th>
                            <th scope="col">코드명</th>
                            <th scope="col">레벨</th>
                            <th scope="col">설명</th>
                            <th scope="col">상태</th>
                            <th scope="col">등록일</th>
                            <th scope="col">수정일</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty codes}">
                                <tr><td class="text-center text-body-secondary py-4" colspan="10">등록된 코드가 없습니다.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="code" items="${codes}">
                                    <tr>
                                        <td><c:out value="${code.cid}"/></td>
                                        <td><c:out value="${code.parentCode}"/></td>
                                        <td><c:out value="${code.parentName}"/></td>
                                        <td><c:out value="${code.code}"/></td>
                                        <td><c:out value="${code.name}"/></td>
                                        <td><c:out value="${code.level}"/></td>
                                        <td><c:out value="${code.dsc}"/></td>
                                        <td><c:out value="${code.status}"/></td>
                                        <td><fmt:formatDate value="${code.CDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td><fmt:formatDate value="${code.UDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</body>
</html>