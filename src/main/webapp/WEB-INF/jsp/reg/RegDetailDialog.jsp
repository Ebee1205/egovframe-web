<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/ol@10.11.0/ol.css">
<script src="https://cdn.jsdelivr.net/npm/ol@10.11.0/dist/ol.js"></script>


<div class="modal fade" id="regDetailModal" tabindex="-1" aria-labelledby="regDetailTitle" aria-hidden="true"
     data-detail-url="<c:url value='/region/detail.do'/>"
     data-map-url="<c:url value='/region/detail/map.do'/>">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="regDetailTitle">행정구역 상세</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            <div class="modal-body">
                <p id="regDetailLoading" class="text-body-secondary d-none" role="status">행정구역 정보를 불러오는 중입니다.</p>
                <dl id="regDetailFields" class="row mb-0 d-none">
                    <dt class="col-4">행정구역 ID</dt><dd class="col-8" id="regDetailId"></dd>
                    <dt class="col-4">상위 행정구역</dt><dd class="col-8" id="regDetailParent"></dd>
                    <dt class="col-4">지역코드</dt><dd class="col-8" id="regDetailCode"></dd>
                    <dt class="col-4">행정구역명</dt><dd class="col-8" id="regDetailName"></dd>
                    <dt class="col-4">레벨</dt><dd class="col-8" id="regDetailLevel"></dd>
                    <dt class="col-4">상태</dt><dd class="col-8" id="regDetailStatus"></dd>
                    <dt class="col-4">등록일</dt><dd class="col-8" id="regDetailCDate"></dd>
                    <dt class="col-4">수정일</dt><dd class="col-8" id="regDetailUDate"></dd>
                </dl>
                <section class="mt-3 pt-3 border-top" aria-label="행정구역 지도">
                    <p id="regMapName" class="fw-semibold mb-2"></p>
                    <p id="regMapCoordinates" class="small text-body-secondary mb-2"></p>
                    <p id="regMapLoading" class="text-body-secondary d-none" role="status">행정구역 지도를 불러오는 중입니다.</p>
                    <p id="regMapMessage" class="text-body-secondary d-none" role="status"></p>
                    <div id="regMapError" class="text-danger d-none mb-2" role="alert" aria-live="polite"></div>
                    <div id="regMapCanvas" style="height: min(65vh, 640px); min-height: 320px; width: 100%;"></div>
                </section>

                <div id="regDetailError" class="text-danger d-none" role="alert" aria-live="polite"></div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<script>
