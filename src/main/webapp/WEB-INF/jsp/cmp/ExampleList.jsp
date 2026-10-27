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

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/code/list.do'/>">
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
                                <th scope="col">action</th>
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
				<c:if test="${totalPage > 0}">
				    <%-- 표시할 번호 범위 계산 (항상 최대 11칸: 앞쪽 9개 / 중간 7개 / 뒤쪽 9개 + 처음·끝·생략) --%>
				    <c:set var="pgCur" value="${filterVO.pageIndex}"/>
				    <c:set var="pgTotal" value="${totalPage}"/>
				    <c:choose>
				        <c:when test="${pgTotal <= 11}">
				            <c:set var="pgFrom" value="1"/>
				            <c:set var="pgTo" value="${pgTotal}"/>
				        </c:when>
				        <c:when test="${pgCur <= 6}">
				            <c:set var="pgFrom" value="1"/>
				            <c:set var="pgTo" value="9"/>
				        </c:when>
				        <c:when test="${pgCur >= pgTotal - 5}">
				            <c:set var="pgFrom" value="${pgTotal - 8}"/>
				            <c:set var="pgTo" value="${pgTotal}"/>
				        </c:when>
				        <c:otherwise>
				            <c:set var="pgFrom" value="${pgCur - 3}"/>
				            <c:set var="pgTo" value="${pgCur + 3}"/>
				        </c:otherwise>
				    </c:choose>

				    <nav aria-label="공통코드 목록 페이징">
				        <ul class="pagination | justify-content-center | flex-wrap">

				            <%-- « 첫 페이지 --%>
				            <li class="page-item ${pgCur > 1 ? '' : 'disabled'}">
				                <c:choose>
				                    <c:when test="${pgCur > 1}">
				                        <c:url var="pgUrl" value="/code/list.do">
				                            <c:param name="pageIndex" value="1"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="name" value="${filterVO.name}"/>
				                            <c:param name="parentCid" value="${filterVO.parentCid}"/>
				                        </c:url>
				                        <a class="page-link" href="<c:out value='${pgUrl}'/>" aria-label="첫 페이지로 이동">
				                            <span aria-hidden="true">&laquo;</span>
				                        </a>
				                    </c:when>
				                    <c:otherwise>
				                        <span class="page-link" aria-label="첫 페이지" aria-disabled="true"><span aria-hidden="true">&laquo;</span></span>
				                    </c:otherwise>
				                </c:choose>
				            </li>

				            <%-- 페이지 번호: 1, 2, 3 ... 9, …, 46 형태 --%>
				            <c:forEach var="i" begin="1" end="${pgTotal}">
				                <c:choose>
				                    <c:when test="${i == 1 or i == pgTotal or (i >= pgFrom and i <= pgTo)}">
				                        <li class="page-item ${i == pgCur ? 'active' : ''}">
				                            <c:choose>
				                                <c:when test="${i == pgCur}">
				                                    <span class="page-link" aria-current="page" aria-label="${i}페이지, 현재 페이지"><c:out value="${i}"/></span>
				                                </c:when>
				                                <c:otherwise>
				                                    <c:url var="pgUrl" value="/code/list.do">
				                                        <c:param name="pageIndex" value="${i}"/>
				                                        <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                                        <c:param name="name" value="${filterVO.name}"/>
				                                        <c:param name="parentCid" value="${filterVO.parentCid}"/>
				                                    </c:url>
				                                    <a class="page-link" href="<c:out value='${pgUrl}'/>" aria-label="${i}페이지로 이동"><c:out value="${i}"/></a>
				                                </c:otherwise>
				                            </c:choose>
				                        </li>
				                    </c:when>
				                    <%-- 생략(…): 숨겨진 구간의 첫 위치에서 한 번만 출력 --%>
				                    <c:when test="${(i == 2 and pgFrom > 2) or (i == pgTo + 1 and pgTo + 1 < pgTotal)}">
				                        <li class="page-item disabled"><span class="page-link" aria-hidden="true">&hellip;</span></li>
				                    </c:when>
				                </c:choose>
				            </c:forEach>

				            <%-- » 마지막 페이지 --%>
				            <li class="page-item ${pgCur < pgTotal ? '' : 'disabled'}">
				                <c:choose>
				                    <c:when test="${pgCur < pgTotal}">
				                        <c:url var="pgUrl" value="/code/list.do">
				                            <c:param name="pageIndex" value="${pgTotal}"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="name" value="${filterVO.name}"/>
				                            <c:param name="parentCid" value="${filterVO.parentCid}"/>
				                        </c:url>
				                        <a class="page-link" href="<c:out value='${pgUrl}'/>" aria-label="마지막 페이지로 이동">
				                            <span aria-hidden="true">&raquo;</span>
				                        </a>
				                    </c:when>
				                    <c:otherwise>
				                        <span class="page-link" aria-label="마지막 페이지" aria-disabled="true"><span aria-hidden="true">&raquo;</span></span>
				                    </c:otherwise>
				                </c:choose>
				            </li>
				        </ul>
				    </nav>
				</c:if>
            </div>

        </div>
    </div>

</body>
</html>
