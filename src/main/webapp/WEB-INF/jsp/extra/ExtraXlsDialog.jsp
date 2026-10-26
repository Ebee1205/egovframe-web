<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="modal fade" id="extraXlsModal" tabindex="-1" aria-labelledby="extraXlsTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            
            <div class="modal-header">
                <h2 class="modal-title | fs-5" id="extraXlsTitle">일괄 업로드</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            
            <div class="modal-body">
                <p id="extraExcelHelp" class="text-body-secondary">화면 확인용입니다. 파일을 선택해도 업로드하거나 저장하지 않으며, 아래에는 목록과 동일한 목업 데이터 15건이 표시됩니다.</p>
                
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
                
                
                <div class="table-container">
                    <div class="table-info">
                        <p class="table-info-text">총 15건</p>
                    </div>
                    <jsp:include page="/WEB-INF/jsp/extra/ExtraMockTable.jsp" />
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                <button type="button" class="btn btn-primary" disabled>등록</button>
            </div>
        </div>
    </div>
</div>
