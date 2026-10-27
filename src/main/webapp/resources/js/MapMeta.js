import { lonLatToGrid } from './MapWeatherGrid.js?v=20261027-grid2';

const MAP_META_KEY = '__openlayers_tile_meta_data';
export const VWORLD_API_KEY =
  document.querySelector('meta[name="vworld-api-key"]')?.content ?? '';

export const MAP_TYPE = {
  BASIC: 'basic',
  SATELLITE: 'satellite',
  TERRAIN: 'terrain',
};

const DEFAULT_MAP_META = {
  mapType: MAP_TYPE.BASIC,

  tileSources: {
    basic: {
      type: 'XYZ',
      url: `https://api.vworld.kr/req/wmts/1.0.0/${VWORLD_API_KEY}/Base/{z}/{y}/{x}.png`,
      minZoom: 6,
      maxZoom: 19,
      attributions: '© <a href="https://www.vworld.kr/">공간정보 오픈플랫폼 VWorld</a>',
    },

    satellite: {
      type: 'XYZ',
      url: `https://api.vworld.kr/req/wmts/1.0.0/${VWORLD_API_KEY}/Satellite/{z}/{y}/{x}.jpeg`,
      minZoom: 6,
      maxZoom: 19,
      attributions: '© <a href="https://www.vworld.kr/">공간정보 오픈플랫폼 VWorld</a>',
    },

    terrain: {
      type: 'XYZ',
      url: 'https://tile.opentopomap.org/{z}/{x}/{y}.png',
      minZoom: 0,
      maxZoom: 17,
      attributions: '© <a href="https://www.vworld.kr/">공간정보 오픈플랫폼 VWorld</a>',
    },
  },

  view: {
    cenLat: 37.5665,
    cenLon: 126.9780,
    zoom: 12,
    minZoom: 0,
    maxZoom: 19,
  },

  projection: 'EPSG:3857',

  weatherGrid: { visible: false, ...lonLatToGrid(126.9780, 37.5665) },

  updateTime: null,
};

class MapMeta {
  get() {
    const data = localStorage.getItem(MAP_META_KEY);

    if (!data) {
      return structuredClone(DEFAULT_MAP_META);
    }

    try {
      const saved = JSON.parse(data);
      const { center: legacyCenter, ...savedView } = saved.view ?? {};
      const savedTileSources = saved.tileSources ?? {};
      const tileSources = {
        ...DEFAULT_MAP_META.tileSources,
        ...savedTileSources,
      };

      for (const [mapType, defaultSource] of Object.entries(DEFAULT_MAP_META.tileSources)) {
        const savedSource = savedTileSources[mapType] ?? {};
        const isLegacyTerrainUrl = mapType === MAP_TYPE.TERRAIN
          && savedSource.url?.includes('/World_Topo_Map/MapServer/tile/');
        // VWorld URL은 현재 페이지에서 전달받은 키로 생성한다.
        const usesVworld = defaultSource.url.includes('https://api.vworld.kr/');

        tileSources[mapType] = {
          ...defaultSource,
          ...savedSource,
          url: usesVworld || !savedSource.url || isLegacyTerrainUrl
            ? defaultSource.url
            : savedSource.url,
          minZoom: usesVworld ? defaultSource.minZoom : (savedSource.minZoom ?? defaultSource.minZoom),
          maxZoom: Math.min(savedSource.maxZoom ?? defaultSource.maxZoom, defaultSource.maxZoom),
          attributions: usesVworld ? defaultSource.attributions : (savedSource.attributions ?? defaultSource.attributions),
        };
      }

      return {
        ...structuredClone(DEFAULT_MAP_META),
        ...saved,

        tileSources,

        weatherGrid: {
          visible: saved.weatherGrid?.visible === true,
          ...lonLatToGrid(
            savedView.cenLon ?? legacyCenter?.[0] ?? DEFAULT_MAP_META.view.cenLon,
            savedView.cenLat ?? legacyCenter?.[1] ?? DEFAULT_MAP_META.view.cenLat),
        },

        view: {
          ...DEFAULT_MAP_META.view,
          ...savedView,
          cenLat: savedView.cenLat ?? legacyCenter?.[1] ?? DEFAULT_MAP_META.view.cenLat,
          cenLon: savedView.cenLon ?? legacyCenter?.[0] ?? DEFAULT_MAP_META.view.cenLon,
        },
      };
    } catch (error) {
      console.error('[MapMeta] parse error', error);

      return structuredClone(DEFAULT_MAP_META);
    }
  }

  save(meta) {
    const current = this.get();

    const next = {
      ...current,
      ...meta,
      updateTime: Date.now(),
    };

    localStorage.setItem(
      MAP_META_KEY,
      JSON.stringify(next)
    );

    return next;
  }

  getMapType() {
    return this.get().mapType;
  }

  setWeatherGridVisible(visible) {
    return this.save({ weatherGrid: { ...this.get().weatherGrid, visible: Boolean(visible) } });
  }

  setMapType(mapType) {
    if (!Object.values(MAP_TYPE).includes(mapType)) {
      console.warn(`[MapMeta] invalid map type: ${mapType}`);
      return;
    }

    this.save({
      mapType,
    });
  }

  getCurrentTileSource() {
    const meta = this.get();

    return meta.tileSources[meta.mapType];
  }

  updateView({
    cenLat,
    cenLon,
    zoom,
    minLat,
    minLon,
    maxLat,
    maxLon,
  }) {
    const current = this.get();

    return this.save({
      weatherGrid: {
        ...current.weatherGrid,
        ...lonLatToGrid(cenLon ?? current.view.cenLon, cenLat ?? current.view.cenLat),
      },
      view: {
        ...current.view,

        ...(cenLat !== undefined && { cenLat }),
        ...(cenLon !== undefined && { cenLon }),

        ...(zoom !== undefined && {
          zoom,
        }),

        ...(minLat !== undefined && { minLat }),
        ...(minLon !== undefined && { minLon }),
        ...(maxLat !== undefined && { maxLat }),
        ...(maxLon !== undefined && { maxLon }),
      },
    });
  }

  setCenter(cenLat, cenLon) {
    return this.updateView({
      cenLat,
      cenLon,
    });
  }

  setZoom(zoom) {
    return this.updateView({
      zoom,
    });
  }

  updateTileSource(
    mapType,
    source
  ) {
    const current = this.get();

    return this.save({
      tileSources: {
        ...current.tileSources,

        [mapType]: {
          ...current.tileSources[mapType],
          ...source,
        },
      },
    });
  }

  resetView() {
    const current = this.get();

    return this.save({
      view: structuredClone(DEFAULT_MAP_META.view),
      weatherGrid: { ...current.weatherGrid, ...lonLatToGrid(DEFAULT_MAP_META.view.cenLon, DEFAULT_MAP_META.view.cenLat) },
    });
  }

  reset() {
    localStorage.removeItem(MAP_META_KEY);

    return structuredClone(DEFAULT_MAP_META);
  }
}

export const mapMeta = new MapMeta();

export {
  MAP_META_KEY,
  DEFAULT_MAP_META,
};

export default MapMeta;
