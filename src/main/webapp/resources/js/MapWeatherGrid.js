/**
 * 기상청 동네예보 격자 좌표 변환. Lambert 정각원추도법과 5km 간격을 사용한다.
 * https://apihub.kma.go.kr/apiList.do?seqApi=10
 */
const RAD = Math.PI / 180;
const RE = 6371.00877 / 5;
const SN = Math.log(Math.cos(30 * RAD) / Math.cos(60 * RAD))
  / Math.log(Math.tan(Math.PI / 4 + 60 * RAD / 2) / Math.tan(Math.PI / 4 + 30 * RAD / 2));
const SF = Math.pow(Math.tan(Math.PI / 4 + 30 * RAD / 2), SN) * Math.cos(30 * RAD) / SN;
const RO = RE * SF / Math.pow(Math.tan(Math.PI / 4 + 38 * RAD / 2), SN);

export function lonLatToGrid(lon, lat) {
  const ra = RE * SF / Math.pow(Math.tan(Math.PI / 4 + lat * RAD / 2), SN);
  const theta = ((lon - 126 + 540) % 360 - 180) * RAD * SN;
  const nx = Math.floor(ra * Math.sin(theta) + 43.5);
  const ny = Math.floor(RO - ra * Math.cos(theta) + 136.5);
  return { nx, ny, inRange: nx >= 1 && nx <= 149 && ny >= 1 && ny <= 253 };
}

export function gridToLonLat(x, y) {
  const dx = x - 43;
  const dy = RO - y + 136;
  const ra = Math.hypot(dx, dy);
  return [126 + Math.atan2(dx, dy) / SN / RAD,
    (2 * Math.atan(Math.pow(RE * SF / ra, 1 / SN)) - Math.PI / 2) / RAD];
}

/**
 * 기상청 격자를 독립된 Vector 레이어에 표시하고 지도 중심 격자를 강조한다.
 * setVisible / destroy를 제공한다.
 */
export function createMapWeatherGrid(map, options = {}) {
  // ----- 선언부 ----- //
  const ol = window.ol;
  const view = map.getView();
  const projection = view.getProjection();
  const selected = new ol.Feature();

  let isInitialized = false;
  let selectedKey = '';
  let isDisposed = false;

  // ----- 격자 스타일 생성 ----- //
  const lineStyle = new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'rgba(0, 100, 180, 0.55)',
      width: 1,
    }),
  });
  const centerStyle = new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: '#0077cc',
      width: 2,
    }),
    fill: new ol.style.Fill({
      color: 'rgba(0, 119, 204, 0.14)',
    }),
    text: new ol.style.Text({
      font: 'bold 13px sans-serif',
      fill: new ol.style.Fill({
        color: '#005a9c',
      }),
      stroke: new ol.style.Stroke({
        color: '#fff',
        width: 3,
      }),
    }),
  });
  // ----- 격자 레이어 생성 및 지도 등록 ----- //
  const source = new ol.source.Vector({
    wrapX: false,
  });
  const layer = new ol.layer.Vector({
    source,
    visible: options.visible ?? false,
    zIndex: 5,
    style: (feature) => feature === selected ? centerStyle : lineStyle,
  });
  layer.set('name', 'weatherGrid');
  map.addLayer(layer);

  // ----- 이벤트 등록 및 초기화 ----- //
  map.on('moveend', refresh);
  refresh();

  return {
    layer,
    setVisible,
    destroy,
  };

  // ----- 함수 정의 ----- //

  function project(x, y) {
    return ol.proj.fromLonLat(gridToLonLat(x, y), projection);
  }

  function initialize() {
    if (isInitialized) return;
    isInitialized = true;
    const features = [];
    // 정수 좌표인 예보 지점을 중심으로 반 격자 간격에 경계선을 그린다.
    for (let x = 0.5; x <= 149.5; x++) {
      const points = [];
      for (let y = 0.5; y <= 253.5; y++) {
        points.push(project(x, y));
      }
      features.push(new ol.Feature(new ol.geom.LineString(points)));
    }
    for (let y = 0.5; y <= 253.5; y++) {
      const points = [];
      for (let x = 0.5; x <= 149.5; x++) {
        points.push(project(x, y));
      }
      features.push(new ol.Feature(new ol.geom.LineString(points)));
    }
    source.addFeatures([...features, selected]);
  }

  function refresh() {
    if (isDisposed || !layer.getVisible()) return;
    initialize();
    const center = view.getCenter();
    if (!center) return;
    const grid = lonLatToGrid(...ol.proj.toLonLat(center, projection));
    const key = [grid.nx, grid.ny, grid.inRange].join(',');
    if (key === selectedKey) return;
    selectedKey = key;
    selected.setGeometry(grid.inRange ? new ol.geom.Polygon([[
      project(grid.nx - 0.5, grid.ny - 0.5), project(grid.nx + 0.5, grid.ny - 0.5),
      project(grid.nx + 0.5, grid.ny + 0.5), project(grid.nx - 0.5, grid.ny + 0.5),
      project(grid.nx - 0.5, grid.ny - 0.5),
    ]]) : undefined);
    centerStyle.getText().setText(grid.nx + ', ' + grid.ny);
    layer.changed();
  }

  function setVisible(visible) {
    if (isDisposed) return;
    layer.setVisible(Boolean(visible));
    refresh();
  }

  // ----- 리소스 정리 ----- //

  function destroy() {
    if (isDisposed) return;
    isDisposed = true;
    map.un('moveend', refresh);
    map.removeLayer(layer);
    source.clear();
  }
}
