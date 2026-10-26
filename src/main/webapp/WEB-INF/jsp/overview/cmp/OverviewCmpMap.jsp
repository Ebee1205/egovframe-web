<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/ol@10.11.0/ol.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@7.4.47/css/materialdesignicons.min.css">
<script src="https://cdn.jsdelivr.net/npm/ol@10.11.0/dist/ol.js"></script>

<div class="map-body | p-0">

    <div id="map" tabindex="0" aria-label="이벤트 지도"></div>
	
    <jsp:include page="/WEB-INF/jsp/overview/cmp/OverviewCmpSideBar.jsp">
        <jsp:param name="contentPage"
                   value="/WEB-INF/jsp/overview/OverviewPannelMain.jsp"/>
    </jsp:include>

    <!-- 그리기 도구: 우측 상단 / 세로 정렬 -->
    <div class="map-top-right">
        <div id="map-draw-tools" class="map-draw-tools" role="group" aria-label="지도 그리기 도구">

            <div class="btn-group-vertical shadow-sm map-draw-buttons" role="group" aria-label="그리기 모드">
                <button type="button" class="btn btn-light" data-draw-mode="None" aria-pressed="true" aria-label="이동" title="이동"><i class="mdi | mdi-hand-back-right-outline" aria-hidden="true"></i></button>
                <button type="button" class="btn btn-light" data-draw-mode="LineString" aria-pressed="false" aria-label="거리" title="거리"><i class="mdi | mdi-ruler" aria-hidden="true"></i></button>
                <button type="button" class="btn btn-light" data-draw-mode="Polygon" aria-pressed="false" aria-label="면적" title="면적"><i class="mdi | mdi-vector-square" aria-hidden="true"></i></button>
                <button type="button" class="btn btn-light" data-draw-mode="Circle" aria-pressed="false" aria-label="반경" title="반경"><i class="mdi | mdi-radius-outline" aria-hidden="true"></i></button>
                <button type="button" class="btn btn-light" data-draw-mode="Modify" aria-pressed="false" aria-label="수정" title="수정"><i class="mdi | mdi-pencil-outline" aria-hidden="true"></i></button>
                <button type="button" class="btn btn-light text-danger" data-draw-clear aria-label="전체 삭제" title="전체 삭제"><i class="mdi | mdi-trash-can-outline" aria-hidden="true"></i></button>
            </div>

            <div class="card shadow-sm map-draw-options">
                <div class="card-body p-2">
                    <div class="form-check form-switch mb-0">
                        <input class="form-check-input" type="checkbox" role="switch" id="map-draw-segments" data-draw-segments checked>
                        <label class="form-check-label" for="map-draw-segments">구간 거리</label>
                    </div>
                    <p id="map-draw-status" class="mb-0 | mt-2 | text-body-secondary" role="status" aria-live="polite"></p>
                </div>
            </div>

        </div>
    </div>

    <div class="map-bottom-right">

        <!-- Zoom Control -->
        <div class="zoom-box">
            <button id="zoom-in" type="button">+</button>

            <input
                id="zoom-slider"
                class="zoom-slider"
                type="range"
                step="0.1"
            />

            <button id="zoom-out" type="button">−</button>
        </div>

        <!-- Attribution + Scale -->
        <div class="bottom-row">
            <div id="map-attribution" class="attribution-box"></div>
            <div id="map-scale" class="scale-box"></div>
        </div>

    </div>

</div>
<script type="module">
import { mapMeta, MAP_TYPE } from '<c:url value="/resources/js/MapMeta.js"/>';
import { createMapMarker } from '<c:url value="/resources/js/MapMarker.js"/>';
import { createMapDraw } from '<c:url value="/resources/js/MapDraw.js"/>';

// ----- 지도 설정 ----- //
const meta = mapMeta.get();
let currentMapType = Object.values(MAP_TYPE).includes(meta.mapType) ? meta.mapType : MAP_TYPE.BASIC;
const tileSource = meta.tileSources[currentMapType];

