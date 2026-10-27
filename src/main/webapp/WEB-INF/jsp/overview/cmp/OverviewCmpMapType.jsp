<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<div id="map-type-control" class="map-type-control" role="group" aria-label="배경지도 유형">
    <button type="button" data-coreui-toggle="button" data-map-type="basic" aria-pressed="false">일반</button>
    <button type="button" data-coreui-toggle="button" data-map-type="satellite" aria-pressed="false">위성</button>
    <button type="button" data-coreui-toggle="button" data-map-type="terrain" aria-pressed="false">지형</button>
</div>

<style>
.map-type-control {
    display: flex;
    background: #fff;
    border-radius: 6px;
    overflow: hidden;
}
.map-type-control button {
    height: 36px;
    padding: 0 14px;
    border: 0;
    background: #fff;
    color: #4b5565;
    font-size: 13px;
    cursor: pointer;
}
.map-type-control button:last-child {
    border-right: 0;
}
.map-type-control button.active {
    background: var(--cui-primary);
    color: #fff;
}
</style>
