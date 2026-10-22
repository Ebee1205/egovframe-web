/**
 * MapTilt3D.js
 * OpenLayers 2D 지도 위에 MapLibre GL 3D(pitch) 뷰를 얹는 하이브리드 모듈.
 *
 * 전제: 투영 EPSG:3857, 256px XYZ 래스터 타일 (OL zoom - 1 = MapLibre zoom)
 * 진입: Ctrl + 왼쪽 버튼 드래그 (또는 enter()/toggle())
 * 복귀: 지도를 다시 평평하게(pitch ≈ 0) 만들거나 exit()
 */

var ML_VERSION = '5.6.0'; // 고정 버전. 폐쇄망이면 자체 호스팅 경로로 교체
var ML_BASE = 'https://cdn.jsdelivr.net/npm/maplibre-gl@' + ML_VERSION + '/dist/';
var loadPromise = null;

function loadMapLibre() {
    if (window.maplibregl) return Promise.resolve(window.maplibregl);
    if (loadPromise) return loadPromise;
    loadPromise = new Promise(function (resolve, reject) {
        var css = document.createElement('link');
        css.rel = 'stylesheet';
        css.href = ML_BASE + 'maplibre-gl.css';
        document.head.appendChild(css);

        var s = document.createElement('script');
        s.src = ML_BASE + 'maplibre-gl.js';
        s.onload = function () { resolve(window.maplibregl); };
        s.onerror = function () {
            loadPromise = null;
            reject(new Error('MapLibre 로드 실패'));
        };
        document.head.appendChild(s);
    });
    return loadPromise;
}

