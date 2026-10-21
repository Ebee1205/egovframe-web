<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/ol@10.11.0/ol.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@mdi/font@7.4.47/css/materialdesignicons.min.css">
<script src="https://cdn.jsdelivr.net/npm/ol@10.11.0/dist/ol.js"></script>
<link href="<c:url value='/resources/css/map.css?v=20261012-2'/>" rel="stylesheet" type="text/css">

<div class="map-body | p-0">

    <div id="map" tabindex="0" aria-label="이벤트 지도"></div>
    
    <jsp:include page="/WEB-INF/jsp/overview/cmp/OverviewCmpSideBar.jsp">
    <jsp:param name="contentPage"
               value="/WEB-INF/jsp/overview/OverviewPannelMain.jsp"/>
	</jsp:include>

    <div class="map-bottom-right">

        <div id="map-draw-tools" class="map-draw-tools" role="group" aria-label="지도 그리기 도구">
            <div class="map-draw-buttons">
                <button type="button" data-draw-mode="None" aria-pressed="true" aria-label="이동" title="이동"><i class="mdi mdi-hand-back-right-outline" aria-hidden="true"></i></button>
                <button type="button" data-draw-mode="LineString" aria-pressed="false" aria-label="거리" title="거리"><i class="mdi mdi-ruler" aria-hidden="true"></i></button>
                <button type="button" data-draw-mode="Polygon" aria-pressed="false" aria-label="면적" title="면적"><i class="mdi mdi-vector-square" aria-hidden="true"></i></button>
                <button type="button" data-draw-mode="Circle" aria-pressed="false" aria-label="반경" title="반경"><i class="mdi mdi-radius-outline" aria-hidden="true"></i></button>
                <button type="button" data-draw-mode="Modify" aria-pressed="false" aria-label="수정" title="수정"><i class="mdi mdi-pencil-outline" aria-hidden="true"></i></button>
                <button type="button" data-draw-clear aria-label="전체 삭제" title="전체 삭제"><i class="mdi mdi-trash-can-outline" aria-hidden="true"></i></button>
            </div>
            <label><input type="checkbox" data-draw-segments checked> 구간 거리 표시</label>
            <p id="map-draw-status" role="status" aria-live="polite"></p>
        </div>

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

<style>
    .map-draw-tools {
        width: 290px;
        max-width: 100%;
        padding: 10px;
        background: #fff;
        border: 1px solid #dcdfe4;
        border-radius: 6px;
        color: #4b5565;
        font-size: 12px;
    }
    .map-draw-buttons { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 8px; }
    .map-draw-buttons button {
        display: inline-flex; align-items: center; justify-content: center;
        width: 40px; height: 40px; padding: 0; font-size: 22px;
        border: 1px solid #dcdfe4;
        border-radius: 4px; background: #fff; color: #4b5565; cursor: pointer;
    }
    .map-draw-buttons button[aria-pressed="true"] { background: #254b86; color: #fff; }
    .map-draw-buttons button:focus-visible { outline: 2px solid #3170d7; outline-offset: 2px; }
    #map-draw-status { margin: 6px 0 0; line-height: 1.5; }
</style>

<script type="module">
	import { mapMeta, MAP_TYPE } from '<c:url value="/resources/js/MapMeta.js"/>';
    import { createMapMarker } from '<c:url value="/resources/js/MapMarker.js"/>';
    import { createMapDraw } from '<c:url value="/resources/js/MapDraw.js"/>';

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
    // Event-list UI bindings stay here; the marker component accepts any point data.
    var eventCards = Array.from(document.querySelectorAll('[data-overview-event]'));
    var sidePanel = document.getElementById('mapSidePanel');
    var eventMarkers = createMapMarker(map, {
        color: '#5856d6',
        hoverColor: '#4645ab',
        opacity: 0.5,
        selectedOpacity: 1,
        hoverOpacity: 1,
        // Supply icon: '/path/to/marker.svg' or {src, width, anchor, ...} as needed.
        items: eventCards.map(function (card) {
            return {
                id: card.dataset.overviewEvent,
                lat: card.dataset.lat,
                lon: card.dataset.lon,
                title: card.querySelector('.event-title').textContent.trim(),
                card: card
            };
        }),
        getPadding: function (size) {
            var panelWidth = sidePanel && !sidePanel.classList.contains('folded') ? sidePanel.offsetWidth + 24 : 0;
            return [40, Math.min(64, size[0] * 0.1), Math.min(100, size[1] * 0.2),
                Math.min(panelWidth + 40, size[0] * 0.6)];
        },
        onSelect: function (item, selection) {
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
    });
    eventCards.forEach(function (card) {
        var hasLocation = eventMarkers.has(card.dataset.overviewEvent);
        card.classList.toggle('is-map-interactive', hasLocation);
        card.setAttribute('aria-disabled', String(!hasLocation));
        card.tabIndex = hasLocation ? 0 : -1;
        card.title = hasLocation ? '지도에서 위치 보기' : '위치 정보 없음';
        card.setAttribute('aria-label', card.querySelector('.event-title').textContent.trim() + '. ' + card.title);
        card.addEventListener('click', function () {
            if (hasLocation) eventMarkers.select(card.dataset.overviewEvent);
        });
        card.addEventListener('mouseenter', function () {
            if (hasLocation) eventMarkers.hover(card.dataset.overviewEvent);
        });
        card.addEventListener('mouseleave', function () { eventMarkers.hover(null); });
        card.addEventListener('keydown', function (event) {
            if (hasLocation && (event.key === 'Enter' || event.key === ' ')) {
                event.preventDefault();
                eventMarkers.select(card.dataset.overviewEvent);
            }
        });
    });


    // =========================
    // Zoom
    // =========================

    zoomSlider.min = view.getMinZoom();
    zoomSlider.max = view.getMaxZoom();
    zoomSlider.value = view.getZoom();


    function setZoom(value) {
        view.setZoom(Number(value));
    }


    function zoomBy(delta) {
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
        });
        mapResizeObserver.observe(document.getElementById('map'));
    }

</script>
