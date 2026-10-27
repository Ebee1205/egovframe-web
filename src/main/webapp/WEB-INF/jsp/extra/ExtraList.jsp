<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>따릉이 대여이력 목록</title>
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
            <c:set var="path" scope="request" value="홈/따릉이 대여이력 목록"/>
            <c:set var="title" scope="request" value="따릉이 대여이력 목록"/>
            <c:set var="dcs" scope="request" value="등록된 따릉이 대여이력을 검색하고 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/extra/list.do'/>">
                <input type="hidden" name="pageIndex" value="1" />
                <input type="hidden" name="pageSize" value="<c:out value='${filterVO.pageSize}'/>" />
                <div class="d-flex | flex-wrap | align-items-center | gap-2">
                    <input class="form-control | input-box" type="search" name="bikeId" aria-label="자전거ID" placeholder="자전거ID 검색" value="<c:out value='${filterVO.bikeId}'/>" />
                    <input class="form-control | input-box" type="search" name="rentId" aria-label="대여소번호" placeholder="대여소번호 검색" value="<c:out value='${filterVO.rentId}'/>" />
                    <input class="form-control | input-box" type="search" name="rtnId" aria-label="반납대여소번호" placeholder="반납대여소번호 검색" value="<c:out value='${filterVO.rtnId}'/>" />
                </div>
                <button type="submit" class="btn | btn-primary">검색</button>
            </form>

            <div class="table-container">
                <div class="table-info">
                    <p class="table-info-text">총 <c:out value="${filterVO.totalCnt}"/>건</p>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn | btn-outline-success" disabled>엑셀 다운로드</button>
                        <button type="button" class="btn | btn-outline-secondary"
                                data-coreui-toggle="modal" data-coreui-target="#extraXlsModal">일괄 등록</button>
                    </div>
                </div>
                <div class="table-responsive">
                    <table id="extraListTable" class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center | text-nowrap">
                            <tr>
                                <th scope="col">NO</th>
                                <th scope="col">자전거ID</th>
                                <th scope="col">대여일시</th>
                                <th scope="col">대여소번호</th>
                                <th scope="col">대여소명</th>
                                <th scope="col">대여 거치대</th>
                                <th scope="col">반납일시</th>
                                <th scope="col">반납대여소번호</th>
                                <th scope="col">반납대여소명</th>
                                <th scope="col">반납거치대</th>
                                <th scope="col">사용시간(분)</th>
                                <th scope="col">이용거리(m)</th>
                                <th scope="col">사용자종류</th>
                                <th scope="col">성별</th>
                                <th scope="col">생년</th>
                                <th scope="col">대여대여소ID</th>
                                <th scope="col">반납대여소ID</th>
                                <th scope="col">자전거 구분</th>
                                <th scope="col">등록일시</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty extras}">
                                    <tr><td class="text-center | text-body-secondary | py-4" colspan="19">조회된 데이터가 없습니다.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="extra" items="${extras}" varStatus="rowStatus">
                                        <tr>
                                            <td class="text-center"><c:out value="${filterVO.firstIndex + rowStatus.count}"/></td>
                                            <td><c:out value="${extra.bikeId}"/></td>
                                            <td><fmt:formatDate value="${extra.rentDt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                            <td><c:out value="${extra.rentId}"/></td>
                                            <td><c:out value="${extra.rentNm}"/></td>
                                            <td><c:out value="${extra.rentHold}"/></td>
                                            <td><fmt:formatDate value="${extra.rtnDt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                            <td><c:out value="${extra.rtnId}"/></td>
                                            <td><c:out value="${extra.rtnNm}"/></td>
                                            <td><c:out value="${extra.rtnHold}"/></td>
                                            <td><c:out value="${extra.useMin}"/></td>
                                            <td><c:out value="${extra.useDst}"/></td>
                                            <td><c:out value="${extra.usrClsCd}"/></td>
                                            <td><c:out value="${extra.sexCd}"/></td>
                                            <td><c:out value="${extra.birthYear}"/></td>
                                            <td><c:out value="${extra.rentStationId}"/></td>
                                            <td><c:out value="${extra.returnStationId}"/></td>
                                            <td><c:out value="${extra.bikeSeCd}"/></td>
                                            <td><fmt:formatDate value="${extra.cDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
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

				    <nav aria-label="따릉이 대여이력 목록 페이징">
				        <ul class="pagination | justify-content-center | flex-wrap">

				            <%-- « 첫 페이지 --%>
				            <li class="page-item ${pgCur > 1 ? '' : 'disabled'}">
				                <c:choose>
				                    <c:when test="${pgCur > 1}">
				                        <c:url var="pgUrl" value="/extra/list.do">
				                            <c:param name="pageIndex" value="1"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="bikeId" value="${filterVO.bikeId}"/>
				                            <c:param name="rentId" value="${filterVO.rentId}"/>
				                            <c:param name="rtnId" value="${filterVO.rtnId}"/>
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
				                                    <c:url var="pgUrl" value="/extra/list.do">
				                                        <c:param name="pageIndex" value="${i}"/>
				                                        <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                                        <c:param name="bikeId" value="${filterVO.bikeId}"/>
				                                        <c:param name="rentId" value="${filterVO.rentId}"/>
				                                        <c:param name="rtnId" value="${filterVO.rtnId}"/>
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
				                        <c:url var="pgUrl" value="/extra/list.do">
				                            <c:param name="pageIndex" value="${pgTotal}"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="bikeId" value="${filterVO.bikeId}"/>
				                            <c:param name="rentId" value="${filterVO.rentId}"/>
				                            <c:param name="rtnId" value="${filterVO.rtnId}"/>
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



    <jsp:include page="/WEB-INF/jsp/extra/ExtraXlsDialog.jsp" />
    <script src="<c:url value='/resources/coreui-5.8.0-dist/js/coreui.bundle.min.js'/>"></script>

</body>
</html>
