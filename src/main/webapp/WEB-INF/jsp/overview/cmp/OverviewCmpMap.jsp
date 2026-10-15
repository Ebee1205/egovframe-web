<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/ol@10.11.0/ol.css">
<script src="https://cdn.jsdelivr.net/npm/ol@10.11.0/dist/ol.js"></script>
<link href="<c:url value='/resources/css/map.css?v=20261012-2'/>" rel="stylesheet" type="text/css">

<div class="map-body | p-0">

    <div id="map"></div>

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