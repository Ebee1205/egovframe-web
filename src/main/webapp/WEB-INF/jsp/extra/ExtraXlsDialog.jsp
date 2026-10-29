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
    #extraExcelTable td.extra-excel-warning-empty {
        --cui-table-bg: var(--cui-warning-bg-subtle);
        background-color: var(--cui-warning-bg-subtle);
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

<div class="modal fade" id="extraXlsModal" tabindex="-1" aria-labelledby="extraXlsTitle" aria-hidden="true"
     data-insert-url="<c:url value='/extra/file/insert.do'/>" data-list-url="<c:url value='/extra/list.do'/>">
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
				<label class="form-label | fw-semibold | upload-label">미리보기는 1000건만 제공됩니다.</label>
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
                <p id="extraExcelInsertStatus" class="me-auto mb-0" role="status" aria-live="polite"></p>

                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>

                <button id="extraExcelInsertButton" type="button" class="btn btn-primary" disabled>등록</button>
            </div>
        </div>
    </div>
</div>
<script>
(() => {
  // ----- 선언부 ----- //
  const box = document.getElementById('extraExcelAttachment');
  const table = document.getElementById('extraExcelTable');
  const totalCount = document.getElementById('extraExcelTotalCount');
  const validCount = document.getElementById('extraExcelValidCount');
  const errorCount = document.getElementById('extraExcelErrorCount');
  const warningCount = document.getElementById('extraExcelWarningCount');
  const paging = document.getElementById('extraExcelPaging');
  const modal = document.getElementById('extraXlsModal');
  const confirmDialog = document.getElementById('cmpYNDialog');
  const insertButton = document.getElementById('extraExcelInsertButton');
  const insertStatus = document.getElementById('extraExcelInsertStatus');
  const closeButtons = Array.from(modal.querySelectorAll('[data-coreui-dismiss="modal"]'));
  const modalInstance = coreui.Modal.getOrCreateInstance(modal);

  const pageSize = 20;
  const fields = ['bikeId', 'rentDt', 'rentId', 'rentNm', 'rentHold', 'rtnDt', 'rtnId', 'rtnNm',
    'rtnHold', 'useMin', 'useDst', 'birthYear', 'sexCd', 'usrClsCd', 'rentStationId', 'returnStationId'];
  const columns = fields.length;

  let isSaving = false;
  let isConfirmingClose = false;
  let allowModalClose = false;
  let rows = [];
  let pageIndex = 1;

  // ----- 이벤트 등록 및 초기화 ----- //
  box.addEventListener('file-input-box:change', handleFileChange);
  modal.addEventListener('hide.coreui.modal', handleHideModal);
  modal.addEventListener('hidden.coreui.modal', handleHiddenModal);
  confirmDialog.addEventListener('shown.coreui.modal', handleShownConfirmDialog);
  confirmDialog.addEventListener('hidden.coreui.modal', handleHiddenConfirmDialog);
  insertButton.addEventListener('click', handleInsertClick);
  paging.addEventListener('click', handlePageClick);
  document.addEventListener('focusin', handleModalFocus);

  // ----- 함수 정의 ----- //
  function cellText(item, field) {
    const value = item[field];
    if (value == null) return '';
    if (field === 'rentDt' || field === 'rtnDt') {
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        const pad = (number) => String(number).padStart(2, '0');
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
          + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
      }
    }
    return String(value);
  }

  function updateInsertButton(content) {
    const errors = content?.errorCount ?? 0;
    const eligible = (content?.totalCount ?? 0) - errors;
    insertButton.disabled = isSaving || eligible <= 0;
    insertButton.textContent = eligible > 0 ? (errors > 0 ? '오류 제외 ' : '') + eligible + '건 등록' : '등록';
  }

  function handleFileChange(event) {
    const content = event.detail.fileContent;
    rows = (content?.dataList ?? []).map((item, index) => ({
      rowNumber: index + 1,
      cells: fields.map((field) => cellText(item, field)),
      issues: [],
    }));
    
    pageIndex = 1;
    
    (content?.cellIssues ?? []).forEach((issue) => {
      const row = rows[issue.rowIndex];
      if (!row || issue.columnIndex < 0 || issue.columnIndex >= columns) return;
      row.issues[issue.columnIndex] = issue;
      // 변환 실패로 VO에 null이 들어간 셀도 원본 입력값을 보여준다.
      row.cells[issue.columnIndex] = issue.value ?? '';
    });

    const total = content?.totalCount ?? 0;
    const errors = content?.errorCount ?? 0;
    const warnings = content?.warningCount ?? 0;
    totalCount.textContent = total;
    validCount.textContent = total - errors - warnings;
    errorCount.textContent = errors;
    warningCount.textContent = warnings;
    updateInsertButton(content);
    insertStatus.textContent = errors > 0 ? '오류 ' + errors + '행을 제외하고 정상·경고 행을 등록합니다.' : '';
    insertStatus.classList.remove('text-danger');
    renderPage();
  }

  function handleHideModal(event) {
    if (event.target !== modal) return;
    if (isSaving || isConfirmingClose) {
      event.preventDefault();
      return;
    }
    if (allowModalClose) return;

    event.preventDefault();
    isConfirmingClose = true;
    CmpDialog.open({
      title: '닫기 확인',
      text: box.getAttribute('aria-busy') === 'true'
        ? '파일 처리 중입니다. 닫으면 결과 확인이 취소됩니다. 닫으시겠습니까?'
        : '닫으면 선택한 파일과 미리보기가 초기화됩니다. 닫으시겠습니까?',
      okText: '닫기',
      cancelText: '취소',
      okButton: confirmClose,
    });
  }

  function confirmClose() {
    allowModalClose = true;
    CmpDialog.close();
  }

  function handleShownConfirmDialog(event) {
    if (event.target === confirmDialog && isConfirmingClose) confirmDialog.focus();
  }

  function handleHiddenConfirmDialog(event) {
    if (event.target !== confirmDialog || !isConfirmingClose) return;
    isConfirmingClose = false;
    if (allowModalClose) {
      // 확인창의 닫힘이 끝난 뒤 업로드 창을 닫아 배경과 초기화 순서를 유지한다.
      modalInstance.hide();
      return;
    }
    document.body.classList.add('modal-open');
    modal.focus();
  }

  function handleHiddenModal(event) {
    if (event.target !== modal) return;
    allowModalClose = false;
    isConfirmingClose = false;
    // 파일·응답·미리보기 초기화는 CmpFileParseBox의 hidden 이벤트에서 수행한다.
  }

  function handleModalFocus(event) {
    // 공용 확인창을 닫은 뒤에도 키보드 포커스를 업로드 창 안에 유지한다.
    if (modal.classList.contains('show') && !isConfirmingClose
        && event.target !== document && !modal.contains(event.target)) {
      modal.focus();
    }
  }

  async function handleInsertClick() {
    if (isSaving || isConfirmingClose || box.getAttribute('aria-busy') === 'true') return;
    const content = box.getFileContent();
    const file = box.getFile();
    if (!file || !content || content.totalCount - content.errorCount <= 0) return;

    const body = new FormData();
    body.append('files', file);
    const csrf = document.querySelector('input[name="_csrf"]');
    if (csrf) body.append(csrf.name, csrf.value);

    isSaving = true;
    box.setProcessing(true);
    closeButtons.forEach((button) => { button.disabled = true; });
    insertButton.disabled = true;
    insertButton.textContent = '등록 중...';
    insertStatus.textContent = '파일을 확인하고 대여이력을 등록하고 있습니다.';
    insertStatus.classList.remove('text-danger');
    let isComplete = false;
    try {
      const response = await fetch(modal.getAttribute('data-insert-url'), {
        method: 'POST',
        credentials: 'same-origin',
        body,
      });
      const result = await response.json();
      if (!response.ok || result?.code?.resultCode !== 'SUCCESS') {
        throw new Error(result?.code?.message || '일괄 등록에 실패했습니다.');
      }
      isComplete = true;
      insertStatus.textContent = result.data.insertedCount + '건이 등록되었습니다. 목록을 새로 조회합니다.';
      window.location.assign(modal.getAttribute('data-list-url'));
    } catch (error) {
      insertStatus.textContent = error.message || '등록 결과를 확인하지 못했습니다. 목록을 확인해주세요.';
      insertStatus.classList.add('text-danger');
    } finally {
      if (!isComplete) {
        isSaving = false;
        box.setProcessing(false);
        closeButtons.forEach((button) => { button.disabled = false; });
        updateInsertButton(content);
      }
    }
  }

  function renderPage() {
    renderRows();
    renderPaging();
    table.parentElement.scrollTop = 0;
  }

  function renderRows() {
    const body = table.tBodies[0];
    body.textContent = '';
    const start = (pageIndex - 1) * pageSize;
    rows.slice(start, start + pageSize).forEach((row) => {
      const tr = document.createElement('tr');
      const number = document.createElement('td');
      number.className = 'text-center';
      number.textContent = row.rowNumber;
      tr.appendChild(number);
      for (let index = 0; index < columns; index++) {
        const cell = document.createElement('td');
        cell.textContent = row.cells[index] ?? '';
        const issue = row.issues[index];
        if (issue) {
          const isError = issue.status === 'ERROR';
          cell.className = isError ? 'extra-excel-error' : 'extra-excel-warning';
          if (!isError && cell.textContent.trim() === '') cell.classList.add('extra-excel-warning-empty');
          cell.title = issue.message;
          cell.setAttribute('aria-label', (cell.textContent || '빈 값') + ' / '
            + (isError ? '오류: ' : '경고: ') + issue.message);
        }
        tr.appendChild(cell);
      }
      body.appendChild(tr);
    });
    if (!rows.length) {
      const empty = body.insertRow().insertCell();
      empty.className = 'text-center | text-body-secondary | py-4';
      empty.colSpan = columns + 1;
      empty.textContent = '조회된 데이터가 없습니다.';
    }
  }

  function renderPaging() {
    const totalPage = Math.ceil(rows.length / pageSize);
    const list = paging.querySelector('ul');
    list.textContent = '';
    paging.hidden = totalPage === 0;
    if (!totalPage) return;

    let pgFrom;
    let pgTo;
    if (totalPage <= 11) {
      pgFrom = 1; pgTo = totalPage;
    } else if (pageIndex <= 6) {
      pgFrom = 1; pgTo = 9;
    } else if (pageIndex >= totalPage - 5) {
      pgFrom = totalPage - 8; pgTo = totalPage;
    } else {
      pgFrom = pageIndex - 3; pgTo = pageIndex + 3;
    }
    addPage(1, '«', pageIndex <= 1);
    for (let page = 1; page <= totalPage; page++) {
      if (page === 1 || page === totalPage || (page >= pgFrom && page <= pgTo)) {
        addPage(page);
      } else if ((page === 2 && pgFrom > 2) || (page === pgTo + 1 && pgTo + 1 < totalPage)) {
        addPage(null, '…', true);
      }
    }
    addPage(totalPage, '»', pageIndex >= totalPage);
  }

  function addPage(page, label, disabled = false) {
    const active = !label && page === pageIndex;
    const item = document.createElement('li');
    item.className = 'page-item' + (active ? ' active' : '') + (disabled ? ' disabled' : '');
    const link = document.createElement(active || disabled ? 'span' : 'a');
    link.className = 'page-link';
    if (label) {
      const symbol = document.createElement('span');
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
      link.dataset.pageIndex = page;
    }
    item.appendChild(link);
    paging.querySelector('ul').appendChild(item);
  }

  function handlePageClick(event) {
    const link = event.target.closest('a[data-page-index]');
    if (!link || !paging.contains(link)) return;
    event.preventDefault();
    pageIndex = Number(link.dataset.pageIndex);
    renderPage();
    paging.querySelector('[aria-current="page"]')?.focus({preventScroll: true});
  }
})();
</script>
