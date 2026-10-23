/**
 * 공통 위치 마커. 경도·위도 데이터를 받아 독립된 Vector 레이어에 표시한다.
 * setItems / has / select / hover / fitAll / destroy를 제공한다.
 */
export function createMapMarker(map, options = {}) {

  // ----- 선언부----- //
  const ol = window.ol;
  const view = map.getView();
  const entries = new Map();
  let selected = null;
  let hovered = null;
  let disposed = false;
  let fitFrame = null;
  const target = map.getTargetElement();
  const originalTitle = target.title;
  const originalCursor = target.style.cursor;

  // ----- 마커 레이어 생성 및 지도 등록 ----- //
  const source = new ol.source.Vector();
  const layer = new ol.layer.Vector({
    source
  });
  map.addLayer(layer);

  // ----- 이벤트 등록 및 초기화 ----- //
  map.on('singleclick', handleClick);
  map.on('pointermove', handlePointer);
  target.addEventListener('pointerleave', handleLeave);
  setItems(options.items);

  return {
    setItems,
    has,
    select,
    hover,
    fitAll,
    destroy
  };

  // ----- 함수 정의 ----- //

  function markerStyle(item, active, over) {
    if (options.style) {
      return options.style(item, active, over);
    }
    const color = over ? item.hoverColor ?? options.hoverColor ?? '#4645ab' : item.color ?? options.color ?? '#5856d6';
    const opacity = over ? item.hoverOpacity ?? options.hoverOpacity ?? 1 : active ? item.selectedOpacity ?? options.selectedOpacity ?? 1 : item.opacity ?? options.opacity ?? 0.5;
    const icon = item.icon ?? options.icon;
    let image;
    if (icon) {
      const config = typeof icon === 'string' ? {
        src: icon
      } : {
        ...icon
      };
      image = new ol.style.Icon({
        anchor: [0.5, 1],
        ...config,
        opacity: (config.opacity ?? 1) * opacity
      });
    } else {
      const fill = String(color).replace(/[&<>"']/g, function (character) {
        return {
          '&': '&amp;',
          '<': '&lt;',
          '>': '&gt;',
          '"': '&quot;',
          "'": '&apos;'
        }[character];
      });
      const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="44" viewBox="0 0 32 44">' + '<path fill="' + fill + '" d="M16 0C7.16 0 0 7.16 0 16C0 28 16 44 16 44S32 28 32 16C32 7.16 24.84 0 16 0Z"/>' + '<circle cx="16" cy="16" r="6" fill="#fff"/></svg>';
      image = new ol.style.Icon({
        src: 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg),
        anchor: [0.5, 1],
        anchorXUnits: 'fraction',
        anchorYUnits: 'fraction',
        width: active ? 38 : 32,
        opacity
      });
    }
    return new ol.style.Style({
      image,
      zIndex: over ? 3 : active ? 2 : 1
    });
  }

  function refresh(entry) {
    if (entry) {
      entry.feature.setStyle(markerStyle(entry.item, entry === selected, entry === hovered));
    }
  }

  function hover(id) {
    if (disposed) {
      return;
    }
    const next = id == null ? null : entries.get(String(id)) || null;
    if (next === hovered) {
      return;
    }
    const previous = hovered;
    hovered = next;
    refresh(previous);
    refresh(hovered);
  }

  function fit(geometry, maxZoom) {
    map.updateSize();
    const size = map.getSize();
    if (!size || size[0] <= 0 || size[1] <= 0) {
      return;
    }
    view.fit(geometry, {
      padding: options.getPadding ? options.getPadding(size) : [40, 40, 40, 40],
      maxZoom,
      duration: 250
    });
  }

  function select(id, fromMap = false) {
    if (disposed) {
      return false;
    }
    const entry = entries.get(String(id));
    if (!entry) {
      return false;
    }
    const previous = selected;
    selected = entry;
    refresh(previous);
    refresh(entry);
    if (options.onSelect) {
      options.onSelect(entry.item, {
        fromMap
      });
    }
    fit(entry.feature.getGeometry(), Math.max(view.getZoom() || 0, 15));
    return true;
  }

  function fitAll() {
    if (!disposed && source.getFeatures().length) {
      fit(source.getExtent(), 14);
    }
  }

  function setItems(items) {
    if (disposed) {
      return;
    }
    if (fitFrame !== null) {
      cancelAnimationFrame(fitFrame);
    }
    selected = null;
    hovered = null;
    entries.clear();
    source.clear();
    target.style.cursor = originalCursor;
    target.title = originalTitle;
    (items || []).forEach(function (data) {
      const lat = Number(data.lat);
      const lon = Number(data.lon);
      const id = data.id == null ? '' : String(data.id);
      if (!id || entries.has(id) || data.lat == null || data.lon == null || String(data.lat).trim() === '' || String(data.lon).trim() === '' || !Number.isFinite(lat) || !Number.isFinite(lon) || lat < -85.05112878 || lat > 85.05112878 || lon < -180 || lon > 180) {
        return;
      }
      const item = {
        ...data
      };
      const feature = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.fromLonLat([lon, lat], view.getProjection())),
        markerId: id
      });
      feature.setStyle(markerStyle(item, false, false));
      entries.set(id, {
        item,
        feature
      });
      source.addFeature(feature);
    });
    fitFrame = requestAnimationFrame(function () {
      fitFrame = null;
      fitAll();
    });
  }

  function featureAt(pixel) {
    return map.forEachFeatureAtPixel(pixel, function (feature) {
      return feature;
    }, {
      layerFilter: function (candidate) {
        return candidate === layer;
      },
      hitTolerance: 6
    });
  }

  function handleClick(event) {
    const feature = featureAt(event.pixel);
    if (feature) {
      select(feature.get('markerId'), true);
    }
  }

  function handlePointer(event) {
    if (event.dragging) {
      return;
    }
    const feature = featureAt(event.pixel);
    const entry = feature && entries.get(feature.get('markerId'));
    hover(entry ? feature.get('markerId') : null);
    target.style.cursor = entry ? 'pointer' : originalCursor;
    target.title = entry ? String(entry.item.title || '') : originalTitle;
  }

  function handleLeave() {
    hover(null);
    target.style.cursor = originalCursor;
    target.title = originalTitle;
  }



  // ----- lifecycle - destroy ----- //

  function destroy() {
    if (disposed) {
      return;
    }
    disposed = true;
    if (fitFrame !== null) {
      cancelAnimationFrame(fitFrame);
    }
    map.un('singleclick', handleClick);
    map.un('pointermove', handlePointer);
    target.removeEventListener('pointerleave', handleLeave);
    map.removeLayer(layer);
    source.clear();
    entries.clear();
    target.style.cursor = originalCursor;
    target.title = originalTitle;
  }

  function has(id) {
    return !disposed && entries.has(String(id));
  }
}
