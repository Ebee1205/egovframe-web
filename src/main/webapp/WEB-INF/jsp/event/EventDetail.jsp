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
                <input type="hidden" name="rid" value="${event.rid}">
                <input type="hidden" name="lat" value="${empty event.lat ? '' : event.lat.toPlainString()}">
                <input type="hidden" name="lon" value="${empty event.lon ? '' : event.lon.toPlainString()}">
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
	
	            // 1. 저장된 첨부파일 수집 (data-original="true"만)
	            const files = Array.from(
	                document.querySelectorAll('#eventAttachment [data-role="file"][data-original="true"]')
	            ).map(function (el) {
	                return { name: el.dataset.fileName, url: el.dataset.downloadUrl };
	            });
	
	            // 2. 이미지 파일 가져오기 → 정규화
	            const images = [];
	            for (let i = 0; i < files.length; i++) {
	                const f = files[i];
	                const ext = getExt(f.name);
	                if (IMG_EXT.indexOf(ext) > -1) {
	                    try {
	                        const res = await fetch(f.url, { credentials: 'same-origin' });
	                        if (!res.ok) throw new Error('HTTP ' + res.status);
	                        images.push(await toExcelImage(await res.blob(), ext));
	                    } catch (err) {
	                        console.error('이미지 처리 실패:', f.name, err);
	                    }
	                }
	            }
	
	            // 3. 워크북 / 시트 / 열 너비
	            const wb = new ExcelJS.Workbook();
	            const ws = wb.addWorksheet('이벤트 상세');
	            ws.columns = [{ width: 12 }, { width: 14 }, { width: 20 }, { width: 14 }, { width: 20 }];
	
	            // 4. 스타일
	            const thin = { style: 'thin', color: { argb: 'FF000000' } };
	            const border = { top: thin, bottom: thin, left: thin, right: thin };
	            const font = { name: '맑은 고딕', size: 10 };
	            const labelStyle = {
	                font: Object.assign({ bold: true }, font),
	                fill: { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFD9D9D9' } },
	                alignment: { horizontal: 'center', vertical: 'middle' },
	                border: border
	            };
	            const centerStyle = { font: font, alignment: { horizontal: 'center', vertical: 'middle' }, border: border };
	            const longStyle = { font: font, alignment: { horizontal: 'left', vertical: 'top', wrapText: true }, border: border };
	
	            // 셀 입력 + 병합 영역 전체에 테두리 적용
	            function put(range, value, style) {
	                const parts = range.split(':');
	                if (parts.length > 1) ws.mergeCells(range);
	                const cell = ws.getCell(parts[0]);
	                cell.value = value;
	                Object.assign(cell, style);   // font, fill, alignment, border
	                if (parts.length > 1) {
	                    const first = ws.getCell(parts[0]);
	                    const last = ws.getCell(parts[1]);
	                    for (let r = first.row; r <= last.row; r++) {
	                        for (let c = first.col; c <= last.col; c++) {
	                            ws.getCell(r, c).border = border;
	                        }
	                    }
	                }
	            }
	
	            // 5. 1~7행: 양식 레이아웃
	            put('A1', '이벤트 제목', labelStyle);   put('B1:E1', d.title, centerStyle);
	            put('A2', '작성일자', labelStyle);
	            put('B2', '최초 작성일', labelStyle);   put('C2', d.cDate, centerStyle);
	            put('D2', '최종 작성일', labelStyle);   put('E2', d.uDate, centerStyle);
	            put('A3', '작성자', labelStyle);        put('B3:E3', d.writer, centerStyle);
	            put('A4', '이벤트 주소', labelStyle);   put('B4:E4', d.address, centerStyle);
	            put('A5', '이벤트 기간', labelStyle);
	            put('B5', '시작일', labelStyle);        put('C5', d.sDate, centerStyle);
	            put('D5', '종료일', labelStyle);        put('E5', d.eDate, centerStyle);
	            put('A6:E6', '이벤트 내용', labelStyle);
	            put('A7:E7', d.dsc, longStyle);
	            const descriptionLines = String(d.dsc || '').split(/\r?\n/).reduce(function (total, line) {
	                return total + Math.max(1, Math.ceil(line.length / 80));
	            }, 0);
	            ws.getRow(7).height = Math.min(409, Math.max(120, descriptionLines * 15 + 10));
	            put('A8:E8', '첨부파일', labelStyle);

	            // 7. 9행~: 첨부 이미지 (한 장당 한 행, A~E 병합)
	            for (let i = 0; i < images.length; i++) {
	                const img = images[i];
	                const rowNo = 9 + i;                                        // 엑셀 행 번호(1-base)
	                const fit = Math.min(1, IMG_MAX_W / img.width, IMG_MAX_H / img.height);
	                const dw = Math.round(img.width * fit);
	                const dh = Math.round(img.height * fit);

	                put('A' + rowNo + ':E' + rowNo, '', centerStyle);
	                ws.getRow(rowNo).height = dh * 0.75 + 8;                    // px → pt + 여백

	                const imageId = wb.addImage({ buffer: img.buffer, extension: img.extension });
	                ws.addImage(imageId, {
	                    tl: { col: 0.05, row: rowNo - 1 + 0.03 },               // 0-base 위치 + 약간의 여백
	                    ext: { width: dw, height: dh },
	                    editAs: 'oneCell'
	                });
	            }

	            const statusRow = 9 + images.length;
	            put('A' + statusRow, '이벤트 상태', labelStyle);
	            put('B' + statusRow + ':E' + statusRow, d.status, centerStyle);

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
