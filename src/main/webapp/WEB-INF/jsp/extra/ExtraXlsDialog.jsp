<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<style>
    #extraXlsModal .extra-excel-valid {
        color: #6610f2;
    }
    #extraXlsModal .extra-excel-error {
        color: #dc3545;
    }
    #extraXlsModal .extra-excel-warning {
        color: #fd7e14;
    }
    #extraXlsModal .modal-content {
        height: 100%;
    }
    #extraXlsModal .modal-body {
        flex: 0 0 auto;
        overflow: visible;
    }
    #extraXlsModal .table-container {
        display: flex;
        flex-direction: column;
        align-items: stretch;
        flex: 1 1 0;
        min-height: 0;
        min-width: 0;
        overflow: hidden;
    }
    #extraXlsModal .table-info,
    #extraExcelPaging {
        flex: 0 0 auto;
    }
    #extraXlsModal .extra-excel-scroll {
        flex: 1 1 0;
        min-height: 0;
        min-width: 0;
        overflow: auto;
        overflow-anchor: none;
    }
    #extraExcelTable {
        width: max-content;
        min-width: 100%;
        table-layout: auto;
    }
    #extraExcelTable thead {
        position: sticky;
        top: 0;
        z-index: 1;
    }
</style>

<div class="modal fade" id="extraXlsModal" tabindex="-1" aria-labelledby="extraXlsTitle" aria-hidden="true">
    <div class="modal-dialog | modal-fullscreen | modal-dialog-centered | modal-dialog-scrollable">
        <div class="modal-content">
            
            <div class="modal-header">
                <h2 class="modal-title | fs-5" id="extraXlsTitle">일괄 업로드</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            
            <div class="modal-body">
	            <jsp:include page="/WEB-INF/jsp/cmp/CmpFileParseBox.jsp">
	                <jsp:param name="id" value="extraExcelAttachment" />
	                <jsp:param name="uploadUrl" value="/extra/file/upload.do" />
	                <jsp:param name="name" value="fileIds" />
	                <jsp:param name="multiple" value="false" />
	                <jsp:param name="label" value="엑셀 파일 업로드" />
	                <jsp:param name="description" value="파일을 선택하면 자동으로 업로드됩니다." />
	                <jsp:param name="accept" value=".xlsx" />
	                <jsp:param name="maxSize" value="${fileMaxSize}" />
	            </jsp:include>

            </div>
                        
            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info | d-flex | justify-content-between | align-items-center | flex-wrap | gap-2">
                    <div class="d-flex | align-items-center | flex-wrap | gap-3">
                        <p class="table-info-text | mb-0">총 <span id="extraExcelTotalCount">0</span>건</p>
                        <p class="table-info-text | mb-0">정상 <span id="extraExcelValidCount" class="extra-excel-valid">0</span>건</p>
                        <p class="table-info-text | mb-0">오류 <span id="extraExcelErrorCount" class="extra-excel-error">0</span>건</p>
                        <p class="table-info-text | mb-0">경고 <span id="extraExcelWarningCount" class="extra-excel-warning">0</span>건</p>
                    </div>
                    <a href="<c:url value='/extra/file/err/download.do'/>" class="ms-auto | flex-shrink-0 | btn | btn-sm | btn-outline-secondary">
                        오류 목록 다운로드
                    </a>
                </div>

                <!-- 테이블 본문 -->
                <div class="table-responsive extra-excel-scroll" tabindex="0" role="region" aria-label="엑셀 데이터 스크롤 영역">
                    <table id="extraExcelTable" class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center | text-nowrap">
                            <tr>
                                <th scope="col">NO</th>
                                <th scope="col">자전거번호</th>
                                <th scope="col">대여일시</th>
                                <th scope="col">대여 대여소번호</th>
                                <th scope="col">대여 대여소명</th>
                                <th scope="col">대여거치대</th>
                                <th scope="col">반납일시</th>
                                <th scope="col">반납대여소번호</th>
                                <th scope="col">반납대여소명</th>
                                <th scope="col">반납거치대</th>
                                <th scope="col">이용시간(분)</th>
                                <th scope="col">이용거리(M)</th>
                                <th scope="col">생년</th>
                                <th scope="col">성별</th>
                                <th scope="col">이용자종류</th>
                                <th scope="col">대여대여소ID</th>
                                <th scope="col">반납대여소ID</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr><td class="text-center | text-body-secondary | py-4" colspan="17">조회된 데이터가 없습니다.</td></tr>
                        </tbody>
                    </table>
                </div>
                <!-- 테이블 페이징 -->
                <nav id="extraExcelPaging" aria-label="엑셀 파싱 결과 페이징" hidden>
                    <ul class="pagination | justify-content-center | flex-wrap"></ul>
                </nav>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
                <button type="button" class="btn btn-primary" disabled>등록</button>
            </div>
        </div>
    </div>