// ----- 화면 요소 ----- //
const zoomSlider = document.getElementById('zoom-slider');
const zoomInButton = document.getElementById('zoom-in');
const zoomOutButton = document.getElementById('zoom-out');
const attributionElement = document.getElementById('map-attribution');
const scaleElement = document.getElementById('map-scale');
const mapTypeSelect = document.getElementById('map-type-select');
const sidePanel = document.getElementById('mapSidePanel');
const boundEventCards = new WeakSet();

let lastPublishedBounds = '';

// ----- 배경지도 레이어 생성 ----- //
const baseTileLayer = new ol.layer.Tile({
  source: createTileSource(tileSource),
});

// ----- 지도 생성 및 배경 레이어 등록 ----- //
const map = new ol.Map({
  target: 'map',
  layers: [baseTileLayer],
  controls: [
    new ol.control.Rotate(),
    new ol.control.Attribution({
      target: attributionElement,
      collapsible: false,
    }),
    new ol.control.ScaleLine({
      target: scaleElement,
    }),
  ],
  view: new ol.View({
    center: ol.proj.fromLonLat([meta.view.cenLon, meta.view.cenLat], meta.projection),
    zoom: meta.view.zoom,
    minZoom: meta.view.minZoom,
    maxZoom: meta.view.maxZoom,
    projection: meta.projection,
  }),
});
const view = map.getView();

// ----- 측정 레이어 추가 ----- //
const mapDraw = createMapDraw(map, {
  toolbar: document.getElementById('map-draw-tools'),
  status: document.getElementById('map-draw-status'),
  cursors: {
    LineString: '<c:url value="/resources/distance.cur.ico"/>',
    Polygon: '<c:url value="/resources/area.cur.ico"/>',
    Circle: '<c:url value="/resources/point_radius.cur.ico"/>',
  },
  keyboardTarget: document.querySelector('.map-body'),
});

// ----- 이벤트 목록 상태 ----- //
let eventCards = Array.from(document.querySelectorAll('[data-overview-event]'));

// ----- 이벤트 마커 레이어 추가 ----- //
const eventMarkers = createMapMarker(map, {
  icon: {
    src: '<c:url value="/resources/place-mark.svg"/>',
    anchor: [0.5, 28 / 32],
  },
  selectedIcon: {
    src: '<c:url value="/resources/place-mark-spot.svg"/>',
    anchor: [0.5, 39 / 43],
  },
  selectedOpacity: 1,
  hoverOpacity: 1,
  hoverColor: '#b3b3b3',
  items: eventCards.map(createEventMarkerItem),
  onSelect: handleEventSelect,
});

// ----- 이벤트 연결 및 초기화 ----- //
bindEventCards();
document.addEventListener('overview:events-changed', refreshEventMarkers);
zoomSlider.min = view.getMinZoom();
zoomSlider.max = view.getMaxZoom();
zoomSlider.value = view.getZoom();
zoomSlider.addEventListener('input', (event) => setZoom(event.target.value));
zoomInButton.addEventListener('click', () => zoomBy(1));
zoomOutButton.addEventListener('click', () => zoomBy(-1));
view.on('change:resolution', () => {
  zoomSlider.value = view.getZoom();
});

if (mapTypeSelect) {
  mapTypeSelect.value = currentMapType;
  mapTypeSelect.addEventListener('change', handleMapTypeChange);
}

map.on('moveend', saveMapBounds);
requestAnimationFrame(refreshMapSize);
if (typeof ResizeObserver !== 'undefined') {
  const mapResizeObserver = new ResizeObserver(refreshMapSize);
  mapResizeObserver.observe(document.getElementById('map'));
}

// ----- 함수 정의 ----- //
function refreshMapSize() {
  map.updateSize();
  saveMapBounds();
}