(function () {
    'use strict';
    var modalElement = document.getElementById('regDetailModal');
    var modal = coreui.Modal.getOrCreateInstance(modalElement);
    var loading = document.getElementById('regDetailLoading');
    var fields = document.getElementById('regDetailFields');
    var error = document.getElementById('regDetailError');
    var requestVersion = 0;
    var activeRequest = null;

    function display(id, value) {
        document.getElementById(id).textContent = value == null || value === '' ? '-' : String(value);
    }
    function formatDate(value) {
        if (!value) { return '-'; }
        var date = new Date(value);
        return isNaN(date.getTime()) ? String(value) : date.toLocaleString('ko-KR');
    }
    function fillDetail(data) {
        display('regDetailId', data.reg_id);
        display('regDetailParent', data.parentRegId == null ? '최상위 행정구역'
            : (data.parentName || '-') + ' (' + (data.parentCode || data.parentRegId) + ')');
        display('regDetailCode', data.code);
        display('regDetailName', data.name);
        display('regDetailLevel', data.level);
        display('regDetailStatus', data.status === 'Y' ? '사용' : '미사용');
        display('regDetailCDate', formatDate(data.cDate));
        display('regDetailUDate', formatDate(data.uDate));
    }
    async function fetchRegDetail(regId) {
        var version = ++requestVersion;
        if (activeRequest) { activeRequest.abort(); }
        var controller = new AbortController();
        activeRequest = controller;
        var timer = setTimeout(function () { controller.abort(); }, 30000);
        loading.classList.remove('d-none');
        fields.classList.add('d-none');
        error.textContent = '';
        error.classList.add('d-none');
        modalElement.dispatchEvent(new CustomEvent('reg:show-map', {detail: {regId: regId}}));
        modal.show();
        try {
            var response = await fetch(modalElement.getAttribute('data-detail-url')
                + '?reg_id=' + encodeURIComponent(regId), {signal: controller.signal});
            var result;
            try { result = await response.json(); }
            catch (parseError) { throw new Error('서버 응답 오류 (HTTP ' + response.status + '): ' + response.url); }
            if (version !== requestVersion) { return; }
            if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS' || !result.data) {
                throw new Error(result.code && result.code.message || '행정구역 정보를 불러오지 못했습니다.');
            }
            fillDetail(result.data);
            fields.classList.remove('d-none');
        } catch (exception) {
            if (version !== requestVersion) { return; }
            error.textContent = exception.name === 'AbortError' ? '응답 시간이 초과되었습니다. 다시 시도해주세요.'
                : exception.message || '서버와 통신하지 못했습니다.';
            error.classList.remove('d-none');
        } finally {
            clearTimeout(timer);
            if (version === requestVersion) {
                loading.classList.add('d-none');
                activeRequest = null;
            }
        }
    }
    modalElement.addEventListener('hidden.coreui.modal', function () {
        ++requestVersion;
        if (activeRequest) { activeRequest.abort(); activeRequest = null; }
        fields.classList.add('d-none');
        error.classList.add('d-none');
        loading.classList.add('d-none');
    });
    var table = document.getElementById('regListTable');
    if (table) {
        table.addEventListener('click', function (event) {
            var button = event.target.closest('button[data-reg-action="detail"], button[data-reg-action="map"]');
            if (button && table.contains(button)) { fetchRegDetail(button.getAttribute('data-reg-id')); }
        });
    }
})();
</script>

