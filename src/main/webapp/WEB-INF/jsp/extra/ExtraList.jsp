<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
        

    <!-- Leftnav -->
    <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpLeftnav.jsp" />
    <!--// Leftnav -->

    <div class="wrapper | bg-body | main-content">
        <!-- Header -->
        <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpHeader.jsp" />
        <!--// Header -->
        
        <div class="p-3 | align-items-center">
            <c:set var="path" scope="request" value="홈/엑셀 등록 테스트"/>
            <c:set var="title" scope="requezst" value="엑셀 등록 테스트"/>
            <c:set var="dcs" scope="request" value="등록된 전체 목록을 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info">
                    <p class="table-info-text">총 <c:out value="${filterVO.totalCnt}"/>건</p>
                    <div>                    
	                    <button type="submit" form="searchForm" formaction="<c:url value='/event/file/list/download.do'/>" class="btn | btn-outline-success">엑셀 다운로드</button>
	                    <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">일괄 등록</a>
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
                                <th scope="col">좌표1</th>
                                <th scope="col">좌표2</th>
                                <th scope="col">ㅁㄴㅇㄹ</th>
                                <th scope="col">수정일</th>
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
