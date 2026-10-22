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
    <link href="<c:url value='/resources/css/form.css'><c:param name='v' value='611b8dbd6edd'/></c:url>" rel="stylesheet" type="text/css">
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
            <c:set var="dcs" scope="request" value="작성자: ${event.createdByNickname}"/>
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
                <input type="hidden" name="lat" value="${empty event.lat ? '' : event.lat.toPlainString()}">
                <input type="hidden" name="lon" value="${empty event.lon ? '' : event.lon.toPlainString()}">
                <div>
                    <label for="title" class="form-label | fw-semibold">이벤트 제목</label>
                    <input type="text" class="form-control" id="title" name="title" maxlength="300" required value="<c:out value='${event.title}'/>">
                </div>
        
                <div>
                    <label for="ctg" class="form-label | fw-semibold">이벤트 카테고리</label>
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
                        <label for="sDate" class="form-label | fw-semibold">이벤트 시작일</label>
                        <input type="text" class="form-control" id="sDate" name="SDate" required maxlength="16" placeholder="yyyy-MM-dd HH:mm" value="<fmt:formatDate value='${event.SDate}' pattern='yyyy-MM-dd HH:mm'/>">
                    </div>
                    <div class="col">
                        <label for="eDate" class="form-label | fw-semibold">이벤트 종료일</label>
                        <input type="text" class="form-control" id="eDate" name="EDate" required maxlength="16" placeholder="yyyy-MM-dd HH:mm" value="<fmt:formatDate value='${event.EDate}' pattern='yyyy-MM-dd HH:mm'/>">
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
                    <textarea class="form-control" id="dsc" name="dsc" rows="3" required><c:out value="${event.dsc}"/></textarea>
                </div>
                
                <div>
                    <jsp:include page="/WEB-INF/jsp/cmp/CmpFileInputBox.jsp">
                        <jsp:param name="id" value="eventAttachment" />
                        <jsp:param name="uploadUrl" value="/event/file/upload.do" />
                        <jsp:param name="name" value="fileIds" />
                        <jsp:param name="multiple" value="true" />
                        <jsp:param name="label" value="첨부파일" />
                        <jsp:param name="description" value="이벤트 관련 파일을 여러 개 첨부할 수 있습니다. 선택한 파일은 한 번에 자동 업로드됩니다. X 버튼으로 첨부를 취소하고, 하단의 수정 버튼을 눌러 반영해주세요." />
                        <jsp:param name="accept" value="${fileAccept}" />
                        <jsp:param name="maxSize" value="${fileMaxSize}" />
                    </jsp:include>
                </div>

                <div>
                    <label for="status" class="form-label | fw-semibold">이벤트 상태</label>
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
				<c:forEach var="eventStatus" items="${eventStatuses}">
				    <c:if test="${eventStatus.code == event.status}">
				        <c:set var="eventStatusName" value="${eventStatus.name}"/>
				    </c:if>
				</c:forEach>
				
				<a id="btnExcel" class="btn | btn-outline-success" role="button" href="#"
				    data-title="<c:out value='${event.title}'/>"
				    data-writer="<c:out value='${event.createdByNickname}'/>"
				    data-c-date="<fmt:formatDate value='${event.CDate}' pattern='yyyy-MM-dd HH:mm'/>"
				    data-u-date="<fmt:formatDate value='${event.UDate}' pattern='yyyy-MM-dd HH:mm'/>"
				    data-address="<c:out value='${event.address}'/>"
				    data-s-date="<fmt:formatDate value='${event.SDate}' pattern='yyyy-MM-dd HH:mm'/>"
				    data-e-date="<fmt:formatDate value='${event.EDate}' pattern='yyyy-MM-dd HH:mm'/>"
				    data-dsc="<c:out value='${event.dsc}'/>"
				    data-status="<c:out value='${eventStatusName}'/>">엑셀 다운로드</a>
                
                <c:url var="eventDomExcelUrl" value="/event/file/detail/dom.do">
                    <c:param name="eid" value="${event.eid}"/>
                </c:url>                
                <a id="btnExcelDom" class="btn | btn-outline-success" href="<c:out value='${eventDomExcelUrl}'/>">엑셀 DOM 다운로드</a>
				    
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
    
	<script src="https://cdn.jsdelivr.net/npm/exceljs@4.4.0/dist/exceljs.min.js"></script>
	<script>
	
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

	

	    const IMG_EXT = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
	    const IMG_MAX_W = 570;    // 엑셀 A~E열 전체 너비(px)에 맞춤
	    const IMG_MAX_H = 400;    // 이미지 한 장의 최대 표시 높이(px)
	    const IMG_SRC_MAX = 1600; // 원본이 너무 크면 이 크기로 축소해 용량 절약
	
	    function getExt(name) {
	        const i = (name || '').lastIndexOf('.');
	        return i < 0 ? '' : name.substring(i + 1).toLowerCase();
	    }
	
	    function blobToImage(blob) {
	        return new Promise(function (resolve, reject) {
	            const url = URL.createObjectURL(blob);
	            const img = new Image();
	            img.onload = function () { URL.revokeObjectURL(url); resolve(img); };
	            img.onerror = function () { URL.revokeObjectURL(url); reject(new Error('이미지 디코딩 실패')); };
	            img.src = url;
	        });
	    }
	
	    // 어떤 포맷이든 canvas를 거쳐 jpeg/png로 정규화 (webp·bmp 대응, EXIF 회전 반영, 용량 축소)
	    async function toExcelImage(blob, ext) {
	        const img = await blobToImage(blob);
	        const scale = Math.min(1, IMG_SRC_MAX / Math.max(img.naturalWidth, img.naturalHeight));
	        const w = Math.max(1, Math.round(img.naturalWidth * scale));
	        const h = Math.max(1, Math.round(img.naturalHeight * scale));
	        const isJpeg = (ext === 'jpg' || ext === 'jpeg');
	
	        const canvas = document.createElement('canvas');
	        canvas.width = w;
	        canvas.height = h;
	        const ctx = canvas.getContext('2d');
	        if (isJpeg) { ctx.fillStyle = '#ffffff'; ctx.fillRect(0, 0, w, h); }
	        ctx.drawImage(img, 0, 0, w, h);
	
	        const out = await new Promise(function (res) {
	            canvas.toBlob(res, isJpeg ? 'image/jpeg' : 'image/png', 0.92);
	        });
	        return { buffer: await out.arrayBuffer(), extension: isJpeg ? 'jpeg' : 'png', width: w, height: h };
	    }
	
	    async function downloadExcel() {
	        const btn = document.getElementById('btnExcel');
	        if (btn.getAttribute('aria-busy') === 'true') return;
	        btn.setAttribute('aria-busy', 'true');
	        btn.classList.add('disabled');
	
	        try {
	            if (typeof ExcelJS === 'undefined') throw new Error('엑셀 라이브러리를 불러오지 못했습니다.');
	            const d = btn.dataset;
	
		            // 서버가 공통 XML 양식으로 만든 파일을 유지하고 이미지 표시만 추가한다.
            const sourceLink = document.getElementById('btnExcelDom');
            const workbookResponse = await fetch(sourceLink.href, { credentials: 'same-origin' });
            if (!workbookResponse.ok) throw new Error('양식 다운로드 실패 (HTTP ' + workbookResponse.status + ')');
            const wb = new ExcelJS.Workbook();
            await wb.xlsx.load(await workbookResponse.arrayBuffer());
            // POI의 기본 파일에는 테마가 없을 수 있다. ExcelJS가 빈 테마 참조를 쓰지 않도록 기본 테마를 사용한다.
            if (!Object.keys(wb.model.themes || {}).length) wb.clearThemes();
            const ws = wb.worksheets[0];
            if (!ws) throw new Error('다운로드한 엑셀에 시트가 없습니다.');

            // 행 번호를 별도로 정의하지 않고 XML 양식에 출력된 첨부파일 행을 찾는다.
            let attachmentHeaderRow = 0;
            const rowsByFileName = new Map();
            ws.eachRow(function (row) {
                const cell = row.getCell(1);
                if (!attachmentHeaderRow && cell.value === '첨부파일' && cell.isMerged) {
                    attachmentHeaderRow = row.number;
                } else if (attachmentHeaderRow && row.number > attachmentHeaderRow && cell.isMerged) {
                    const fileName = String(cell.value || '');
                    if (!rowsByFileName.has(fileName)) rowsByFileName.set(fileName, []);
                    rowsByFileName.get(fileName).push(row.number);
                }
            });
            // 1. 저장된 첨부파일 수집 (data-original="true"만)
	            const files = Array.from(
	                document.querySelectorAll('#eventAttachment [data-role="file"][data-original="true"]')
	            ).map(function (el) {
	                return { name: el.dataset.fileName, url: el.dataset.downloadUrl };
	            }).filter(function (file) { return rowsByFileName.has(file.name); });
	
	            // 2. 이미지 파일 가져오기 → 정규화
	            const attachments = files.map(function (file) {
	                return { name: file.name, image: null };
	            });
	            for (let i = 0; i < files.length; i++) {
	                const f = files[i];
	                const ext = getExt(f.name);
	                if (IMG_EXT.indexOf(ext) > -1) {
	                    try {
	                        const res = await fetch(f.url, { credentials: 'same-origin' });
	                        if (!res.ok) throw new Error('HTTP ' + res.status);
	                        attachments[i].image = await toExcelImage(await res.blob(), ext);
	                    } catch (err) {
	                        console.error('이미지 처리 실패:', f.name, err);
	                    }
	                }
	            }
	
            for (const attachment of attachments) {
                const matchingRows = rowsByFileName.get(attachment.name);
                if (!matchingRows || !matchingRows.length) continue;
                const rowNo = matchingRows.shift();
                if (!attachment.image) continue;
                const img = attachment.image;
                const fit = Math.min(1, IMG_MAX_W / img.width, IMG_MAX_H / img.height);
                const dw = Math.round(img.width * fit);
                const dh = Math.round(img.height * fit);
                ws.getRow(rowNo).height = dh * 0.75 + 8;
                const imageId = wb.addImage({ buffer: img.buffer, extension: img.extension });
                ws.addImage(imageId, {
                    tl: { col: 0.05, row: rowNo - 1 + 0.03 },
                    ext: { width: dw, height: dh },
                    editAs: 'oneCell'
                });
            }
            // 8. 다운로드
	            const buffer = await wb.xlsx.writeBuffer();
	            const blob = new Blob([buffer], {
	                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
	            });
	            const safeTitle = (d.title || '이벤트').replace(/[\\/:*?"<>|]/g, '_').substring(0, 50);
	            const url = URL.createObjectURL(blob);
	            const a = document.createElement('a');
	            a.href = url;
	            a.download = '이벤트_' + safeTitle + '.xlsx';
	            document.body.appendChild(a);
	            a.click();
	            document.body.removeChild(a);
	            setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
	        } catch (e) {
	            console.error(e);
	            alert('엑셀 다운로드 중 오류가 발생했습니다.\n' + e.message);
	        } finally {
	            btn.setAttribute('aria-busy', 'false');
	            btn.classList.remove('disabled');
	        }
	    }
	
	    document.getElementById('btnExcel').addEventListener('click', function (e) {
	        e.preventDefault();
	        downloadExcel();
	    });
		    	
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
