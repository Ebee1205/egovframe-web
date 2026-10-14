<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>행정구역 목록</title>
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
            <c:set var="path" scope="request" value="홈/목록"/>
            <c:set var="title" scope="request" value="행정구역 목록"/>
            <c:set var="dcs" scope="request" value="등록된 전체 코드와 상위 행정구역을 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/region/list.do'/>">
                <input type="hidden" name="pageIndex" value="1" />
                <div class="d-flex | flex-wrap | align-items-center | gap-2">
                    <select 
                        class="form-select | input-box" 
                        placeholder="상위 행정구역 선택"
                        id="parentRegId" name="parentRegId" autocomplete="off">
                        <option value="">전체</option>
                        <c:forEach var="code" items="${rootRegs}">
                            <option value="${code.reg_id}" ${code.reg_id == filterVO.parentRegId ? 'selected' : ''}>
                                <c:out value="${code.code}"/> - <c:out value="${code.name}"/>
                            </option>
                        </c:forEach>
                    </select>

                    <input
                        class="form-control | input-box" 
                        type="text"
                        placeholder="행정구역명 검색"
                        id="name" name="name"
                        value="${filterVO.name}"
                        autocomplete="off"
                    ></input>
                </div>

                <!-- 우측: 검색 버튼 -->
                <button type="submit" class="btn | btn-primary">검색</button>
            </form>

            <div class="table-container">
                <!-- 테이블 상단 정보 -->
                <div class="table-info">
                    <p class="table-info-text">총 <c:out value="${filterVO.totalCnt}"/>건</p>
                    <div>
	                    <a id="btnExcel" class="btn | btn-outline-success">엑셀 다운로드</a>
	                    <a id="openRegMap" type="button" class="btn | btn-outline-secondary">지도 보기</a>
                    </div>
                </div>

                <!-- 테이블 본문 -->
                <div class="table-responsive">
                    <table id="regListTable" class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center">
                            <tr>
                                <th scope="col" style="width: 45px;">NO</th>
                                <th scope="col">상위 행정구역</th>
                                <th scope="col" style="width: 150px;">상위 행정구역명</th>
                                <th scope="col">코드</th>
                                <th scope="col" style="width: 150px;">행정구역명</th>
                                <th scope="col" style="width: 45px;">레벨</th>
                                <th scope="col" style="width: 45px;">상태</th>
                                <th scope="col" style="width: 160px;">등록일</th>
                                <th scope="col" style="width: 160px;">수정일</th>
                                <th scope="col" style="width: 108px;">action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty regs}">
                                    <tr><td class="text-center | text-body-secondary | py-4" colspan="10">조회된 데이터가 없습니다.</td></tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="code" items="${regs}" varStatus="rowStatus">
                                        <tr>
                                            <td class="text-center"><c:out value="${filterVO.firstIndex + rowStatus.count}"/></td>
                                            <td><c:out value="${code.parentCode}"/></td>
                                            <td><c:out value="${code.parentName}"/></td>
                                            <td><c:out value="${code.code}"/></td>
                                            <td><c:out value="${code.name}"/></td>
                                            <td><c:out value="${code.level}"/></td>
                                            <td><c:out value="${code.status}"/></td>
                                            <td><fmt:formatDate value="${code.CDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><fmt:formatDate value="${code.UDate}" pattern="yyyy-MM-dd HH:mm"/></td>

                                            <td class="text-nowrap">
                                                <button data-reg-action="detail" type="button" class="btn | btn-sm | btn-outline-primary" data-reg-id="${code.reg_id}" aria-label="<c:out value='${code.name}'/> 상세보기">상세보기</button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

				<!-- 페이지 표시 계산은 JSP에서 처리한다. 조회 건수와 크기는 filterVO를 사용한다. -->
				<c:set var="totalPage" value="0"/>
				<c:if test="${filterVO.pageSize > 0 and filterVO.totalCnt > 0}">
				    <fmt:parseNumber var="totalPage" integerOnly="true"
				        value="${(filterVO.totalCnt - 1) / filterVO.pageSize + 1}"/>
				</c:if>
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
				
				    <nav>
				        <ul class="pagination | justify-content-center | flex-wrap">
				
				            <%-- « 첫 페이지 --%>
				            <li class="page-item ${pgCur > 1 ? '' : 'disabled'}">
				                <c:choose>
				                    <c:when test="${pgCur > 1}">
				                        <c:url var="pgUrl" value="/region/list.do">
				                            <c:param name="pageIndex" value="1"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="name" value="${filterVO.name}"/>
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
				                                    <c:url var="pgUrl" value="/region/list.do">
				                                        <c:param name="pageIndex" value="${i}"/>
				                                        <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                                        <c:param name="name" value="${filterVO.name}"/>
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
				                        <c:url var="pgUrl" value="/region/list.do">
				                            <c:param name="pageIndex" value="${pgTotal}"/>
				                            <c:param name="pageSize" value="${filterVO.pageSize}"/>
				                            <c:param name="name" value="${filterVO.name}"/>
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
    <jsp:include page="/WEB-INF/jsp/cmp/CmpYNDialog.jsp" />
    <jsp:include page="/WEB-INF/jsp/reg/RegDetailDialog.jsp" />
