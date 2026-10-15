<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="modal fade" id="eventCreateXlsModal" tabindex="-1" aria-labelledby="eventCreateXlsTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="eventCreateXlsTitle">이벤트 엑셀 업로드</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            
            <div class="modal-body">
                <p>엑셀 파일을 선택해 이벤트를 등록합니다.</p>
                <a href="<c:url value='/event/file/template/download.do'/>" class="mt-2 | btn | btn-sm | btn-outline">
                    이벤트 등록 엑셀 양식 다운로드
                </a>
                
                <div class="mt-4">
                    <jsp:include page="/WEB-INF/jsp/cmp/CmpFileParseBox.jsp">
                        <jsp:param name="id" value="eventExcelAttachment" />
                        <jsp:param name="uploadUrl" value="/event/file/template/upload.do" />
                        <jsp:param name="name" value="fileIds" />
                        <jsp:param name="multiple" value="false" />
                        <jsp:param name="label" value="엑셀 파일 업로드" />
                        <jsp:param name="description" value="파일을 선택하면 자동으로 업로드됩니다." />
                        <jsp:param name="accept" value=".xls,.xlsx" />
                        <jsp:param name="maxSize" value="${fileMaxSize}" />
                    </jsp:include>
                </div>
                
            </div>
            
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">업로드</button>
            </div>
        </div>
    </div>
</div>