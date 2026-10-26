/**
 * 공통 위치 마커. 경도·위도 데이터를 받아 독립된 Vector 레이어에 표시한다.
 * setItems / has / isSelected / select / hover / fitAll / destroy를 제공한다.
 */
export function createMapMarker(map, options = {}) {
  // ----- 선언부 ----- //
  const ol = window.ol;
  const view = map.getView();
  const entries = new Map();
  const target = map.getTargetElement();
  const originalTitle = target.title;
  const originalCursor = target.style.cursor;

  let selected = null;
  let selectedId = null;
  let hovered = null;
  let isDisposed = false;

  // ----- 마커 레이어 생성 및 지도 등록 ----- //
  const source = new ol.source.Vector();
  const layer = new ol.layer.Vector({
    source,
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
    isSelected,
    select,
    hover,
    fitAll,
    destroy,
  };

  // ----- 함수 정의 ----- //

  function createMarkerStyle(item, active, over) {
    if (options.style) {
      return options.style(item, active, over);
    }
    const opacity = getMarkerOpacity(item, active, over);
    const defaultIcon = item.icon ?? options.icon ?? new URL('../place-mark.svg', import.meta.url).href;
    const icon = active
      ? item.selectedIcon ?? options.selectedIcon ?? new URL('../place-mark-spot.svg', import.meta.url).href
      : defaultIcon;
    const config = typeof icon === 'string' ? { src: icon } : { ...icon };
    const image = new ol.style.Icon({
      anchor: [0.5, 1],
      anchorXUnits: 'fraction',
      anchorYUnits: 'fraction',
      ...config,
      opacity: (config.opacity ?? 1) * opacity,
    });
    return new ol.style.Style({
      image,
      zIndex: over ? 3 : active ? 2 : 1,
    });
  }

  function getMarkerOpacity(item, active, over) {
    if (active) return item.selectedOpacity ?? options.selectedOpacity ?? 1;
    if (over) return item.hoverOpacity ?? options.hoverOpacity ?? 1;

    return item.opacity ?? options.opacity ?? 0.5;
  }

  function isValidLocation(data) {
    if (data.lat == null || data.lon == null) return false;
    if (String(data.lat).trim() === '' || String(data.lon).trim() === '') return false;

    const lat = Number(data.lat);
    const lon = Number(data.lon);

    return Number.isFinite(lat) && Number.isFinite(lon)
      && lat >= -85.05112878 && lat <= 85.05112878
      && lon >= -180 && lon <= 180;
  }

  function refreshMarker(entry) {
    if (entry) {
      entry.feature.setStyle(createMarkerStyle(entry.item, entry === selected, entry === hovered));
    }
  }

  function hover(id) {
    if (isDisposed) return;
    const next = id == null ? null : entries.get(String(id)) || null;
    if (next === hovered) return;
    const previous = hovered;
    hovered = next;
    refreshMarker(previous);
    refreshMarker(hovered);
  }

  function fit(geometry, maxZoom) {
    map.updateSize();
    const size = map.getSize();
    if (!size || size[0] <= 0 || size[1] <= 0) return;
    view.fit(geometry, {
      padding: options.getPadding ? options.getPadding(size) : [40, 40, 40, 40],
      maxZoom,
      duration: 250,
    });
  }

  function select(id, fromMap = false) {
    if (isDisposed) return false;
    const entry = entries.get(String(id));
    if (!entry) return false;
    const previous = selected;
    selected = entry;
    selectedId = String(id);
    refreshMarker(previous);
    refreshMarker(entry);
    if (options.onSelect) {
      options.onSelect(entry.item, {
        fromMap,
      });
    }
    // 선택 상태만 변경하고 지도 영역은 유지한다.
    return true;
  }

  function fitAll() {
    if (!isDisposed && source.getFeatures().length) {
      fit(source.getExtent(), 14);
    }
  }

  function setItems(items) {
    if (isDisposed) return;
    selected = null;
    hovered = null;
    entries.clear();
    source.clear();
    target.style.cursor = originalCursor;
    target.title = originalTitle;
    (items || []).forEach((data) => {
      const lat = Number(data.lat);
      const lon = Number(data.lon);
      const id = data.id == null ? '' : String(data.id);
      if (!id || entries.has(id) || !isValidLocation(data)) {
        return;
      }
      const item = {
        ...data,
      };
      const feature = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.fromLonLat([lon, lat], view.getProjection())),
        markerId: id,
      });
      const entry = { item, feature };
      if (id === selectedId) selected = entry;
      feature.setStyle(createMarkerStyle(item, entry === selected, false));
      entries.set(id, entry);
      source.addFeature(feature);
    });
    // 목록 갱신은 지도 영역 변경이나 재조회를 유발하지 않는다.
  }

  function featureAt(pixel) {
    return map.forEachFeatureAtPixel(pixel, (feature) => feature, {
      layerFilter: (candidate) => candidate === layer,
      hitTolerance: 6,
    });
  }

  function handleClick(event) {
    const feature = featureAt(event.pixel);
    if (feature) {
      select(feature.get('markerId'), true);
    }
  }

  function handlePointer(event) {
    if (event.dragging) return;
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

  // ----- 리소스 정리 ----- //

  function destroy() {
    if (isDisposed) return;
    isDisposed = true;
    map.un('singleclick', handleClick);
    map.un('pointermove', handlePointer);
    target.removeEventListener('pointerleave', handleLeave);
    map.removeLayer(layer);
    source.clear();
    entries.clear();
    target.style.cursor = originalCursor;
    target.title = originalTitle;
  }

  function isSelected(id) {
    return !isDisposed && selected !== null && String(id) === selectedId;
  }

  function has(id) {
    return !isDisposed && entries.has(String(id));
  }
}