</body>

<!-- SheetJS -->
<script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>
<script>
(function () {
    const btn = document.getElementById('btnExcel');
    const form = document.getElementById('searchForm');
    const listUrl = '<c:url value="/region/list.do"/>';

    function pad(n) { return String(n).padStart(2, '0'); }

    function timestamp() {
        const d = new Date();
        return d.getFullYear() + pad(d.getMonth() + 1) + pad(d.getDate())
             + '_' + pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds());
    }	    

    async function downloadExcel() {
        btn.disabled = true;
        const originText = btn.textContent;
        btn.textContent = '다운로드 중...';

        try {
            // 현재 검색 조건 + pageSize=0 (전체 조회)
            const params = new URLSearchParams(new FormData(form));
            params.set('pageIndex', '1');
            params.set('pageSize', '0');

            const res = await fetch(listUrl + '?' + params.toString(), {
                credentials: 'same-origin'
            });
            if (!res.ok) throw new Error('HTTP ' + res.status);

            // 응답 HTML 파싱
            const html = await res.text();
            const doc = new DOMParser().parseFromString(html, 'text/html');
            const table = doc.querySelector('.table-responsive table');
            if (!table) throw new Error('테이블을 찾을 수 없습니다.');

            // 헤더 ("action" 마지막 컬럼 제외)
            const headers = Array.from(table.querySelectorAll('thead th'))
                .slice(0, -1)
                .map(th => th.textContent.trim());

            // 본문 ("action" 마지막 컬럼 제외)
            const rows = [];
            table.querySelectorAll('tbody tr').forEach(tr => {
                const tds = tr.querySelectorAll('td');
                if (tds.length === 1 && tds[0].hasAttribute('colspan')) return; // 데이터 없음 행
                rows.push(Array.from(tds).slice(0, -1).map(td => td.textContent.trim()));
            });

            if (rows.length === 0) {
                alert('다운로드할 데이터가 없습니다.');
                return;
            }

            // NO 컬럼 재번호 (pageSize=0 이므로 1부터)
            rows.forEach((r, i) => { r[0] = i + 1; });

            // 시트 생성
            const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
            const merges = [];
            [1, 2].forEach(columnIndex => {
                let start = 0;
                while (start < rows.length) {
                    const value = rows[start][columnIndex];
                    let end = start;
                    while (
                        value !== '' &&
                        end + 1 < rows.length &&
                        rows[end + 1][columnIndex] === value
                    ) {
                        end++;
                    }
                    if (end > start) {
                        merges.push({
                            s: { r: start + 1, c: columnIndex },
                            e: { r: end + 1, c: columnIndex }
                        });
                    }
                    start = end + 1;
                }
            });
            if (merges.length > 0) ws['!merges'] = merges;

            ws['!cols'] = headers.map((h, i) => {
                const maxLen = Math.max(
                    h.length,
                    ...rows.map(r => String(r[i] ?? '').length)
                );
                return { wch: Math.min(Math.max(maxLen + 2, 8), 50) };
            });

            const wb = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(wb, ws, '행정구역 목록');
            XLSX.writeFile(wb, '행정구역목록_' + timestamp() + '.xlsx');
        } catch (e) {
            console.error(e);
            alert('엑셀 다운로드 중 오류가 발생했습니다.\n' + e.message);
        } finally {
            btn.disabled = false;
            btn.textContent = originText;
        }
    }

    btn.addEventListener('click', downloadExcel);
})();
</script>
</html>