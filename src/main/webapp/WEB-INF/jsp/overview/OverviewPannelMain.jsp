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
    <button id="overviewSearchRetry" type="button" class="btn btn-outline-secondary | m-2" hidden>다시 불러오기</button>
</div>

<script>
(() => {
  // ----- 선언부 ----- //
  const panel = document.getElementById('overviewEventPanel');
  const searchForm = document.getElementById('overviewEventSearchForm');
  const searchInput = document.getElementById('overviewEventTitle');
  const recentEvents = document.getElementById('overviewRecentEvents');
  const searchEvents = document.getElementById('overviewSearchEvents');
  const retryButton = document.getElementById('overviewSearchRetry');
  
  const mapMetaKey = '__openlayers_tile_meta_data';
  
  const boundsKeys = ['minLat', 'minLon', 'maxLat', 'maxLon'];
  const boundsRefreshDelay = 150;

  let searchTitle = '';
  let requestRevision = 0;
  let requestController = null;
  let isComposing = false;
  let loadedBounds = '';
  let requestedBounds = '';
  let boundsTimer = null;

  // ----- 이벤트 등록 및 초기화 ----- //
  searchInput.addEventListener('input', clearSearchIfEmpty);
  searchInput.addEventListener('search', clearSearchIfEmpty);
  searchInput.addEventListener('compositionstart', handleCompositionStart);
  searchInput.addEventListener('compositionend', handleCompositionEnd);
  searchInput.addEventListener('keydown', handleSearchKeydown);
  searchForm.addEventListener('submit', handleSearchSubmit);
  retryButton.addEventListener('click', handleRetryClick);
  
  document.addEventListener('overview:bounds-changed', refreshBounds);

  // ----- 함수 정의 ----- //
  function notifyListChange() {
    document.dispatchEvent(new Event('overview:events-changed'));
  }

  function readBounds() {
    try {
      const savedMeta = JSON.parse(localStorage.getItem(mapMetaKey));
      const savedView = savedMeta?.view;

      if (!savedView || !boundsKeys.every((key) => isValidCoordinate(savedView[key]))) return null;

      const bounds = {};
      boundsKeys.forEach((key) => {
        bounds[key] = Number(savedView[key]);
      });

      if (bounds.minLat < -90 || bounds.maxLat > 90
          || bounds.minLon < -180 || bounds.maxLon > 180
          || bounds.minLat > bounds.maxLat || bounds.minLon > bounds.maxLon) {
        return null;
      }

      return new URLSearchParams(bounds).toString();
    } catch (error) {
      return null;
    }
  }

  function isValidCoordinate(value) {
    return value !== null && value !== undefined && value !== '' && Number.isFinite(Number(value));
  }

  function isCurrentBounds(bounds) {
    return bounds && (bounds === loadedBounds || (requestController && bounds === requestedBounds));
  }

  function cancelRequest() {
    if (requestController) requestController.abort();

    requestController = null;
    requestedBounds = '';
  }

  async function loadEvents(force = false) {
    const bounds = searchTitle ? '' : readBounds();

    if (!searchTitle && !force && isCurrentBounds(bounds)) return;

    loadedBounds = '';
    requestRevision++;
    const currentRevision = requestRevision;
    cancelRequest();

    const target = searchTitle ? searchEvents : recentEvents;

    // 응답이 준비될 때까지 기존 목록과 마커를 유지한다.
    target.setAttribute('aria-busy', 'false');
    retryButton.hidden = true;

    if (!searchTitle && !bounds) {
      return;
    }

    const controller = new AbortController();
    const url = searchTitle
      ? '<c:url value="/overview/event/list.do"/>?' + new URLSearchParams({ title: searchTitle }).toString()
      : '<c:url value="/overview/event/list/recent.do"/>?' + bounds;
    const selector = searchTitle ? '[data-search-events]' : '[data-viewport-events]';

    requestController = controller;
    requestedBounds = bounds;
    target.setAttribute('aria-busy', 'true');

    try {
      const response = await fetch(url, {
        signal: controller.signal,
        credentials: 'same-origin',
        headers: { Accept: 'text/html' },
      });

      if (!response.ok) throw new Error('조회 요청 실패');

      const html = await response.text();

      if (currentRevision !== requestRevision) return;

      const parsedHtml = new DOMParser().parseFromString(html, 'text/html');
      const section = parsedHtml.querySelector(selector);

      if (!section) throw new Error('조회 응답 오류');

      target.replaceChildren(section);
      if (!searchTitle) loadedBounds = bounds;

      notifyListChange();
    } catch (error) {
      if (currentRevision !== requestRevision || error.name === 'AbortError') return;

      retryButton.hidden = false;
    } finally {
      if (currentRevision === requestRevision) {
        requestController = null;
        target.setAttribute('aria-busy', 'false');
      }
    }
  }

  function search(force = false) {
    const nextTitle = searchInput.value.trim();

    if (nextTitle === searchTitle && !force) return;

    searchTitle = nextTitle;
    recentEvents.hidden = !!searchTitle;
    searchEvents.hidden = !searchTitle;
    notifyListChange();
    panel.scrollTop = 0;
    loadEvents(true);
  }

  function refreshBounds() {
    if (searchTitle) return;

    const bounds = readBounds();

    if (isCurrentBounds(bounds)) return;

    // 대기 시간 중 이전 응답이 도착해도 목록을 덮어쓰지 못하게 한다.
    if (requestController) {
      requestRevision++;
      cancelRequest();
    }

    clearTimeout(boundsTimer);
    boundsTimer = setTimeout(() => {
      if (!searchTitle) loadEvents();
    }, boundsRefreshDelay);
  }

  function clearSearchIfEmpty() {
    if (!isComposing && !searchInput.value.trim()) search();
  }

  function handleCompositionStart() {
    isComposing = true;
  }

  function handleCompositionEnd() {
    isComposing = false;
    clearSearchIfEmpty();
  }

  function handleSearchKeydown(event) {
    if (event.key === 'Enter' && (isComposing || event.isComposing || event.keyCode === 229)) {
      event.preventDefault();
    }
  }

  function handleSearchSubmit(event) {
    event.preventDefault();
    if (!isComposing) search(true);
  }

  function handleRetryClick() {
    loadEvents(true);
  }
})();
</script>
