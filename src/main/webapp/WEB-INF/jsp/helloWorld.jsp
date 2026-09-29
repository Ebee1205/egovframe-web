<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>사용자 목록</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>" />
    <link href="<c:url value='/resources/css/base.css'/>" rel="stylesheet" type="text/css">
</head>
<body>
    <main class="container py-4">
        <h1 class="h3 mb-4">사용자 목록 2</h1>
        <div class="table-responsive">
    <table class="table | table-light | table-hover | table-bordered">
        <thead>
            <tr>
                <th>UID</th>
                <th>이메일</th>
                <th>닉네임</th>
                <th>유형</th>
                <th>상태</th>
                <th>지역 ID</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty users}">
                    <tr><td class="text-center | text-body-secondary | py-4" colspan="6">등록된 사용자가 없습니다.</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="user" items="${users}">
                        <tr>
                            <td><c:out value="${user.uid}"/></td>
                            <td><c:out value="${user.email}"/></td>
                            <td><c:out value="${user.nickname}"/></td>
                            <td><c:out value="${user.type}"/></td>
                            <td><c:out value="${user.status}"/></td>
                            <td><c:out value="${user.rid}"/></td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
        </div>
    </main>
</body>
</html>