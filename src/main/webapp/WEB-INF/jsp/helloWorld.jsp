<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>사용자 목록</title>
    <style>
        body { margin: 40px; font-family: sans-serif; color: #222; }
        h1 { margin-bottom: 24px; }
        table { width: 100%; max-width: 900px; border-collapse: collapse; }
        th, td { padding: 12px; border: 1px solid #d9dfe5; text-align: left; }
        th { background: #f3f5f7; }
        .empty { padding: 24px; color: #68737d; text-align: center; }
    </style>
</head>
<body>
    <h1>사용자 목록</h1>
    <table>
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
                    <tr><td class="empty" colspan="6">등록된 사용자가 없습니다.</td></tr>
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
</body>
</html>