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
            <c:set var="path" scope="request" value="홈/목록"/>
            <c:set var="title" scope="request" value="공통코드 목록"/>
            <c:set var="dcs" scope="request" value="등록된 전체 코드와 루트 코드를 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/CodeList.do'/>">
                <input type="hidden" name="pageIndex" value="1" />
                <div class="d-flex | flex-wrap | align-items-center | gap-2">
                    <select 
                        class="form-select | input-box" 
                        placeholder="코드 그룹 선택"
                        id="parentCid" name="parentCid">
                        <option value="">전체</option>
                        <c:forEach var="code" items="${rootCodes}">
                            <option value="${code.cid}" ${code.cid == filterVO.parentCid ? 'selected' : ''}>
                                <c:out value="${code.code}"/> - <c:out value="${code.name}"/>
                            </option>
                        </c:forEach>
                    </select>

                    <input
                        class="form-control | input-box" 
                        type="text"
                        placeholder="코드명 검색"
                        id="name" name="name"
                        value="${filterVO.name}"
                    ></input>
                </div>

                <!-- 우측: 검색 버튼 -->
                <button type="submit" class="btn | btn-primary">검색</button>
            </form>

            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info">
                    <p class="table-info-text">총 <c:out value="${filterVO.totalCnt}"/>건</p>
                    <button type="create" class="btn | btn-outline-secondary">신규 등록</button>
                </div>

                <!-- 테이블 본문 -->
                <div class="table-responsive">
                    <table class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center">
                            <tr>
                                <th scope="col">상위 코드</th>
                                <th scope="col">상위 코드명</th>
                                <th scope="col">코드</th>
                                <th scope="col">코드명</th>
                                <th scope="col">레벨</th>
                                <th scope="col">설명</th>
                                <th scope="col">상태</th>
                                <th scope="col">등록일</th>
                                <th scope="col">수정일</th>
                                <th scope="col">관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty codes}">
                                    <tr><td class="text-center text-body-secondary py-4" colspan="10">조회된 데이터가 없습니다.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="code" items="${codes}">
                                        <tr>
                                            <td><c:out value="${code.parentCode}"/></td>
                                            <td><c:out value="${code.parentName}"/></td>
                                            <td><c:out value="${code.code}"/></td>
                                            <td><c:out value="${code.name}"/></td>
                                            <td><c:out value="${code.level}"/></td>
                                            <td><c:out value="${code.dsc}"/></td>
                                            <td><c:out value="${code.status}"/></td>
                                            <td><fmt:formatDate value="${code.CDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><fmt:formatDate value="${code.UDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td class="text-nowrap">
                                                <button type="button" class="btn | btn-sm | btn-outline-primary" data-code-id="${code.cid}" aria-label="${code.name} 수정">수정</button>
                                                <button type="button" class="btn | btn-sm | btn-outline-danger" data-code-id="${code.cid}" aria-label="${code.name} 삭제">삭제</button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                
                <!-- 테이블 페이징 -->
                <c:if test="${filterVO.totalCnt > 0}">
                    <nav aria-label="공통코드 목록 페이징">
                        <ul class="pagination | justify-content-center">
                            <c:forEach var="i" begin="1" end="${totalPage}">
                                <li class="page-item ${i == filterVO.pageIndex ? 'active' : ''}">
                                    <a class="page-link"
                                        href="<c:url value='/CodeList.do'>
                                                <c:param name='pageIndex' value='${i}'/>
                                                <c:param name='name' value='${filterVO.name}'/>
                                                <c:param name='parentCid' value='${filterVO.parentCid}'/>
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


    <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
        
    </div>
</body>
</html>