function saveMapBounds() {
  const center = view.getCenter();
  const size = map.getSize();

  if (!center || !size || size[0] <= 0 || size[1] <= 0) {
    return;
  }

  const extent = view.calculateExtent(size);
  const bottomLeft = ol.proj.toLonLat([extent[0], extent[1]], view.getProjection());
  const topRight = ol.proj.toLonLat([extent[2], extent[3]], view.getProjection());
  const centerLonLat = ol.proj.toLonLat(center, view.getProjection());
  const boundsKey = [
    centerLonLat[0], centerLonLat[1], view.getZoom(),
    bottomLeft[0], bottomLeft[1], topRight[0], topRight[1],
  ].map((value) => Number(value).toFixed(7)).join(',');

  if (boundsKey === lastPublishedBounds) return;

  lastPublishedBounds = boundsKey;
  mapMeta.updateView({
    cenLat: centerLonLat[1],
    cenLon: centerLonLat[0],
    zoom: view.getZoom(),
    minLat: bottomLeft[1],
    minLon: bottomLeft[0],
    maxLat: topRight[1],
    maxLon: topRight[0],
  });
  document.dispatchEvent(new Event('overview:bounds-changed'));
}

function createEventMarkerItem(card) {
  return {
    id: card.dataset.overviewEvent,
    lat: card.dataset.lat,
    lon: card.dataset.lon,
    title: card.querySelector('.event-title').textContent.trim(),
    card,
  };
}

function refreshEventMarkers() {
  eventCards = Array.from(document.querySelectorAll('[data-overview-event]'))
    .filter((card) => !card.closest('[hidden]'));

  eventMarkers.setItems(eventCards.map(createEventMarkerItem));
  bindEventCards();
}

function handleEventSelect(item, selection) {
  eventCards.forEach((card) => {
    const isActive = card === item.card;

    card.classList.toggle('is-map-selected', isActive);
    card.setAttribute('aria-pressed', String(isActive));
  });

  if (!selection.fromMap) return;

  if (sidePanel && sidePanel.classList.contains('folded')) {
    sidePanel.querySelector('.fold-button').click();
  }

  item.card.scrollIntoView({
    block: 'nearest',
    behavior: 'smooth',
  });
}

function handleMapTypeChange(event) {
  const nextMapType = event.target.value;
  const nextSource = meta.tileSources[nextMapType];

  if (!nextSource) {
    event.target.value = currentMapType;
    return;
  }

  baseTileLayer.setSource(createTileSource(nextSource));
  currentMapType = nextMapType;
  mapMeta.setMapType(currentMapType);
}

function createTileSource(source) {
  return new ol.source.XYZ({
    url: source.url,
    minZoom: source.minZoom,
    maxZoom: source.maxZoom,
    attributions: source.attributions,
  });
}

function bindEventCards() {
  eventCards.forEach((card) => {
    const isActive = eventMarkers.isSelected(card.dataset.overviewEvent);
    const hasLocation = eventMarkers.has(card.dataset.overviewEvent);

    card.classList.toggle('is-map-selected', isActive);
    card.setAttribute('aria-pressed', String(isActive));
    card.classList.toggle('is-map-interactive', hasLocation);
    card.setAttribute('aria-disabled', String(!hasLocation));
    card.tabIndex = hasLocation ? 0 : -1;
    card.title = hasLocation ? '지도에서 위치 보기' : '위치 정보 없음';
    card.setAttribute('aria-label', card.querySelector('.event-title').textContent.trim() + '. ' + card.title);

    if (boundEventCards.has(card)) return;

    boundEventCards.add(card);
    card.addEventListener('click', () => {
      if (hasLocation) {
        eventMarkers.select(card.dataset.overviewEvent);
      }
    });
    card.addEventListener('mouseenter', () => {
      if (hasLocation) {
        eventMarkers.hover(card.dataset.overviewEvent);
      }
    });
    card.addEventListener('mouseleave', () => {
      eventMarkers.hover(null);
    });
    card.addEventListener('keydown', (event) => {
      if (hasLocation && (event.key === 'Enter' || event.key === ' ')) {
        event.preventDefault();
        eventMarkers.select(card.dataset.overviewEvent);
      }
    });
  });
}

function setZoom(value) {
  view.setZoom(Number(value));
}

function zoomBy(delta) {
  view.animate({
    zoom: view.getZoom() + delta,
    duration: 200,
  });
}
</script>
