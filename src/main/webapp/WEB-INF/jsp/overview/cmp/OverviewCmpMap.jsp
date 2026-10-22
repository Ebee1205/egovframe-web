<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/ol@10.11.0/ol.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@7.4.47/css/materialdesignicons.min.css">
<script src="https://cdn.jsdelivr.net/npm/ol@10.11.0/dist/ol.js"></script>

<%-- 3D 컨테이너: 기존 #map 의 CSS(위치/크기)와 동일하게 맞춰야 합니다. --%>
<style>
    #map3d { position: absolute; inset: 0; z-index: 0; }
    #map3d[hidden] { display: none; }
    .map-top-right[hidden] { display: none; }
</style>

<div class="map-body | p-0">

    <div id="map" tabindex="0" aria-label="이벤트 지도"></div>
    <div id="map3d" hidden></div>

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
    import { createMap3D } from '<c:url value="/resources/js/MapTilt3D.js"/>';

    var meta = mapMeta.get();

    var currentMapType = Object.values(MAP_TYPE).includes(meta.mapType)
        ? meta.mapType
        : MAP_TYPE.BASIC;

    var tileSource = meta.tileSources[currentMapType];


    // =========================
    // DOM
    // =========================

    var zoomSlider = document.getElementById('zoom-slider');
    var zoomInButton = document.getElementById('zoom-in');
    var zoomOutButton = document.getElementById('zoom-out');

    var attributionElement = document.getElementById('map-attribution');
    var scaleElement = document.getElementById('map-scale');

    var mapTypeSelect = document.getElementById('map-type-select');

    var drawToolsEl = document.getElementById('map-draw-tools');
    var toolboxEl = drawToolsEl.closest('.map-top-right');


    // =========================
    // Tile Source
    // =========================

    function createTileSource(source) {
        return new ol.source.XYZ({
            url: source.url,
            minZoom: source.minZoom,
            maxZoom: source.maxZoom,
            attributions: source.attributions
        });
    }


    var baseTileLayer = new ol.layer.Tile({
        source: createTileSource(tileSource)
    });


    // =========================
    // Map
    // =========================

    var map = new ol.Map({
        target: 'map',

        layers: [
            baseTileLayer
        ],

        controls: [
            new ol.control.Rotate(),

            new ol.control.Attribution({
                target: attributionElement,
                collapsible: false
            }),

            new ol.control.ScaleLine({
                target: scaleElement
            })
        ],

        view: new ol.View({
            center: ol.proj.fromLonLat(
                meta.view.center,
                meta.projection
            ),

            zoom: meta.view.zoom,

            minZoom: meta.view.minZoom,
            maxZoom: meta.view.maxZoom,

            projection: meta.projection
        })
    });


    var view = map.getView();
    var mapDraw = createMapDraw(map, {
        toolbar: document.getElementById('map-draw-tools'),
        status: document.getElementById('map-draw-status'),
        cursors: {
            LineString: '<c:url value="/resources/distance.cur.ico"/>',
            Polygon: '<c:url value="/resources/area.cur.ico"/>',
            Circle: '<c:url value="/resources/point_radius.cur.ico"/>'
        },
        keyboardTarget: document.querySelector('.map-body')
    });


    // =========================
    // Event markers (2D / 3D 공용 로직)
    // =========================

    // Event-list UI bindings stay here; the marker component accepts any point data.
    var eventCards = Array.from(document.querySelectorAll('[data-overview-event]'));
    var sidePanel = document.getElementById('mapSidePanel');

    function toItems() {
        return eventCards.map(function (card) {
            return {
                id: card.dataset.overviewEvent,
                lat: card.dataset.lat,
                lon: card.dataset.lon,
                title: card.querySelector('.event-title').textContent.trim(),
                card: card
            };
        });
    }

    function getPadding(size) {
        var panelWidth = sidePanel && !sidePanel.classList.contains('folded') ? sidePanel.offsetWidth + 24 : 0;
        return [40, Math.min(64, size[0] * 0.1), Math.min(100, size[1] * 0.2),
            Math.min(panelWidth + 40, size[0] * 0.6)];
    }

    function handleSelect(item, selection) {
        eventCards.forEach(function (card) {
            var active = card === item.card;
            card.classList.toggle('is-map-selected', active);
            card.setAttribute('aria-pressed', String(active));
        });
        if (selection.fromMap) {
            if (sidePanel && sidePanel.classList.contains('folded')) sidePanel.querySelector('.fold-button').click();
            item.card.scrollIntoView({block: 'nearest', behavior: 'smooth'});
        }
    }

    var eventMarkers = createMapMarker(map, {
        color: '#5856d6',
        hoverColor: '#4645ab',
        opacity: 0.5,
        selectedOpacity: 1,
        hoverOpacity: 1,
        // Supply icon: '/path/to/marker.svg' or {src, width, anchor, ...} as needed.
        items: toItems(),
        getPadding: getPadding,
        onSelect: handleSelect
    });


    // =========================
    // 3D (MapLibre pitch) - 하이브리드
    // =========================

    var map3d = createMap3D(map, {
        container: document.getElementById('map3d'),
        projection: meta.projection,
        getTileSource: function () { return meta.tileSources[currentMapType]; },
        // 건물 3D 등 벡터 스타일을 쓰려면: style: 'https://.../style.json'
        color: '#5856d6',
        hoverColor: '#4645ab',
        opacity: 0.5,
        selectedOpacity: 1,
        hoverOpacity: 1,
        items: toItems(),
        getPadding: getPadding,
        onSelect: handleSelect,
        onZoom: function (z) {
            zoomSlider.value = z;
        },
        onEnter: function () {
            // 진행 중인 그리기 모드 해제 후 도구 숨김
            mapDraw.setMode('None');
            toolboxEl.hidden = true;
        },
        onExit: function () {
            toolboxEl.hidden = false;
            zoomSlider.value = view.getZoom();
            // Return to a north-up view smoothly after leaving 3D.
            console.log('[OverviewCmpMap] 2D 북쪽 정렬 시작 / 도구상자 표시', {
                time: new Date().toISOString(), rotation: view.getRotation()
            });
            view.animate({rotation: 0, duration: 500}, function (completed) {
                console.log('[OverviewCmpMap] 2D 북쪽 정렬 ' + (completed ? '완료' : '중단'), {
                    time: new Date().toISOString(), rotation: view.getRotation()
                });
            });
        }
    });

    // 2D / 3D 상태에 맞는 마커 컨트롤러
    function activeMarkers() {
        return map3d.isActive() ? map3d : eventMarkers;
    }

    var boundEventCards = new WeakSet();
    function bindEventCards() {
      eventCards.forEach(function (card) {
        card.classList.remove('is-map-selected');
        card.setAttribute('aria-pressed', 'false');
        var hasLocation = eventMarkers.has(card.dataset.overviewEvent);
        card.classList.toggle('is-map-interactive', hasLocation);
        card.setAttribute('aria-disabled', String(!hasLocation));
        card.tabIndex = hasLocation ? 0 : -1;
        card.title = hasLocation ? '지도에서 위치 보기' : '위치 정보 없음';
        card.setAttribute('aria-label', card.querySelector('.event-title').textContent.trim() + '. ' + card.title);
        if (boundEventCards.has(card)) return;
        boundEventCards.add(card);
        card.addEventListener('click', function () {
            if (hasLocation) activeMarkers().select(card.dataset.overviewEvent);
        });
        card.addEventListener('mouseenter', function () {
            if (hasLocation) activeMarkers().hover(card.dataset.overviewEvent);
        });
        card.addEventListener('mouseleave', function () { activeMarkers().hover(null); });
        card.addEventListener('keydown', function (event) {
            if (hasLocation && (event.key === 'Enter' || event.key === ' ')) {
                event.preventDefault();
                activeMarkers().select(card.dataset.overviewEvent);
            }
        });
      });
    }
    bindEventCards();
    document.addEventListener('overview:events-changed', function () {
        eventCards = Array.from(document.querySelectorAll('[data-overview-event]')).filter(function (card) {
            return !card.closest('[hidden]');
        });
        eventMarkers.setItems(toItems());
        map3d.setItems(toItems());
        bindEventCards();
    });

    // =========================
    // Zoom
    // =========================

    zoomSlider.min = view.getMinZoom();
    zoomSlider.max = view.getMaxZoom();
    zoomSlider.value = view.getZoom();


    function setZoom(value) {
        if (map3d.isActive()) {
            map3d.setZoom(Number(value));
        } else {
            view.setZoom(Number(value));
        }
    }


    function zoomBy(delta) {
        if (map3d.isActive()) {
            map3d.zoomBy(delta);
            return;
        }
        view.animate({
            zoom: view.getZoom() + delta,
            duration: 200
        });
    }


    zoomSlider.addEventListener('input', function () {
        setZoom(this.value);
    });


    zoomInButton.addEventListener('click', function () {
        zoomBy(1);
    });


    zoomOutButton.addEventListener('click', function () {
        zoomBy(-1);
    });


    /*
     * 휠 줌 / 더블클릭 줌 등으로
     * zoom이 변경됐을 때 slider도 같이 이동
     */
    view.on('change:resolution', function () {
        zoomSlider.value = view.getZoom();
    });


    // =========================
    // Map Type
    // =========================

    if (mapTypeSelect) {

        mapTypeSelect.value = currentMapType;

        mapTypeSelect.addEventListener('change', function () {

            var nextSource = meta.tileSources[this.value];

            if (!nextSource) {
                this.value = currentMapType;
                return;
            }

            baseTileLayer.setSource(
                createTileSource(nextSource)
            );

            currentMapType = this.value;

            mapMeta.setMapType(currentMapType);

            // 3D 뷰도 같은 타일로 갱신
            map3d.refreshSource();
        });
    }


    // =========================
    // View 저장
    // =========================

    map.on('moveend', function () {

        var center = view.getCenter();

        if (!center) {
            return;
        }

        mapMeta.updateView({
            center: ol.proj.toLonLat(
                center,
                meta.projection
            ),

            zoom: view.getZoom()
        });
    });


    // =========================
    // Map Size 갱신
    // =========================

    requestAnimationFrame(function () {
        map.updateSize();
    });

    // Keep the viewport in sync with header/sidebar and window size changes.
    if (typeof ResizeObserver !== 'undefined') {
        var mapResizeObserver = new ResizeObserver(function () {
            map.updateSize();
            var map3dInstance = map3d.getMap();
            if (map3dInstance) map3dInstance.resize();
        });
        mapResizeObserver.observe(document.getElementById('map'));
    }

</script>
