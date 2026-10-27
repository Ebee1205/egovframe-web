/**
 * 지도 측정 도구. 거리·면적·반경 도형과 수정 기능을 관리한다.
 * 측정 결과는 브라우저 메모리에서만 유지한다.
 */
export function createMapDraw(map, options = {}) {

  // ----- 선언부----- //
  const ol = window.ol;
  const primaryColor = window.getComputedStyle(map.getTargetElement())
    .getPropertyValue('--cui-primary').trim() || '#5856d6';
  const toolbar = options.toolbar;
  const status = options.status;
  const projection = map.getView().getProjection();
  const metricOptions = {
    projection
  };
  const viewport = map.getViewport();
  const originalCursor = viewport.style.getPropertyValue('cursor');
  const originalCursorPriority = viewport.style.getPropertyPriority('cursor');
  const cursors = options.cursors || {};
  const shapeStyles = {};
  let mode = 'None';
  let draw = null;
  let disposed = false;
  const buttons = toolbar ? Array.from(toolbar.querySelectorAll('[data-draw-mode]')) : [];
  const clearButton = toolbar && toolbar.querySelector('[data-draw-clear]');
  const tools = {
    None: {
      cursor: 'grab',
      msg: '지도를 드래그해 이동하세요.'
    },
    LineString: {
      color: primaryColor,
      label: '거리',
      msg: '지점을 차례로 클릭하고 더블클릭으로 거리 측정을 마칩니다.'
    },
    Polygon: {
      color: primaryColor,
      label: '면적',
      msg: '꼭짓점을 클릭하고 시작점 클릭 또는 더블클릭으로 면적 측정을 마칩니다.'
    },
    Circle: {
      color: primaryColor,
      label: '반경',
      msg: '중심점을 클릭한 뒤 반경 끝점을 클릭하세요.'
    },
    Modify: {
      msg: '도형의 꼭짓점이나 원의 가장자리를 드래그해 수정하세요.'
    }
  };

  // ----- 도형 스타일 초기화 ----- //
  initializeStyles();

  // ----- 측정 레이어 생성 및 지도 등록 ----- //
  const source = new ol.source.Vector({
    wrapX: false
  });
  const layer = new ol.layer.Vector({
    source,
    style: function (feature) {
      return styleFeature(feature);
    },
    zIndex: 20
  });
  const modify = new ol.interaction.Modify({
    source
  });
  modify.setActive(false);
  map.addLayer(layer);
  map.addInteraction(modify);

  // ----- 이벤트 등록 및 초기화 ----- //
  map.on('pointerdrag', handleDrag);
  window.addEventListener('pointerup', handleDragEnd);
  window.addEventListener('pointercancel', handleDragEnd);
  window.addEventListener('blur', handleDragEnd);

  if (toolbar) {
    toolbar.addEventListener('click', handleToolbar);
  }
  if (clearButton) {
    clearButton.addEventListener('click', clear);
  }

  const container = options.keyboardTarget || map.getTargetElement();
  container.addEventListener('keydown', handleKey);
  setMode('None');

  return {
    setMode,
    toggleMode,
    clear,
    source,
    layer,
    destroy
  };

  // ----- 함수 정의 ----- //

  function announce(text) {
    if (status) {
      status.textContent = text;
    }
  }

  function lengthText(value) {
    return value >= 1000 ? (value / 1000).toFixed(2) + ' km' : value.toFixed(1) + ' m';
  }

  function areaText(value) {
    return value >= 1000000 ? (value / 1000000).toFixed(2) + ' km²' : value.toFixed(1) + ' m²';
  }

  function label(point, text, color, small = false) {
    return new ol.style.Style({
      geometry: point,
      text: new ol.style.Text({
        text,
        font: (small ? '12' : '14') + 'px sans-serif',
        fill: new ol.style.Fill({
          color: small ? '#fff' : color
        }),
        backgroundFill: new ol.style.Fill({
          color: small ? color : '#fff'
        }),
        backgroundStroke: new ol.style.Stroke({
          color,
          width: 1
        }),
        padding: [4, 6, 4, 6],
        offsetY: small ? 14 : -18
      })
    });
  }

  function styleFeature(feature, sketch = false) {
    const geometry = feature.getGeometry();
    const type = geometry.getType();
    const colorType = sketch && tools[mode].color ? mode : type;
    const tool = tools[colorType] || tools.LineString;
    const color = tool.color;
    const styles = [shapeStyles[colorType] || shapeStyles.LineString];
    let line;

    switch (type) {
      case 'LineString':
        line = geometry;
        styles.push(label(new ol.geom.Point(line.getLastCoordinate()), tools.LineString.label + ' ' + lengthText(ol.sphere.getLength(line, metricOptions)), color));
        break;

      case 'Polygon':
        line = new ol.geom.LineString(geometry.getCoordinates()[0]);
        styles.push(label(geometry.getInteriorPoint(), tools.Polygon.label + ' ' + areaText(ol.sphere.getArea(geometry, metricOptions)), color));
        break;

      case 'Circle':
        {
          const center = geometry.getCenter();
          const edge = [center[0] + geometry.getRadius(), center[1]];
          const radius = ol.sphere.getDistance(ol.proj.toLonLat(center, projection), ol.proj.toLonLat(edge, projection));
          const radiusLine = new ol.geom.LineString([center, edge]);
          styles.push(new ol.style.Style({
            geometry: radiusLine,
            stroke: new ol.style.Stroke({
              color,
              width: 1,
              lineDash: [5, 5]
            })
          }));
          styles.push(label(new ol.geom.Point(center), tools.Circle.label + ' ' + lengthText(radius), color));
          break;
        }
      default:
        break;
    }

    if (line) {
      line.forEachSegment(function (a, b) {
        const segment = new ol.geom.LineString([a, b]);
        styles.push(label(new ol.geom.Point(segment.getCoordinateAt(0.5)), lengthText(ol.sphere.getLength(segment, metricOptions)), color, true));
      });
    }
    
    return styles;
  }

  function setMode(nextMode) {
    if (disposed || !Object.prototype.hasOwnProperty.call(tools, nextMode)) {
      return;
    }
    if (draw) {
      draw.abortDrawing();
      map.removeInteraction(draw);
      draw = null;
    }
    mode = nextMode;
    if (mode === 'None') {
      viewport.style.setProperty('cursor', tools.None.cursor, 'important');
    } else {
      const cursor = cursors[mode] ? 'url("' + cursors[mode] + '"), crosshair' : mode === 'Modify' ? 'move' : 'crosshair';
      viewport.style.setProperty('cursor', cursor, 'important');
    }
    modify.setActive(mode === 'Modify');
    if (tools[mode].color) {
      draw = new ol.interaction.Draw({
        source,
        type: mode,
        style: function (feature) {
          return styleFeature(feature, true);
        },
        stopClick: true,
        freehandCondition: function () {
          return false;
        }
      });
      draw.on('drawend', function (event) {
        event.feature.set('measurementType', mode);
        announce('측정 완료. 계속 그리거나 수정 도구를 선택하세요.');
      });
      draw.on('drawstart', function () {
        announce(tools[mode].msg + ' Esc: 취소');
      });
      map.addInteraction(draw);
    }
    buttons.forEach(function (button) {
      const active = button.dataset.drawMode === mode;
      button.classList.toggle('active', active);
      button.setAttribute('aria-pressed', String(active));
    });
    announce(tools[mode].msg);
  }

  function toggleMode(nextMode) {
    if (!Object.prototype.hasOwnProperty.call(tools, nextMode)) {
      return;
    }
    setMode(nextMode === mode ? 'None' : nextMode);
  }

  function clear() {
    if (disposed) {
      return;
    }
    if (draw) {
      draw.abortDrawing();
    }
    source.clear();
    announce('모든 측정 도형을 삭제했습니다. ' + tools[mode].msg);
  }

  function handleToolbar(event) {
    const button = event.target.closest('[data-draw-mode]');
    if (!button || !toolbar.contains(button)) {
      return;
    }
    toggleMode(button.dataset.drawMode);
  }

  function handleKey(event) {
    if (event.key !== 'Escape' || mode === 'None' || /^(INPUT|TEXTAREA|SELECT)$/.test(event.target.tagName) || event.target.isContentEditable) {
      return;
    }
    setMode('None');
    event.preventDefault();
  }

  function handleDrag() {
    if (mode === 'None') {
      viewport.style.setProperty('cursor', 'grabbing', 'important');
    }
  }

  function handleDragEnd() {
    if (mode === 'None') {
      viewport.style.setProperty('cursor', tools.None.cursor, 'important');
    }
  }

  function initializeStyles() {
    Object.keys(tools).filter(function (type) {
      return tools[type].color;
    }).forEach(function (type) {
      const color = tools[type].color;
      shapeStyles[type] = new ol.style.Style({
        fill: new ol.style.Fill({
          color: [...ol.color.asArray(color).slice(0, 3), 0.15]
        }),
        stroke: new ol.style.Stroke({
          color,
          width: 3
        }),
        image: new ol.style.Circle({
          radius: 4,
          fill: new ol.style.Fill({
            color: '#fff'
          }),
          stroke: new ol.style.Stroke({
            color,
            width: 2
          })
        })
      });
    });
  }



  // ----- ㅣlifecycle - destroy ----- //

  function destroy() {
    if (disposed) {
      return;
    }
    setMode('None');
    disposed = true;
    map.un('pointerdrag', handleDrag);
    window.removeEventListener('pointerup', handleDragEnd);
    window.removeEventListener('pointercancel', handleDragEnd);
    window.removeEventListener('blur', handleDragEnd);
    if (originalCursor) {
      viewport.style.setProperty('cursor', originalCursor, originalCursorPriority);
    } else viewport.style.removeProperty('cursor');
    map.removeInteraction(modify);
    map.removeLayer(layer);
    if (toolbar) {
      toolbar.removeEventListener('click', handleToolbar);
    }
    if (clearButton) {
      clearButton.removeEventListener('click', clear);
    }
    container.removeEventListener('keydown', handleKey);
  }
}
