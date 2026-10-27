<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<div id="map-draw-tools" class="map-draw-tools" role="group" aria-label="지도 그리기 도구">
    <div class="map-toolbox" role="group" aria-label="그리기 모드">
        <button type="button" class="map-tool-button" data-draw-mode="LineString" aria-pressed="false" aria-label="거리 측정" aria-describedby="map-tool-distance-info">
            <i class="mdi | mdi-ruler" aria-hidden="true"></i><span class="map-tool-label">거리</span>
            <span id="map-tool-distance-info" class="map-tool-chip" role="tooltip">거리 측정 · 더블클릭으로 완료 · Esc 취소</span>
        </button>
        <button type="button" class="map-tool-button" data-draw-mode="Polygon" aria-pressed="false" aria-label="면적 측정" aria-describedby="map-tool-area-info">
            <i class="mdi | mdi-vector-square" aria-hidden="true"></i><span class="map-tool-label">면적</span>
            <span id="map-tool-area-info" class="map-tool-chip" role="tooltip">면적 측정 · 시작점 클릭 또는 더블클릭으로 완료</span>
        </button>
        <button type="button" class="map-tool-button" data-draw-mode="Circle" aria-pressed="false" aria-label="반경 측정" aria-describedby="map-tool-radius-info">
            <i class="mdi | mdi-radius-outline" aria-hidden="true"></i><span class="map-tool-label">반경</span>
            <span id="map-tool-radius-info" class="map-tool-chip" role="tooltip">반경 측정 · 중심점과 반경 끝점을 클릭</span>
        </button>
        <button type="button" class="map-tool-button" data-draw-mode="Modify" aria-pressed="false" aria-label="도형 수정" aria-describedby="map-tool-modify-info">
            <i class="mdi | mdi-pencil-outline" aria-hidden="true"></i><span class="map-tool-label">수정</span>
            <span id="map-tool-modify-info" class="map-tool-chip" role="tooltip">도형의 꼭짓점이나 가장자리를 드래그해 수정</span>
        </button>
        <button type="button" class="map-tool-button | map-tool-clear" data-draw-clear aria-label="측정 도형 전체 삭제" aria-describedby="map-tool-clear-info">
            <i class="mdi | mdi-trash-can-outline" aria-hidden="true"></i><span class="map-tool-label">삭제</span>
            <span id="map-tool-clear-info" class="map-tool-chip" role="tooltip">측정 도형 전체 삭제</span>
        </button>
    </div>
    <p id="map-draw-status" class="visually-hidden" role="status" aria-live="polite"></p>
</div>

<style>
.map-draw-tools .map-toolbox {
    display: flex;
    flex-direction: column;
    width: 44px;
    background: #fff;
    border: 1px solid #c8cdd3;
    border-radius: 5px;
    box-shadow: 0 2px 6px rgb(0 0 0 / 16%);
}
.map-draw-tools .map-tool-button {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    width: 100%;
    height: 46px;
    padding: 4px 0;
    border: 0;
    border-bottom: 1px solid #edf0f3;
    background: #fff;
    color: #454c53;
    cursor: pointer;
}
.map-draw-tools .map-tool-button:first-child { border-radius: 4px 4px 0 0; }
.map-draw-tools .map-tool-button:last-child { border-bottom: 0; border-radius: 0 0 4px 4px; }
.map-draw-tools .map-tool-button .mdi { font-size: 20px; line-height: 1; }
.map-draw-tools .map-tool-label { font-size: 10px; line-height: 1.2; }
.map-draw-tools .map-tool-button:hover { background: rgba(var(--cui-primary-rgb), .08); }
.map-draw-tools .map-tool-button[aria-pressed="true"] { background: var(--cui-primary); color: #fff; }
.map-draw-tools .map-tool-button:focus-visible { outline: 2px solid var(--cui-primary); outline-offset: 2px; z-index: 1; }
.map-draw-tools .map-tool-clear { color: #d34a43; }
.map-draw-tools .map-tool-chip {
    position: absolute;
    right: calc(100% + 8px);
    top: 50%;
    transform: translateY(-50%);
    width: max-content;
    max-width: min(300px, calc(100vw - 80px));
    padding: 7px 11px;
    border-radius: 16px;
    background: #343b43;
    color: #fff;
    font-size: 12px;
    font-weight: 400;
    line-height: 1.5;
    text-align: left;
    visibility: hidden;
    opacity: 0;
    pointer-events: none;
    box-shadow: 0 2px 6px rgb(0 0 0 / 15%);
}
.map-draw-tools .map-tool-button:hover .map-tool-chip,
.map-draw-tools .map-tool-button:focus-visible .map-tool-chip { visibility: visible; opacity: 1; }
</style>
