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
        <jsp:param name="contentPage" value="/WEB-INF/jsp/overview/OverviewPannelMain.jsp"/>
    </jsp:include>

    <div class="map-weather-position">
        <jsp:include page="/WEB-INF/jsp/overview/cmp/OverviewCmpRtHud.jsp" />
    </div>
    

    <!-- 공통 배치 클래스에서 지도 위 컴포넌트 위치 지정 -->
    <div class="map-top-right">
	    <jsp:include page="/WEB-INF/jsp/overview/cmp/OverviewCmpMapType.jsp" />
	      
	    <div class="form-check form-switch mb-2">
	        <input class="form-check-input" type="checkbox" role="switch" id="map-weather-grid">
	        <label class="form-check-label" for="map-weather-grid">기상청 격자 (5km)</label>
	    </div>
	</div>
	    
    
    <div class="map-center-right">

        <jsp:include page="/WEB-INF/jsp/overview/cmp/OverviewCmpToolBox.jsp" />
    </div>

    <div class="map-bottom-right map-zoom-controls">

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
import { mapMeta, MAP_TYPE } from '<c:url value="/resources/js/MapMeta.js?v=20261027-grid2"/>';
import { createMapMarker } from '<c:url value="/resources/js/MapMarker.js"/>';
import { createMapDraw } from '<c:url value="/resources/js/MapDraw.js?v=20261027-toolbox1"/>';
import { createMapWeatherGrid } from '<c:url value="/resources/js/MapWeatherGrid.js?v=20261027-grid2"/>';

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
const mapTypeControl = document.getElementById('map-type-control');
const mapTypeButtons = Array.from(mapTypeControl.querySelectorAll('[data-map-type]'));
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
const weatherGridVisible = meta.weatherGrid?.visible === true;
const weatherGrid = createMapWeatherGrid(map, { visible: weatherGridVisible });
const weatherGridToggle = document.getElementById('map-weather-grid');
weatherGridToggle.checked = weatherGridVisible;
weatherGridToggle.addEventListener('change', () => {
  weatherGrid.setVisible(weatherGridToggle.checked);
  mapMeta.setWeatherGridVisible(weatherGridToggle.checked);
});

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

refreshMapTypeButtons();
mapTypeControl.addEventListener('click', handleMapTypeChange);

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

  const updatedMeta = mapMeta.updateView({
    cenLat: centerLonLat[1],
    cenLon: centerLonLat[0],
    zoom: view.getZoom(),
    minLat: bottomLeft[1],
    minLon: bottomLeft[0],
    maxLat: topRight[1],
    maxLon: topRight[0],
  });
  lastPublishedBounds = boundsKey;
  document.dispatchEvent(new Event('overview:bounds-changed'));

  const gridCenterElement = document.getElementById('map-weather-grid-center');
  const grid = updatedMeta.weatherGrid;
  if (gridCenterElement && grid) {
    gridCenterElement.textContent = grid.inRange
      ? '중심 격자: ' + grid.nx + ', ' + grid.ny
      : '기상청 격자 영역 밖';
  }
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
  const button = event.target.closest('[data-map-type]');
  if (!button || !mapTypeControl.contains(button)) return;
  const nextMapType = button.dataset.mapType;
  if (!Object.values(MAP_TYPE).includes(nextMapType) || nextMapType === currentMapType) return;
  const nextSource = meta.tileSources[nextMapType];

  if (!nextSource) return;

  baseTileLayer.setSource(createTileSource(nextSource));
  currentMapType = nextMapType;
  mapMeta.setMapType(currentMapType);
  refreshMapTypeButtons();
}

function refreshMapTypeButtons() {
  mapTypeButtons.forEach((button) => {
    const active = button.dataset.mapType === currentMapType;
    button.classList.toggle('active', active);
    button.setAttribute('aria-pressed', String(active));
  });
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
