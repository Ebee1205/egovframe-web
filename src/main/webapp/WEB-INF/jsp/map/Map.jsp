<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/ol@10/ol.css">
<link href="<c:url value='/resources/css/map.css'/>" rel="stylesheet" type="text/css">

<div class="map-body | p-0">
    <div id="map"></div>
</div>

<div class="map | container-fluid | p-0">
    <div id="map"></div>
</div>

<script src="https://cdn.jsdelivr.net/npm/ol@10/dist/ol.js"></script>
<script>
    var map = new ol.Map({
        target: 'map',
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            })
        ],
        view: new ol.View({
            center: ol.proj.fromLonLat([126.9780, 37.5665]), // 서울시청 (경도, 위도)
            zoom: 12
        })
    });
</script>
