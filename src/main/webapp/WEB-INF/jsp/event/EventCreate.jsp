<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="validator" uri="http://www.springmodules.org/tags/commons-validator"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이벤트 등록</title>
    <link rel="stylesheet" href="<c:url value='/resources/coreui-5.8.0-dist/css/coreui.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/base.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/form.css'><c:param name='v' value='611b8dbd6edd'/></c:url>">
</head>
<body>
    <noscript>자바스크립트를 지원하지 않는 브라우저에서는 일부 기능을 사용하실 수 없습니다.</noscript>
    <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpLeftnav.jsp" />
    <div class="wrapper | bg-body | main-content">
        <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpHeader.jsp" />
        <div class="p-3 | align-items-center">
            <c:set var="path" scope="request" value="홈/이벤트/등록"/>
            <c:set var="title" scope="request" value="이벤트 등록"/>
            <c:set var="dcs" scope="request" value="새로운 이벤트를 등록합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />
        </div>

        <div class="form-btn-box">
            <a href="<c:url value='/event/list.do'/>" class="btn | btn-outline-secondary">목록</a>
            <button id="btnExcelDialog" type="button" class="btn | btn-outline-success">엑셀 업로드</button>
        </div>

        <div class="p-3 | align-items-center">
            <c:url var="insertUrl" value="/event/insert.do"/>
            <form:form id="eventCreateForm" modelAttribute="eventCreateVO" action="${insertUrl}"
                       method="post" cssClass="form-container" accept-charset="UTF-8" novalidate="novalidate">
                <input type="hidden" name="reg_id" value="1">
                <input type="hidden" name="lat" value="">
                <input type="hidden" name="lon" value="">
                <div>
                    <label for="title" class="form-label | fw-semibold">이벤트 제목</label>
                    <form:input path="title" id="title" cssClass="form-control" maxlength="300" required="required" aria-describedby="titleError"/>
                    <form:errors path="title" id="titleError" cssClass="text-danger" element="div" htmlEscape="true"/>
                </div>
                <div>
                    <label for="ctg" class="form-label | fw-semibold">이벤트 카테고리</label>
                    <form:select path="ctg" id="ctg" cssClass="form-select | input-box" required="required" aria-describedby="ctgError">
                        <form:option value="" label="카테고리 선택"/>
                        <form:options items="${eventCtgs}" itemValue="code" itemLabel="name"/>
                    </form:select>
                    <form:errors path="ctg" id="ctgError" cssClass="text-danger" element="div" htmlEscape="true"/>
                </div>
                <div class="row">
                    <div class="col">
                        <label for="sDate" class="form-label | fw-semibold">이벤트 시작일</label>
                        <form:input path="SDate" id="sDate" cssClass="form-control" required="required" maxlength="16" placeholder="yyyy-MM-dd HH:mm" aria-describedby="sDateError"/>
                        <form:errors path="SDate" id="sDateError" cssClass="text-danger" element="div" htmlEscape="true"/>
                    </div>
                    <div class="col">
                        <label for="eDate" class="form-label | fw-semibold">이벤트 종료일</label>
                        <form:input path="EDate" id="eDate" cssClass="form-control" required="required" maxlength="16" placeholder="yyyy-MM-dd HH:mm" aria-describedby="eDateError"/>
                        <form:errors path="EDate" id="eDateError" cssClass="text-danger" element="div" htmlEscape="true"/>
                    </div>
                </div>
                
                <c:set var="eventParentRegId" value=""/>
                <c:forEach var="reg" items="${childRegs}">
                    <c:if test="${reg.reg_id == event.reg_id}">
                        <c:set var="eventParentRegId" value="${reg.parentRegId}"/>
                    </c:if>
                </c:forEach>
            	<div class="d-flex | gap-4 | w-100">        		
	                <div>
	                    <label for="parentRegId" class="form-label | fw-semibold">이벤트 지역</label>
	                    <div class="d-flex | gap-2">
		                    <select class="form-select | input-box" id="parentRegId" name="parentRegId" aria-label="시도 선택">
		                        <option value="">전체 지역</option>
		                        <c:forEach var="reg" items="${rootRegs}">
		                            <option value="${reg.reg_id}" ${reg.reg_id == eventParentRegId || reg.reg_id == event.reg_id ? 'selected' : ''}>
		                                <c:out value="${reg.name}"/>
		                            </option>
		                        </c:forEach>
		                    </select>
		                    <select class="form-select | input-box" id="reg_id" name="reg_id" aria-label="시군구 선택"
		                        ${empty eventParentRegId ? 'disabled' : ''}>
		                        <option value="">전체 시(구)</option>
		                        <c:forEach var="reg" items="${childRegs}">
		                            <c:if test="${not empty reg.parentRegId}">
		                                <option value="${reg.reg_id}" data-parent-id="${reg.parentRegId}"
		                                    ${reg.reg_id == event.reg_id ? 'selected' : ''}>
		                                    <c:out value="${reg.name}"/>
		                                </option>
		                            </c:if>
		                        </c:forEach>
		                    </select>
	                    </div>
	                </div>
	                <div class="w-100">
	                    <label for="address" class="form-label | fw-semibold">이벤트 주소</label>
	                    <input type="text" class="form-control" id="address" name="address" maxlength="500" value="<c:out value='${event.address}'/>">
	                </div>
        		</div>
        		
                <div>
                    <label for="dsc" class="form-label | fw-semibold">이벤트 내용</label>
                    <form:textarea path="dsc" id="dsc" cssClass="form-control" rows="3" maxlength="1000" required="required" aria-describedby="dscError"/>
                    <form:errors path="dsc" id="dscError" cssClass="text-danger" element="div" htmlEscape="true"/>
                </div>
                
                <div>
                    <jsp:include page="/WEB-INF/jsp/cmp/CmpFileInputBox.jsp">
                        <jsp:param name="id" value="eventAttachment" />
                        <jsp:param name="uploadUrl" value="/event/file/upload.do" />
                        <jsp:param name="name" value="fileIds" />
                        <jsp:param name="multiple" value="true" />
                        <jsp:param name="label" value="첨부 파일" />
                        <jsp:param name="accept" value="${fileAccept}" />
                        <jsp:param name="maxSize" value="${fileMaxSize}" />
                    </jsp:include>
                </div>
                
                <div>
                    <label for="status" class="form-label | fw-semibold">이벤트 상태</label>
                    <form:select path="status" id="status" cssClass="form-select | input-box" required="required" aria-describedby="statusError">
                        <form:option value="" label="상태 선택"/>
                        <form:options items="${eventStatuses}" itemValue="code" itemLabel="name"/>
                    </form:select>
                    <form:errors path="status" id="statusError" cssClass="text-danger" element="div" htmlEscape="true"/>
                </div>
                <div>
                    <label for="createdBy" class="form-label | fw-semibold">등록자 ID</label>
                    <form:input path="createdBy" id="createdBy" cssClass="form-control" inputmode="numeric" required="required" aria-describedby="createdByError"/>
                    <form:errors path="createdBy" id="createdByError" cssClass="text-danger" element="div" htmlEscape="true"/>
                </div>
            </form:form>
        </div>

        <div class="form-info">
            <div></div>
            <div>
                <button type="submit" form="eventCreateForm" class="btn | btn-outline-secondary">등록</button>
            </div>
        </div>
        <jsp:include page="/WEB-INF/jsp/event/EventCreateXlsDialog.jsp" />
        <jsp:include page="/WEB-INF/jsp/cmp/CmpYNDialog.jsp" />
    </div>

    <validator:javascript formName="eventCreateVO" method="validateEventCreateVO" staticJavascript="true" dynamicJavascript="true" xhtml="true" cdata="false"/>
    <script>
        (function () {
            var excelDialogButton = document.getElementById('btnExcelDialog');
            var excelDialogElement = document.getElementById('eventCreateXlsModal');
            var excelDialog = coreui.Modal.getOrCreateInstance(excelDialogElement);

            excelDialogButton.addEventListener('click', function () {
                excelDialog.show();
            });
        })();

	    const parentRegion = document.getElementById('parentRegId');
	    const childRegion = document.getElementById('reg_id');
	
	    function updateChildRegions(resetSelection) {
	        const parentId = parentRegion.value;
	        childRegion.disabled = !parentId;
	        if (resetSelection) { childRegion.value = ''; }
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
        

        (function () {
            var form = document.getElementById('eventCreateForm');
            form.addEventListener('submit', function (event) {
                event.preventDefault();
                if (!validateEventCreateVO(form)) { return; }
                CmpDialog.open({
                    title: '등록 확인',
                    text: '이벤트를 등록하시겠습니까?',
                    okText: '등록',
                    cancelText: '취소',
                    okButton: function () {
                        CmpDialog.close();
                        HTMLFormElement.prototype.submit.call(form);
                    }
                });
            });
        })();
    </script>
</body>
</html>