export function createMap3D(olMap, options) {
    var o = Object.assign({
        pitch: 55,
        maxPitch: 75,
        flatThreshold: 1,        // 이 pitch 이하로 내려오면 2D 복귀
        gestureThreshold: 24,    // Ctrl + 드래그 이동 거리가 이 px 이상이면 진입
        color: '#5856d6',
        hoverColor: '#4645ab',
        opacity: 0.5,
        selectedOpacity: 1,
        hoverOpacity: 1,
        items: [],
        style: null,             // 벡터 스타일 URL/객체를 주면 건물 3D 등 가능
        getTileSource: null,     // () => {url,minZoom,maxZoom,attributions}
        getPadding: null,        // (size) => [top, right, bottom, left]
        projection: 'EPSG:3857',
        container: null,
        onEnter: null,
        onExit: null,
        onSelect: null,          // (item, {fromMap})
        onZoom: null             // (olEquivalentZoom)
    }, options);

    var ml = null;
    var map = null;
    var active = false;
    var busy = false;
    var items = o.items || [];
    var markers = new Map();
    var selectedId = null;
    var hoverId = null;

    function logTransition(stage, details) {
        console.log('[MapTilt3D] ' + stage, Object.assign({
            time: new Date().toISOString(),
            active: active,
            pitch: map ? map.getPitch() : 0,
            bearing: map ? map.getBearing() : 0
        }, details));
    }

    // ---------- 좌표/줌 변환 ----------
    function olToMl() {
        var v = olMap.getView();
        var c = ol.proj.toLonLat(v.getCenter(), o.projection);
        return {
            center: c,
            zoom: v.getZoom() - 1,
            bearing: -v.getRotation() * 180 / Math.PI
        };
    }

    function mlToOl() {
        var v = olMap.getView();
        var c = map.getCenter();
        v.setCenter(ol.proj.fromLonLat([c.lng, c.lat], o.projection));
        v.setZoom(map.getZoom() + 1);
        v.setRotation(-map.getBearing() * Math.PI / 180);
    }

    // ---------- 스타일 ----------
    function buildStyle() {
        if (o.style) return o.style;
        var src = o.getTileSource ? o.getTileSource() : null;
        if (!src) throw new Error('getTileSource 또는 style 옵션이 필요합니다.');
        var attr = Array.isArray(src.attributions) ? src.attributions.join(' ') : (src.attributions || '');
        return {
            version: 8,
            sources: {
                base: {
                    type: 'raster',
                    tiles: [src.url],
                    tileSize: 256,
                    minzoom: src.minZoom,
                    maxzoom: src.maxZoom,
                    attribution: attr
                }
            },
            layers: [{ id: 'base', type: 'raster', source: 'base' }]
        };
    }

    // ---------- 마커 ----------
    function styleMarker(entry, id) {
        var selected = id === selectedId;
        var hovered = id === hoverId;
        var el = entry.el;
        el.style.opacity = selected ? o.selectedOpacity : (hovered ? o.hoverOpacity : o.opacity);
        el.style.background = (selected || hovered) ? o.hoverColor : o.color;
        el.style.zIndex = selected ? '2' : '1';
        el.style.transform = selected ? 'scale(1.3)' : '';
    }

    function renderMarkers() {
        markers.forEach(function (e) { e.marker.remove(); });
        markers.clear();
        if (!map) return;

        items.forEach(function (item) {
            var lat = parseFloat(item.lat);
            var lon = parseFloat(item.lon);
            if (!isFinite(lat) || !isFinite(lon)) return;

            var el = document.createElement('button');
            el.type = 'button';
            el.title = item.title || '';
            el.setAttribute('aria-label', item.title || '이벤트');
            el.style.cssText = 'width:20px;height:20px;border-radius:50%;border:2px solid #fff;' +
                'padding:0;cursor:pointer;box-shadow:0 1px 4px rgba(0,0,0,.4);transition:transform .15s,opacity .15s;';
            el.addEventListener('click', function (e) {
                e.stopPropagation();
                select(item.id, true);
            });

            var marker = new ml.Marker({ element: el }).setLngLat([lon, lat]).addTo(map);
            var entry = { marker: marker, el: el, item: item, lon: lon, lat: lat };
            markers.set(String(item.id), entry);
            styleMarker(entry, String(item.id));
        });
    }

    function has(id) { return markers.has(String(id)); }

    function select(id, fromMap) {
        id = id == null ? null : String(id);
        var entry = id ? markers.get(id) : null;
        selectedId = entry ? id : null;
        markers.forEach(function (e, key) { styleMarker(e, key); });
        if (!entry) return;

        var size = [map.getContainer().clientWidth, map.getContainer().clientHeight];
        var p = o.getPadding ? o.getPadding(size) : [0, 0, 0, 0];
        map.easeTo({
            center: [entry.lon, entry.lat],
            padding: { top: p[0], right: p[1], bottom: p[2], left: p[3] },
            duration: 500
        });
        if (o.onSelect) o.onSelect(entry.item, { fromMap: !!fromMap });
    }

    function hover(id) {
        hoverId = id == null ? null : String(id);
        markers.forEach(function (e, key) { styleMarker(e, key); });
    }

    function setItems(next) {
        items = next || [];
        if (!markers.size && !active) return;
        renderMarkers();
    }

    // ---------- 진입 / 복귀 ----------
    function ensureMap() {
        if (map) return;
        var v = olToMl();
        var view = olMap.getView();
        map = new ml.Map({
            container: o.container,
            style: buildStyle(),
            center: v.center,
            zoom: v.zoom,
            bearing: v.bearing,
            pitch: 0,
            minZoom: view.getMinZoom() - 1,
            maxZoom: view.getMaxZoom() - 1,
            maxPitch: o.maxPitch,
            attributionControl: { compact: true },
            canvasContextAttributes: { antialias: true }
        });
        map.on('zoom', function () {
            if (active && o.onZoom) o.onZoom(map.getZoom() + 1);
        });
        map.on('pitchstart', function () {
            logTransition('틸트 시작');
        });
        map.on('pitchend', function () {
            logTransition('틸트 종료');
            if (active && map.getPitch() <= o.flatThreshold) {
                logTransition('평면 임계값 도달 → 2D 복귀', {flatThreshold: o.flatThreshold});
                exit();
            }
        });
    }

    function enter(pitch) {
        if (active || busy) return Promise.resolve();
        busy = true;
        logTransition('2D → 3D 전환 요청', {targetPitch: pitch || o.pitch});
        return loadMapLibre().then(function (lib) {
            ml = lib;
            ensureMap();

            var v = olToMl();
            o.container.hidden = false;
            map.resize();
            map.jumpTo({ center: v.center, zoom: v.zoom, bearing: v.bearing });
            map.easeTo({ pitch: pitch || o.pitch, duration: 600 });

            active = true;
            renderMarkers();
            if (selectedId) select(selectedId, false);
            if (o.onEnter) o.onEnter();
            logTransition('3D 활성화 / 도구상자 숨김');

            // 3D 타일이 그려지면 OL을 숨겨 GPU/이벤트 낭비를 줄임
            var done = false;
            var hideOl = function () {
                if (done || !active) return;
                done = true;
                olMap.getTargetElement().style.visibility = 'hidden';
                logTransition('2D 지도 숨김');
            };
            map.once('idle', hideOl);
            setTimeout(hideOl, 1500);
        }).catch(function (err) {
            console.error('[MapTilt3D] 3D 전환 실패', err);
        }).then(function () {
            busy = false;
        });
    }

    function exit() {
        if (!active) return;
        logTransition('3D → 2D 복귀 시작');
        active = false;
        mlToOl();
        map.jumpTo({ pitch: 0 });
        olMap.getTargetElement().style.visibility = '';
        olMap.updateSize();
        o.container.hidden = true;
        markers.forEach(function (e) { e.marker.remove(); });
        markers.clear();
        logTransition('2D 지도 복원 완료');
        if (o.onExit) o.onExit();
    }

    function toggle() { return active ? (exit(), Promise.resolve()) : enter(); }

    // ---------- Ctrl + 왼쪽 버튼 드래그 → 3D 진입 ----------
    var vp = olMap.getViewport();
    var start = null;

    vp.addEventListener('pointerdown', function (e) {
        if (active || busy || e.button !== 0 || !e.ctrlKey) return;
        start = { x: e.clientX, y: e.clientY, pointerId: e.pointerId };
        logTransition('Ctrl + 왼쪽 버튼 누름 / 드래그 대기');
        // Reserve this gesture before OpenLayers can pan or add a draw vertex.
        e.preventDefault();
        e.stopImmediatePropagation();
    }, true);
    vp.addEventListener('pointermove', function (e) {
        if (!start || e.pointerId !== start.pointerId) return;
        if (!e.ctrlKey || !(e.buttons & 1)) {
            start = null;
            return;
        }
        e.preventDefault();
        e.stopImmediatePropagation();
        if (Math.hypot(e.clientX - start.x, e.clientY - start.y) >= o.gestureThreshold) {
            logTransition('드래그 임계값 도달 → 3D 전환', {
                distance: Math.hypot(e.clientX - start.x, e.clientY - start.y),
                gestureThreshold: o.gestureThreshold
            });
            start = null;
            enter();
        }
    }, true);
    function cancelGesture() { start = null; }
    window.addEventListener('pointerup', cancelGesture);
    window.addEventListener('pointercancel', cancelGesture);
    window.addEventListener('blur', cancelGesture);
    window.addEventListener('keyup', function (e) {
        if (e.key === 'Control') cancelGesture();
    });

    // ---------- 줌 제어 (슬라이더/버튼 연동) ----------
    function setZoom(olZoom) { if (map) map.setZoom(olZoom - 1); }
    function zoomBy(delta) { if (map) map.easeTo({ zoom: map.getZoom() + delta, duration: 200 }); }

    function refreshSource() {
        if (map && !o.style) map.setStyle(buildStyle());
    }

    return {
        enter: enter,
        exit: exit,
        toggle: toggle,
        isActive: function () { return active; },
        has: has,
        select: function (id) { select(id, false); },
        hover: hover,
        setItems: setItems,
        setZoom: setZoom,
        zoomBy: zoomBy,
        refreshSource: refreshSource,
        getMap: function () { return map; }
    };
}