<script>
    (function () {
        'use strict';

        var modalElement = document.getElementById('regDetailModal');
        var modal = coreui.Modal.getOrCreateInstance(modalElement);
        var loading = document.getElementById('regMapLoading');
        var message = document.getElementById('regMapMessage');
        var error = document.getElementById('regMapError');
        var nameElement = document.getElementById('regMapName');
        var coordinates = document.getElementById('regMapCoordinates');
        var requestVersion = 0;
        var activeRequest = null;
        var mapShown = false;
        var currentData;
        var map;
        var vectorSource;
        var vectorLayer;
        var view;

        function ensureMap() {
            if (map) {
                map.updateSize();
                return;
            }

            var baseLayer = new ol.layer.Tile({
                source: new ol.source.XYZ({
                    url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                    minZoom: 0,
                    maxZoom: 19,
                    attributions: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
                })
            });
            vectorSource = new ol.source.Vector();
            vectorLayer = new ol.layer.Vector({
                source: vectorSource,
                style: new ol.style.Style({
                    fill: new ol.style.Fill({color: 'rgba(13, 110, 253, 0.18)'}),
                    stroke: new ol.style.Stroke({color: '#0d6efd', width: 3}),
                    image: new ol.style.Circle({
                        radius: 7,
                        fill: new ol.style.Fill({color: '#0d6efd'}),
                        stroke: new ol.style.Stroke({color: '#ffffff', width: 2})
                    })
                })
            });

            view = new ol.View({
                center: ol.proj.fromLonLat([126.9780, 37.5665]),
                zoom: 12,
                minZoom: 0,
                maxZoom: 19,
                projection: 'EPSG:3857'
            });
            map = new ol.Map({
                target: 'regMapCanvas',
                layers: [baseLayer, vectorLayer],
                view: view,
                controls: [
                    new ol.control.Attribution({collapsible: true}),
                    new ol.control.ScaleLine()
                ]
            });
            map.updateSize();
        }

        function setCoordinates(lat, lon) {
            if (Number.isFinite(lat) && Number.isFinite(lon)) {
                coordinates.textContent = '위도: ' + lat.toFixed(6) + ' / 경도: ' + lon.toFixed(6);
            } else {
                coordinates.textContent = '위도·경도 정보가 없습니다.';
            }
        }

        function displayGeometry(data) {
            vectorSource.clear();
            var feature;

            if (data.geom) {
                var geometry = JSON.parse(data.geom);
                feature = new ol.format.GeoJSON().readFeature(
                    {type: 'Feature', properties: {}, geometry: geometry},
                    {dataProjection: 'EPSG:4326', featureProjection: view.getProjection()}
                );
            } else if (Number.isFinite(data.lat) && Number.isFinite(data.lon)) {
                feature = new ol.Feature({
                    geometry: new ol.geom.Point(
                        ol.proj.fromLonLat([data.lon, data.lat], view.getProjection())
                    )
                });
            }

            if (!feature) {
                message.textContent = '등록된 geometry가 없어 지도에 표시할 영역이 없습니다.';
                message.classList.remove('d-none');
                return;
            }

            message.classList.add('d-none');
            vectorSource.addFeature(feature);
            if (feature.getGeometry().getType() === 'Point') {
                view.setCenter(feature.getGeometry().getCoordinates());
                view.setZoom(14);
            } else {
                view.fit(feature.getGeometry().getExtent(), {
                    padding: [48, 48, 48, 48],
                    maxZoom: 14,
                    duration: 200
                });
            }
        }

        function displayRegMap(data) {
            ensureMap();
            nameElement.textContent = (data.name || '-') + ' (' + (data.code || '-') + ')';
            setCoordinates(data.lat, data.lon);
            displayGeometry(data);
        }

        async function fetchRegMap(regId) {
            var version = ++requestVersion;
            if (activeRequest) {
                activeRequest.abort();
            }

            var controller = new AbortController();
            activeRequest = controller;
            var timer = setTimeout(function () { controller.abort(); }, 30000);
            nameElement.textContent = '';
            coordinates.textContent = '';
            loading.classList.remove('d-none');
            message.classList.add('d-none');
            error.textContent = '';
            error.classList.add('d-none');
            currentData = null;
            if (vectorSource) {
                vectorSource.clear();
            }
            modal.show();

            try {
                var response = await fetch(modalElement.getAttribute('data-map-url')
                    + '?reg_id=' + encodeURIComponent(regId), {signal: controller.signal});
                var result;
                try { result = await response.json(); }
                catch (parseError) { throw new Error('서버 응답 오류 (HTTP ' + response.status + '): ' + response.url); }
                if (version !== requestVersion) {
                    return;
                }
                if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS' || !result.data) {
                    throw new Error(result.code && result.code.message || '행정구역 정보를 불러오지 못했습니다.');
                }

                currentData = result.data;
                if (mapShown) {
                    displayRegMap(currentData);
                }
            } catch (exception) {
                if (version !== requestVersion) {
                    return;
                }
                error.textContent = exception.name === 'AbortError'
                    ? '응답 시간이 초과되었습니다. 다시 시도해주세요.'
                    : exception.message || '서버와 통신하지 못했습니다.';
                error.classList.remove('d-none');
            } finally {
                clearTimeout(timer);
                if (version === requestVersion) {
                    loading.classList.add('d-none');
                    activeRequest = null;
                }
            }
        }

        modalElement.addEventListener('reg:show-map', function (event) {
            fetchRegMap(event.detail.regId);
        });
        modalElement.addEventListener('shown.coreui.modal', function () {
            mapShown = true;
            ensureMap();
            if (currentData) {
                displayRegMap(currentData);
            }
        });
        modalElement.addEventListener('hidden.coreui.modal', function () {
            mapShown = false;
            ++requestVersion;
            if (activeRequest) {
                activeRequest.abort();
                activeRequest = null;
            }
            loading.classList.add('d-none');
            message.classList.add('d-none');
            error.classList.add('d-none');
            currentData = null;
            if (vectorSource) {
                vectorSource.clear();
            }
        });

    })();
</script>
