<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이벤트 목록</title>
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
            <c:set var="path" scope="request" value="홈/이벤트 목록"/>
            <c:set var="title" scope="request" value="이벤트 목록"/>
            <c:set var="dcs" scope="request" value="등록된 전체 이벤트를 확인합니다."/>
            <jsp:include page="/WEB-INF/jsp/main/cmp/MainCmpTitle.jsp" />

            <form id="searchForm" class="search-box | bg-body-tertiary" method="get" action="<c:url value='/event/list.do'/>">
                <input type="hidden" name="pageIndex" value="1" />
                <div class="d-flex | flex-wrap | align-items-center | gap-2">
                    <select 
                        class="form-select | input-box" 
                        placeholder="카테고리 선택"
                        id="ctg" name="ctg">
                        <option value="">전체</option>
                        <c:forEach var="eventCtg" items="${eventCtgs}">
                            <option value="${eventCtg.code}" ${eventCtg.code == filterVO.ctg ? 'selected' : ''}>
                                <c:out value="${eventCtg.name}"/>
                            </option>
                        </c:forEach>
                    </select>

                    <select 
                        class="form-select | input-box" 
                        placeholder="지역 선택"
                        id="region" name="region">
                        <option value="">전체</option>
                        <c:forEach var="reg" items="${regs}">
                            <option value="${reg.code}" ${reg.code == filterVO.reg ? 'selected' : ''}>
                                <c:out value="${reg.name}"/>
                            </option>
                        </c:forEach>
                    </select>

                    <input
                        class="form-control | input-box" 
                        type="text"
                        placeholder="제목 검색"
                        id="title" name="title"
                        value="${filterVO.title}"
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
	                    <a href="<c:url value='/event/create.do'/>" class="btn | btn-outline-secondary">신규 등록</a>
                    </div>
                </div>

                <!-- 테이블 본문 -->
                <div class="table-responsive">
                    <table class="table | table-hover | table-bordered | align-middle">
                        <thead class="table-light | text-center">
                            <tr>
                                <th scope="col" style="width: 45px;">NO</th>
                                <th scope="col" style="width: 80px;">지역</th>
                                <th scope="col" style="width: 200px;">제목</th>
                                <th scope="col" style="width: 80px;">분류</th>
                                <th scope="col">시작일</th>
                                <th scope="col">종료일</th>
                                <th scope="col">주소</th>
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
                                            <td><c:out value="${event.reg_id}"/></td>

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
                                    <a class="page-link"
                                        href="<c:url value='/event/list.do'>
                                                <c:param name='pageIndex' value='${i}'/>
                                                <c:param name='title' value='${filterVO.title}'/>
                                                <c:param name='ctg' value='${filterVO.ctg}'/>
                                              </c:url>">
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

<!-- SheetJS -->
<script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>
<script>
(function () {
    const btn = document.getElementById('btnExcel');
    const form = document.getElementById('searchForm');
    const listUrl = '<c:url value="/event/list.do"/>';

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
            ws['!cols'] = headers.map((h, i) => {
                const maxLen = Math.max(
                    h.length,
                    ...rows.map(r => String(r[i] ?? '').length)
                );
                return { wch: Math.min(Math.max(maxLen + 2, 8), 50) };
            });

            const wb = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(wb, ws, '이벤트 목록');
            XLSX.writeFile(wb, '이벤트목록_' + timestamp() + '.xlsx');
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
