<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
            <!-- Content here -->
            <div class="table-responsive">
                <table class="table | table-light | table-hover | table-bordered">
                    <thead>
                        <tr>
                            <th>코드명</th>
                            <th>코드값</th>
                            <th>상위 코드</th>
                            <th>레벨</th>
                            <th>설명</th>
                            <th>상태</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty codes}">
                                <tr><td class="text-center | text-body-secondary | py-4" colspan="6">등록된 코드가 없습니다.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="code" items="${codes}">
                                    <tr>
                                        <td><c:out value="${code.name}"/></td>
                                        <td><c:out value="${code.code}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty code.parentCid}">-</c:when>
                                                <c:otherwise><c:out value="${code.parentCode}"/></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:out value="${code.level}"/></td>
                                        <td><c:out value="${code.dsc}"/></td>
                                        <td><c:out value="${code.status}"/></td>
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
<!DOCTYPE html>