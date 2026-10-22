<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div id="overviewEventPanel" class="col | overflow-auto | h-100">
    <form id="overviewEventSearchForm" class="p-2" role="search">
        <label for="overviewEventTitle" class="visually-hidden">이벤트 제목 검색</label>
        <input class="form-control" type="search" id="overviewEventTitle" name="title"
               placeholder="이벤트 제목 검색" autocomplete="off" enterkeyhint="search"
               aria-controls="overviewSearchEvents">
    </form>
    
    
    <div id="overviewRecentEvents">
        <jsp:include page="/WEB-INF/jsp/overview/event/OverviewEventList.jsp"/>
    </div>
    
    
    <div id="overviewSearchEvents" hidden></div>
    <p id="overviewSearchStatus" class="p-2 | text-body-secondary | small" role="status" aria-live="polite" hidden></p>
    <button id="overviewSearchRetry" type="button" class="btn btn-outline-secondary | m-2" hidden>다시 불러오기</button>
</div>

<script>
(function () {
    var panel = document.getElementById('overviewEventPanel');
    var form = document.getElementById('overviewEventSearchForm');
    var input = document.getElementById('overviewEventTitle');
    var recent = document.getElementById('overviewRecentEvents');
    var results = document.getElementById('overviewSearchEvents');
    var status = document.getElementById('overviewSearchStatus');
    var retry = document.getElementById('overviewSearchRetry');
    
    var title = '', page = 1, hasNext = false, loading = false, failed = false;
    var revision = 0, request = null, composing = false;

    function notifyListChange() {
        document.dispatchEvent(new Event('overview:events-changed'));
    }

    function loadNext() {
        if (!title || !hasNext || loading || composing) return;
        var currentRevision = revision;
        var requestedPage = page;
        var controller = new AbortController();
        request = controller;
        loading = true;
        failed = false;
        retry.hidden = true;
        results.setAttribute('aria-busy', 'true');
        status.hidden = false;
        status.textContent = '검색 결과를 불러오는 중입니다.';
        var query = new URLSearchParams({title: title, pageIndex: requestedPage});
        fetch('<c:url value="/overview/event/list.do"/>?' + query.toString(), {
            signal: controller.signal, credentials: 'same-origin', headers: {'Accept': 'text/html'}
        }).then(function (response) {
            if (!response.ok) throw new Error('검색 요청 실패');
            return response.text();
        }).then(function (html) {
            if (currentRevision !== revision) return;
            var parsed = new DOMParser().parseFromString(html, 'text/html');
            var section = parsed.querySelector('[data-search-page]');
            if (!section || Number(section.dataset.searchPage) !== requestedPage) throw new Error('검색 응답 오류');
            if (requestedPage === 1) {
                results.replaceChildren(section);
            } else {
                var items = results.querySelector('[data-search-items]');
                var cards = section.querySelectorAll('[data-overview-event]');
                if (!items && cards.length) throw new Error('검색 목록 오류');
                cards.forEach(function (card) { items.appendChild(card); });
            }
            hasNext = section.dataset.hasNext === 'true';
            page = requestedPage + 1;
            status.textContent = hasNext ? '아래로 스크롤하면 더 불러옵니다.' : '';
            notifyListChange();
        }).catch(function (error) {
            if (currentRevision !== revision || error.name === 'AbortError') return;
            failed = true;
            status.textContent = '검색 결과를 불러오지 못했습니다. 다시 시도해 주세요.';
            retry.hidden = false;
        }).finally(function () {
            if (currentRevision !== revision) return;
            loading = false;
            request = null;
            results.setAttribute('aria-busy', 'false');
            if (!failed) requestAnimationFrame(loadIfNearBottom);
        });
    }

    function loadIfNearBottom() {
        if (!failed && panel.scrollHeight - panel.scrollTop - panel.clientHeight < 200) loadNext();
    }

    function search(force) {
        var nextTitle = input.value.trim();
        if (nextTitle === title && !force) return;
        revision++;
        if (request) request.abort();
        request = null;
        title = nextTitle;
        page = 1;
        hasNext = !!title;
        loading = false;
        failed = false;
        results.replaceChildren();
        results.setAttribute('aria-busy', 'false');
        recent.hidden = !!title;
        results.hidden = !title;
        retry.hidden = true;
        status.hidden = !title;
        status.textContent = '';
        panel.scrollTop = 0;
        notifyListChange();
        loadNext();
    }

    // The native search-input clear icon emits input/search events with an empty value.
    function clearSearchIfEmpty() {
        if (!composing && !input.value.trim()) search();
    }
    input.addEventListener('input', clearSearchIfEmpty);
    input.addEventListener('search', clearSearchIfEmpty);
    input.addEventListener('compositionstart', function () { composing = true; });
    input.addEventListener('compositionend', function () { composing = false; clearSearchIfEmpty(); });
    input.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' && (composing || event.isComposing || event.keyCode === 229)) event.preventDefault();
    });
    form.addEventListener('submit', function (event) { event.preventDefault(); if (!composing) search(true); });
    retry.addEventListener('click', loadNext);
    panel.addEventListener('scroll', loadIfNearBottom, {passive: true});
    window.addEventListener('resize', loadIfNearBottom);
})();
</script>

