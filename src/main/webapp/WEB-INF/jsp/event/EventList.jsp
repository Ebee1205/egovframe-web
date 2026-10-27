<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이벤트 목록</title>
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
            <c:set var="path" scope="request" value="홈/이벤트 목록"/>
            <c:set var="title" scope="request" value="이벤트 목록"/>
            <c:set var="dcs" scope="request" value="등록된 전체 이벤트를 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/event/list.do'/>">
                <input type="hidden" name="pageIndex" value="1" />

                <div class="row | m-0 | gap-2 | mb-2">
                    <select class="form-select | input-box" id="ctg" name="ctg">
                        <option value="">전체 분류</option>
                        <c:forEach var="eventCtg" items="${eventCtgs}">
                            <option value="${eventCtg.code}" ${eventCtg.code == filterVO.ctg ? 'selected' : ''}>
                                <c:out value="${eventCtg.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <select class="form-select | input-box" id="parentRegId" name="parentRegId" aria-label="시도 선택">
                        <option value="">전체 지역</option>
                        <c:forEach var="reg" items="${rootRegs}">
                            <option value="${reg.reg_id}" ${reg.reg_id == filterVO.parentRegId ? 'selected' : ''}>
                                <c:out value="${reg.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <select class="form-select | input-box" id="reg_id" name="reg_id" aria-label="시군구 선택"
                        ${empty filterVO.parentRegId ? 'disabled' : ''}>
                        <option value="">전체 시(구)</option>
                        <c:forEach var="reg" items="${childRegs}">
                            <c:if test="${not empty reg.parentRegId}">
                                <option value="${reg.reg_id}" data-parent-id="${reg.parentRegId}"
                                    ${reg.reg_id == filterVO.reg_id ? 'selected' : ''}>
                                    <c:out value="${reg.name}"/>
                                </option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <div class="d-flex | justify-content-between | align-items-center | gap-2 | w-100">
                    <input
                        class="form-control | input-box"
                        type="text"
                        placeholder="제목 검색"
                        id="title" name="title"
                        value="${filterVO.title}"
                    >
                    <button type="submit" class="btn | btn-primary | flex-shrink-0">검색</button>
                </div>
            </form>

            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info">
                    <p class="table-info-text">총 <c:out value="${filterVO.totalCnt}"/>건</p>
                    <div>                    
	                    <button type="submit" form="searchForm" formaction="<c:url value='/event/file/list/download.do'/>" class="btn | btn-outline-success">엑셀 다운로드</button>
	                    <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">신규 등록</a>
                    </div>
                </div>

                <!-- 테이블 본문 -->
                <div class="table-responsive">
                    <table class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center">
                            <tr>
                                <th scope="col" style="width: 45px;">NO</th>
                                <th scope="col" style="width: 100px;">지역</th>
                                <th scope="col" style="width: 200px;">제목</th>
                                <th scope="col" style="width: 80px;">분류</th>
                                <th scope="col">시작일</th>
                                <th scope="col">종료일</th>
                                <th scope="col">주소</th>
                                <th scope="col">등록일</th>
                                <th scope="col">수정일</th>
                                <th scope="col" style="width: 60px;">상태</th>
                                <th scope="col" style="width: 80px;">action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty events}">
                                    <tr><td class="text-center | text-body-secondary | py-4" colspan="10">조회된 데이터가 없습니다.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="event" items="${events}" varStatus="rowStatus">
                                        <tr>
                                            <td class="text-center"><c:out value="${filterVO.firstIndex + rowStatus.count}"/></td>
                                            <td><c:out value="${event.regName}"/></td>

                                            <td><c:out value="${event.title}"/></td>
                                            <td>
                                                <c:set var="displayCtg" value="${event.ctg}"/>
                                                <c:forEach var="eventCtg" items="${eventCtgs}">
                                                    <c:if test="${event.ctg eq eventCtg.code}">
                                                        <c:set var="displayCtg" value="${eventCtg.name}"/>
                                                    </c:if>
                                                </c:forEach>
                                                <c:out value="${displayCtg}"/>
                                            </td>

                                            <td><fmt:formatDate value="${event.SDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><fmt:formatDate value="${event.EDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><c:out value="${event.address}"/></td>
                                            <td><fmt:formatDate value="${event.CDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><fmt:formatDate value="${event.UDate}" pattern="yyyy-MM-dd HH:mm"/></td>

                                            <td>
                                                <c:set var="displayStatus" value="${event.status}"/>
                                                <c:forEach var="eventStatus" items="${eventStatuses}">
                                                    <c:if test="${event.status eq eventStatus.code}">
                                                        <c:set var="displayStatus" value="${eventStatus.name}"/>
                                                    </c:if>
                                                </c:forEach>
                                                <c:out value="${displayStatus}"/>
                                            </td>

                                            <td><a href="<c:url value='/event/detail.do'><c:param name='eid' value='${event.eid}'/></c:url>" class="btn | btn-outline-primary">상세</a></td>

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

				    <nav aria-label="이벤트 목록 페이징">
				        <ul class="pagination | justify-content-center | flex-wrap">

				            <%-- « 첫 페이지 --%>
				            <li class="page-item ${pgCur > 1 ? '' : 'disabled'}">
				                <c:choose>
				                    <c:when test="${pgCur > 1}">
				                        <c:url var="pgUrl" value="/event/list.do">
				                            <c:param name="pageIndex" value="1"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="title" value="${filterVO.title}"/>
				                            <c:param name="ctg" value="${filterVO.ctg}"/>
				                            <c:param name="reg_id" value="${filterVO.reg_id}"/>
				                            <c:param name="parentRegId" value="${filterVO.parentRegId}"/>
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
				                                    <c:url var="pgUrl" value="/event/list.do">
				                                        <c:param name="pageIndex" value="${i}"/>
				                                        <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                                        <c:param name="title" value="${filterVO.title}"/>
				                                        <c:param name="ctg" value="${filterVO.ctg}"/>
				                                        <c:param name="reg_id" value="${filterVO.reg_id}"/>
				                                        <c:param name="parentRegId" value="${filterVO.parentRegId}"/>
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
				                        <c:url var="pgUrl" value="/event/list.do">
				                            <c:param name="pageIndex" value="${pgTotal}"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="title" value="${filterVO.title}"/>
				                            <c:param name="ctg" value="${filterVO.ctg}"/>
				                            <c:param name="reg_id" value="${filterVO.reg_id}"/>
				                            <c:param name="parentRegId" value="${filterVO.parentRegId}"/>
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


    <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
        
    </div>
</body>

<script>
(function () {
    const parentRegion = document.getElementById('parentRegId');
    const childRegion = document.getElementById('reg_id');

    function updateChildRegions(resetSelection) {
        const parentId = parentRegion.value;
        childRegion.disabled = !parentId;
        if (resetSelection) { 
        	childRegion.value = ''; 
        }
        Array.from(childRegion.options).forEach(function (option) {
            if (option.value) {
                option.hidden = !parentId || option.dataset.parentId !== parentId;
            }
        });
        if (childRegion.selectedOptions.length && childRegion.selectedOptions[0].hidden) {
            childRegion.value = '';
        }
    }

    parentRegion.addEventListener('change', function () {
        updateChildRegions(true);
    });
    updateChildRegions(false);

})();
</script>

</html>
