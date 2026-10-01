<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="validator" uri="http://www.springmodules.org/tags/commons-validator"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이벤트 상세</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>" />
    <link href="<c:url value='/resources/css/base.css'/>" rel="stylesheet" type="text/css">
    <link href="<c:url value='/resources/css/form.css'/>" rel="stylesheet" type="text/css">
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
            <c:set var="path" scope="request" value="홈/이벤트/상세"/>
            <c:set var="title" scope="request" value="${event.title}"/>
            <c:set var="dcs" scope="request" value="등록자: ${event.createdBy}"/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />
        </div>

        <div class="p-3 | align-items-center">
            <form id="eventUpdateForm" action="<c:url value='/event/update.do'/>" method="post"
                class="form-container | js-update-form" accept-charset="UTF-8"
                novalidate
                data-confirm-text="이벤트를 수정하시겠습니까?" 
                data-success-text="수정이 완료되었습니다."
            >
                <input type="hidden" name="eid" value="${event.eid}">
                <input type="hidden" name="rid" value="${event.rid}">
                <input type="hidden" name="lat" value="${empty event.lat ? '' : event.lat.toPlainString()}">
                <input type="hidden" name="lon" value="${empty event.lon ? '' : event.lon.toPlainString()}">
                <input type="hidden" name="fileId" value="${event.fileId}">
                <div>
                    <label for="title" class="form-label">이벤트 제목</label>
                    <input type="text" class="form-control" id="title" name="title" maxlength="300" required value="<c:out value='${event.title}'/>">
                </div>
        
                <div>
                    <label for="ctg" class="form-label">이벤트 카테고리</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="카테고리 선택"
                        id="ctg" name="ctg" required>
                        <option value="">전체</option>
                        <c:forEach var="eventCtg" items="${eventCtgs}">
                            <option value="${eventCtg.code}" ${eventCtg.code == event.ctg ? 'selected' : ''}>
                                <c:out value="${eventCtg.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
        
                <div class="row">
                    <div class="col">
                        <label for="sDate" class="form-label">이벤트 시작일</label>
                        <input type="text" class="form-control" id="sDate" name="SDate" required maxlength="16" placeholder="yyyy-MM-dd HH:mm" value="<fmt:formatDate value='${event.SDate}' pattern='yyyy-MM-dd HH:mm'/>">
                    </div>
                    <div class="col">
                        <label for="eDate" class="form-label">이벤트 종료일</label>
                        <input type="text" class="form-control" id="eDate" name="EDate" required maxlength="16" placeholder="yyyy-MM-dd HH:mm" value="<fmt:formatDate value='${event.EDate}' pattern='yyyy-MM-dd HH:mm'/>">
                    </div>
                </div>
        
                <!-- <div class="mb-3">
                    <label for="exampleFormControlTextarea1" class="form-label">이벤트 지역</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="지역 선택"
                        id="region" name="region">
                        <option value="">전체</option>
                        <c:forEach var="eventRegion" items="${eventRegions}">
                            <option value="${eventRegion.code}" ${eventRegion.code == filterVO.region ? 'selected' : ''}>
                                <c:out value="${eventRegion.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div> -->
        
                <div>
                    <label for="address" class="form-label">이벤트 주소</label>
                    <input type="text" class="form-control" id="address" name="address" maxlength="500" value="<c:out value='${event.address}'/>">
                </div>
        
                <div>
                    <label for="dsc" class="form-label">이벤트 내용</label>
                    <textarea class="form-control" id="dsc" name="dsc" rows="3" required><c:out value="${event.dsc}"/></textarea>
                </div>

                <div>
                    <label for="status" class="form-label">이벤트 상태</label>
                    <select 
                        class="form-select | input-box" 
                        placeholder="상태 선택"
                        id="status" name="status" required>
                        <option value="">전체</option>
                        <c:forEach var="eventStatus" items="${eventStatuses}">
                            <option value="${eventStatus.code}" ${eventStatus.code == event.status ? 'selected' : ''}>
                                <c:out value="${eventStatus.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

            </form>
        </div>

        <!-- 폼 하단 버튼박스 -->
        <div class="form-info">
            <div>
                <p class="form-info-text">최초 등록일: <fmt:formatDate value="${event.CDate}" pattern="yyyy-MM-dd HH:mm"/></p>
                <p class="form-info-text">최종 수정일: <fmt:formatDate value="${event.UDate}" pattern="yyyy-MM-dd HH:mm"/></p>
            </div>
            <div>
                <button type="submit" form="eventUpdateForm" class="btn | btn-outline-secondary">수정</button>
                <form action="<c:url value='/event/delete.do'/>" 
                    method="post" class="js-confirm-form d-inline"
                    data-confirm-text="이벤트를 삭제하시겠습니까?" 
                    data-success-text="삭제가 완료되었습니다."
                    data-success-url="<c:url value='/event/list.do'/>"
                >
                    <input type="hidden" name="eid" value="${event.eid}">
                    <button type="submit" class="btn | btn-outline-secondary">삭제</button>
                </form>
            </div>
        </div>
        

        <div class="p-3 | align-items-center">
            <jsp:include page="/WEB-INF/jsp/cmp/CmpCmtMngBox.jsp">
                <jsp:param name="insertUrl" value="/event/cmt/insert.do" />
                <jsp:param name="updateUrl" value="/event/cmt/update.do" />
                <jsp:param name="deleteUrl" value="/event/cmt/delete.do" />
                <jsp:param name="targetIdParam" value="eid" />
                <jsp:param name="targetId" value="${event.eid}" />
                <jsp:param name="commentsAttribute" value="comments" />
                <jsp:param name="replyMapAttribute" value="replyMap" />
            </jsp:include>
        </div>

        <jsp:include page="/WEB-INF/jsp/cmp/CmpYNDialog.jsp" />
        
    </div>
    
    
    <validator:javascript formName="eventVO" method="validateEventVO" staticJavascript="true" dynamicJavascript="true" xhtml="true" cdata="false"/>
    <script>
        window.alert = function (message) {
            CmpDialog.open({
                title: '입력 확인',
                text: String(message).replace(/\r?\n/g, '<br>'),
                isOneButton: true,
                okButton: function () {
                    CmpDialog.close();
                }
            });
        };

        document.getElementById('eventUpdateForm').addEventListener('submit', function (event) {
            if (!validateEventVO(this)) {
                event.preventDefault();
                event.stopImmediatePropagation();
            }
        });
    </script>

</body>
</html>