</div>
<script>
(function () {
    'use strict';
    var box = document.getElementById('extraExcelAttachment');
    var table = document.getElementById('extraExcelTable');
    var count = document.getElementById('extraExcelTotalCount');
    var paging = document.getElementById('extraExcelPaging');
    var rows = [];
    var columns = 16;
    var pageIndex = 1;
    var pageSize = 20;
    var fields = ['bikeId', 'rentDt', 'rentId', 'rentNm', 'rentHold', 'rtnDt', 'rtnId', 'rtnNm',
        'rtnHold', 'useMin', 'useDst', 'birthYear', 'sexCd', 'usrClsCd', 'rentStationId', 'returnStationId'];
    function cellText(item, field) {
        var value = item[field];
        if (value == null) return '';
        if (field === 'rentDt' || field === 'rtnDt') {
            var date = new Date(value);
            if (!isNaN(date.getTime())) {
                function pad(number) { return String(number).padStart(2, '0'); }
                return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
                    + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
            }
        }
        return String(value);
    }
    box.addEventListener('file-input-box:change', function (event) {
        var content = event.detail.fileContent;
        rows = [];
        pageIndex = 1;
        (content ? content.dataList : []).forEach(function (item, index) {
            rows.push({rowNumber: index + 1, cells: fields.map(function (field) { return cellText(item, field); }), issues: []});
        });
        (content && content.cellIssues || []).forEach(function (issue) {
            var row = rows[issue.rowIndex];
            if (row && issue.columnIndex >= 0 && issue.columnIndex < columns) {
                row.issues[issue.columnIndex] = issue;
                // 변환 실패로 VO에 null이 들어간 셀도 원본 입력값을 보여준다.
                row.cells[issue.columnIndex] = issue.value == null ? '' : issue.value;
            }
        });
        var total = content ? content.totalCount : 0;
        var errors = content ? content.errorCount : 0;
        var warnings = content ? content.warningCount : 0;
        count.textContent = total;
        document.getElementById('extraExcelValidCount').textContent = total - errors - warnings;
        document.getElementById('extraExcelErrorCount').textContent = errors;
        document.getElementById('extraExcelWarningCount').textContent = warnings;
        renderPage();
    });
    function renderPage() {
        var body = table.tBodies[0];
        body.textContent = '';
        var start = (pageIndex - 1) * pageSize;
        rows.slice(start, start + pageSize).forEach(function (row) {
                var tr = document.createElement('tr');
                var number = document.createElement('td');
                number.className = 'text-center';
                number.textContent = row.rowNumber;
                tr.appendChild(number);
                for (var index = 0; index < columns; index++) {
                    var cell = document.createElement('td');
                    cell.textContent = row.cells[index] == null ? '' : row.cells[index];
                    var issue = row.issues[index];
                    if (issue) {
                        var isError = issue.status === 'ERROR';
                        cell.className = isError ? 'extra-excel-error' : 'extra-excel-warning';
                        cell.title = issue.message;
                        cell.setAttribute('aria-label', (cell.textContent || '빈 값') + ' / '
                            + (isError ? '오류: ' : '경고: ') + issue.message);
                    }
                    tr.appendChild(cell);
                }
                body.appendChild(tr);
        });
        if (!rows.length) {
            var empty = body.insertRow().insertCell();
            empty.className = 'text-center | text-body-secondary | py-4';
            empty.colSpan = columns + 1;
            empty.textContent = '조회된 데이터가 없습니다.';
        }
        table.parentElement.scrollTop = 0;
        var totalPage = Math.ceil(rows.length / pageSize);
        var list = paging.querySelector('ul');
        list.textContent = '';
        paging.hidden = rows.length === 0 || pageSize <= 0;
        var pgFrom;
        var pgTo;
        if (totalPage <= 11) {
            pgFrom = 1; pgTo = totalPage;
        } else if (pageIndex <= 6) {
            pgFrom = 1; pgTo = 9;
        } else if (pageIndex >= totalPage - 5) {
            pgFrom = totalPage - 8; pgTo = totalPage;
        } else {
            pgFrom = pageIndex - 3; pgTo = pageIndex + 3;
        }
        function addPage(page, label, disabled) {
            var active = !label && page === pageIndex;
            var item = document.createElement('li');
            item.className = 'page-item' + (active ? ' active' : '') + (disabled ? ' disabled' : '');
            var link = document.createElement(active || disabled ? 'span' : 'a');
            link.className = 'page-link';
            if (label) {
                var symbol = document.createElement('span');
                symbol.setAttribute('aria-hidden', 'true');
                symbol.textContent = label;
                link.appendChild(symbol);
                if (label === '…') link.setAttribute('aria-hidden', 'true');
                else link.setAttribute('aria-label', (label === '«' ? '첫 페이지' : '마지막 페이지') + (disabled ? '' : '로 이동'));
            } else {
                link.textContent = page;
                link.setAttribute('aria-label', page + (active ? '페이지, 현재 페이지' : '페이지로 이동'));
            }
            if (disabled) link.setAttribute('aria-disabled', 'true');
            if (active) {
                link.setAttribute('aria-current', 'page');
                link.tabIndex = -1;
            }
            if (!active && !disabled) {
                link.href = '#extraExcelTable';
                link.addEventListener('click', function (event) {
                event.preventDefault();
                pageIndex = page;
                renderPage();
                var current = paging.querySelector('[aria-current="page"]');
                if (current) current.focus({preventScroll: true});
                table.parentElement.scrollTop = 0;
                });
            }
            item.appendChild(link);
            list.appendChild(item);
        }
        if (totalPage > 0) {
            addPage(1, '«', pageIndex <= 1);
            for (var page = 1; page <= totalPage; page++) {
                if (page === 1 || page === totalPage || (page >= pgFrom && page <= pgTo)) {
                    addPage(page);
                } else if ((page === 2 && pgFrom > 2) || (page === pgTo + 1 && pgTo + 1 < totalPage)) {
                    addPage(null, '…', true);
                }
            }
            addPage(totalPage, '»', pageIndex >= totalPage);
        }
    }
})();
</script>
