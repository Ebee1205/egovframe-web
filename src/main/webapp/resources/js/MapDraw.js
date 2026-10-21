/** Drawing/measurement layer for an existing OpenLayers map (window.ol).
 * Results stay in memory. No server requests or persistence.
 * Returns {setMode, clear, destroy, source, layer}.
 */
export function createMapDraw(map, options = {}) {
    const ol = window.ol;
    const toolbar = options.toolbar;
    const status = options.status;
    const source = new ol.source.Vector({wrapX: false});
    const projection = map.getView().getProjection();
    const metricOptions = {projection};
    const viewport = map.getViewport();
    const originalCursor = viewport.style.getPropertyValue('cursor');
    const originalCursorPriority = viewport.style.getPropertyPriority('cursor');
    const cursors = options.cursors || {};
    let mode = 'None';
    let draw = null;
    let disposed = false;
    const buttons = toolbar ? Array.from(toolbar.querySelectorAll('[data-draw-mode]')) : [];
    const clearButton = toolbar && toolbar.querySelector('[data-draw-clear]');
    const segmentInput = toolbar && toolbar.querySelector('[data-draw-segments]');
    const hints = {
        None: '도구를 선택해 지도 위에 그려보세요.',
        LineString: '지점을 차례로 클릭하고 더블클릭으로 거리 측정을 마칩니다.',
        Polygon: '꼭짓점을 클릭하고 시작점 클릭 또는 더블클릭으로 면적 측정을 마칩니다.',
        Circle: '중심점을 클릭한 뒤 반경 끝점을 클릭하세요.',
        Modify: '도형의 꼭짓점이나 원의 가장자리를 드래그해 수정하세요.'
    };

    function announce(text) { if (status) status.textContent = text; }
    function lengthText(value) {
        return value >= 1000 ? (value / 1000).toFixed(2) + ' km' : value.toFixed(1) + ' m';
    }
    function areaText(value) {
        return value >= 1000000 ? (value / 1000000).toFixed(2) + ' km²' : value.toFixed(1) + ' m²';
    }
    const colors = {Polygon: '#4C50E8', LineString: '#F10000', Circle: '#FF4DEC'};
    function label(point, text, color, small = false) {
        return new ol.style.Style({
            geometry: point,
            text: new ol.style.Text({
                text, font: (small ? '12' : '14') + 'px sans-serif',
                fill: new ol.style.Fill({color}),
                backgroundFill: new ol.style.Fill({color: '#fff'}),
                backgroundStroke: new ol.style.Stroke({color, width: 1}),
                padding: [4, 6, 4, 6], offsetY: small ? 14 : -18
            })
        });
    }
    const shapeStyles = {};
    Object.keys(colors).forEach(function (type) {
        const color = colors[type];
        shapeStyles[type] = new ol.style.Style({
            fill: new ol.style.Fill({color: color + '26'}),
            stroke: new ol.style.Stroke({color, width: 3}),
            image: new ol.style.Circle({radius: 4,
                fill: new ol.style.Fill({color: '#fff'}),
                stroke: new ol.style.Stroke({color, width: 2})})
        });
    });
    function styleFeature(feature, sketch = false) {
        const geometry = feature.getGeometry();
        const type = geometry.getType();
        // Sketch points and polygon outlines follow the active tool color.
        const colorType = sketch && colors[mode] ? mode : type;
        const color = colors[colorType] || colors.LineString;
        const styles = [shapeStyles[colorType] || shapeStyles.LineString];
        let line;
        if (type === 'LineString') {
            line = geometry;
            styles.push(label(new ol.geom.Point(line.getLastCoordinate()),
                '거리 ' + lengthText(ol.sphere.getLength(line, metricOptions)), color));
        } else if (type === 'Polygon') {
            line = new ol.geom.LineString(geometry.getCoordinates()[0]);
            styles.push(label(geometry.getInteriorPoint(),
                '면적 ' + areaText(ol.sphere.getArea(geometry, metricOptions)), color));
        } else if (type === 'Circle') {
            const center = geometry.getCenter();
            const edge = [center[0] + geometry.getRadius(), center[1]];
            // Measure the ground distance, rather than Web Mercator map units.
            const radius = ol.sphere.getDistance(ol.proj.toLonLat(center, projection),
                ol.proj.toLonLat(edge, projection));
            const radiusLine = new ol.geom.LineString([center, edge]);
            styles.push(new ol.style.Style({geometry: radiusLine,
                stroke: new ol.style.Stroke({color, width: 1, lineDash: [5, 5]})}));
            styles.push(label(new ol.geom.Point(center), '반경 ' + lengthText(radius), color));
        }
        if (line && segmentInput && segmentInput.checked) {
            line.forEachSegment(function (a, b) {
                const segment = new ol.geom.LineString([a, b]);
                styles.push(label(new ol.geom.Point(segment.getCoordinateAt(0.5)),
                    lengthText(ol.sphere.getLength(segment, metricOptions)), color, true));
            });
        }
        return styles;
    }
    const layer = new ol.layer.Vector({source, style: function (feature) {
        return styleFeature(feature);
    }, zIndex: 20});
    const modify = new ol.interaction.Modify({source});
    modify.setActive(false);
    map.addLayer(layer);
    map.addInteraction(modify);

    function setMode(nextMode) {
        if (disposed || !Object.prototype.hasOwnProperty.call(hints, nextMode)) return;
        if (draw) {
            draw.abortDrawing();
            map.removeInteraction(draw);
            draw = null;
        }
        mode = nextMode;
        // Style the viewport itself so marker hover cursors on the map target
        // cannot replace the selected drawing cursor.
        if (mode === 'None') {
            if (originalCursor) viewport.style.setProperty('cursor', originalCursor, originalCursorPriority);
            else viewport.style.removeProperty('cursor');
        } else {
            const cursor = cursors[mode]
                ? 'url("' + cursors[mode] + '"), crosshair'
                : mode === 'Modify' ? 'move' : 'crosshair';
            viewport.style.setProperty('cursor', cursor, 'important');
        }
        modify.setActive(mode === 'Modify');
        if (['LineString', 'Polygon', 'Circle'].includes(mode)) {
            draw = new ol.interaction.Draw({source, type: mode,
                style: function (feature) { return styleFeature(feature, true); },
                stopClick: true, freehandCondition: function () { return false; }});
            draw.on('drawend', function (event) {
                event.feature.set('measurementType', mode);
                announce('측정 완료. 계속 그리거나 수정 도구를 선택하세요.');
            });
            draw.on('drawstart', function () { announce(hints[mode] + ' Esc: 취소'); });
            map.addInteraction(draw);
        }
        buttons.forEach(function (button) {
            button.setAttribute('aria-pressed', String(button.dataset.drawMode === mode));
        });
        announce(hints[mode]);
    }
    function clear() {
        if (disposed) return;
        if (draw) draw.abortDrawing();
        source.clear();
        announce('모든 측정 도형을 삭제했습니다. ' + hints[mode]);
    }
    function handleToolbar(event) {
        const button = event.target.closest('[data-draw-mode]');
        if (button && toolbar.contains(button)) setMode(button.dataset.drawMode);
    }
    function refreshSegments() {
        layer.changed();
        if (draw) draw.getOverlay().changed();
    }
    function handleKey(event) {
        if (event.key !== 'Escape' || mode === 'None' ||
            /^(INPUT|TEXTAREA|SELECT)$/.test(event.target.tagName) || event.target.isContentEditable) return;
        setMode('None');
        event.preventDefault();
    }
    if (toolbar) toolbar.addEventListener('click', handleToolbar);
    if (clearButton) clearButton.addEventListener('click', clear);
    if (segmentInput) segmentInput.addEventListener('change', refreshSegments);
    // Keep shortcuts within this map and its controls.
    const container = options.keyboardTarget || map.getTargetElement();
    container.addEventListener('keydown', handleKey);
    setMode('None');
    return {
        setMode, clear, source, layer,
        destroy: function () {
            if (disposed) return;
            setMode('None');
            disposed = true;
            map.removeInteraction(modify);
            map.removeLayer(layer);
            if (toolbar) toolbar.removeEventListener('click', handleToolbar);
            if (clearButton) clearButton.removeEventListener('click', clear);
            if (segmentInput) segmentInput.removeEventListener('change', refreshSegments);
            container.removeEventListener('keydown', handleKey);
        }
    };
}
