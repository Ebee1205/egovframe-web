<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>사용자 목록</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>" />
    <link href="<c:url value='/resources/css/base.css'/>" rel="stylesheet" type="text/css">
    <link href="<c:url value='/resources/css/form.css'/>" rel="stylesheet" type="text/css">
    <link href="<c:url value='/resources/css/table.css'/>" rel="stylesheet" type="text/css">
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
            <c:set var="path" scope="request" value="홈/사용자"/>
            <c:set var="title" scope="request" value="사용자 목록"/>
            <c:set var="dcs" scope="request" value="등록된 사용자를 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/user/list.do'/>">
                <input type="hidden" name="pageIndex" value="1" />
                <div class="d-flex | flex-wrap | align-items-center | gap-2">
                    <input
                        class="form-control | input-box"
                        type="search"
                        placeholder="이메일 검색"
                        id="email" name="email"
                        value="<c:out value='${filterVO.email}'/>"
                    />
                    <input
                        class="form-control | input-box"
                        type="search"
                        placeholder="닉네임 검색"
                        id="nickname" name="nickname"
                        value="<c:out value='${filterVO.nickname}'/>"
                    />
                    <select 
                        class="form-select | input-box" 
                        placeholder="사용자 유형 선택"
                        id="type" name="type">
                        <option value="">전체</option>
                        <c:forEach var="code" items="${userTypes}">
                            <option value="<c:out value='${code.code}'/>" ${code.code == filterVO.type ? 'selected' : ''}>
                                <c:out value="${code.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <select 
                        class="form-select | input-box" 
                        placeholder="사용자 상태 선택"
                        id="status" name="status">
                        <option value="">전체</option>
                        <c:forEach var="code" items="${userStatuses}">
                            <option value="<c:out value='${code.code}'/>" ${code.code == filterVO.status ? 'selected' : ''}>
                                <c:out value="${code.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn | btn-primary">검색</button>
            </form>

            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info">
                    <p class="table-info-text">총 <c:out value="${filterVO.totalCnt}"/>건</p>
                    <a href="<c:url value='/user/create.do'/>" class="btn | btn-outline-secondary">신규 등록</a>
                </div>

                <div class="table-responsive">
                    <table class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center">
                            <tr>
                                <th scope="col" style="width: 45px;">NO</th>
                                <th scope="col">이메일</th>
                                <th scope="col">닉네임</th>
                                <th scope="col" style="width: 120px;">유형</th>
                                <th scope="col" style="width: 80px;">상태</th>
                                <th scope="col" style="width: 120px;">지역</th>
                                <th scope="col" style="width: 80px;">상세</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty users}">
                                    <tr><td class="text-center text-body-secondary py-4" colspan="7">조회된 데이터가 없습니다.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="user" items="${users}" varStatus="rowStatus">
                                        <tr>
                                            <td class="text-center"><c:out value="${filterVO.firstIndex + rowStatus.count}"/></td>
                                            <td><c:out value="${user.email}"/></td>
                                            <td><c:out value="${user.nickname}"/></td>
                                            <td>
                                                <c:set var="displayType" value="${user.type}"/>
                                                <c:forEach var="userType" items="${userTypes}">
                                                    <c:if test="${user.type eq userType.code}">
                                                        <c:set var="displayType" value="${userType.name}"/>
                                                    </c:if>
                                                </c:forEach>
                                                <c:out value="${displayType}"/>
                                            </td>
                                            <td>
                                                <c:set var="displayStatus" value="${user.status}"/>
                                                <c:forEach var="userStatus" items="${userStatuses}">
                                                    <c:if test="${user.status eq userStatus.code}">
                                                        <c:set var="displayStatus" value="${userStatus.name}"/>
                                                    </c:if>
                                                </c:forEach>
                                                <c:out value="${displayStatus}"/>
                                            </td>
                                            <td><c:out value="${user.rid}"/></td>
                                            <td><a href="<c:url value='/user/detail.do'><c:param name='uid' value='${user.uid}'/></c:url>" class="btn | btn-outline-primary">상세</a></td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- 테이블 페이징 -->
                <c:if test="${filterVO.totalCnt > 0}">
                    <nav aria-label="사용자 목록 페이징">
                        <ul class="pagination | justify-content-center">
                            <c:forEach var="i" begin="1" end="${totalPage}">
                                <li class="page-item ${i == filterVO.pageIndex ? 'active' : ''}">
                                    <a class="page-link"
                                        href="<c:url value='/user/list.do'>
                                                <c:param name='pageIndex' value='${i}'/>
                                                <c:param name='email' value='${filterVO.email}'/>
                                                <c:param name='nickname' value='${filterVO.nickname}'/>
                                                <c:param name='type' value='${filterVO.type}'/>
                                                <c:param name='status' value='${filterVO.status}'/>
                                              </c:url>">
                                        <c:out value="${i}"/>
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </nav>
                </c:if>
            </div>

        </div>
    </div>
</body>
</html>
