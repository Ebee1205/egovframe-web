const MAP_META_KEY = '__openlayers_tile_meta_data';

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
      url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      minZoom: 0,
      maxZoom: 19,
      attributions: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>',
    },

    satellite: {
      type: 'XYZ',
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      minZoom: 0,
      maxZoom: 19,
      attributions: 'Tiles © <a href="https://www.esri.com/">Esri</a>',
    },

    terrain: {
      type: 'XYZ',
      url: 'https://tile.opentopomap.org/{z}/{x}/{y}.png',
      minZoom: 0,
      maxZoom: 17,
      attributions: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>, SRTM | Map style: <a href="https://opentopomap.org/">OpenTopoMap</a> (CC-BY-SA)',
    },
  },

  view: {
    center: [126.9780, 37.5665],
    zoom: 12,
    minZoom: 0,
    maxZoom: 19,
  },

  projection: 'EPSG:3857',

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
      const savedTileSources = saved.tileSources ?? {};
      const tileSources = {
        ...DEFAULT_MAP_META.tileSources,
        ...savedTileSources,
      };

      for (const [mapType, defaultSource] of Object.entries(DEFAULT_MAP_META.tileSources)) {
        const savedSource = savedTileSources[mapType] ?? {};
        const isLegacyTerrainUrl = mapType === MAP_TYPE.TERRAIN
          && savedSource.url?.includes('/World_Topo_Map/MapServer/tile/');

        tileSources[mapType] = {
          ...defaultSource,
          ...savedSource,
          url: !savedSource.url || isLegacyTerrainUrl
            ? defaultSource.url
            : savedSource.url,
          maxZoom: Math.min(savedSource.maxZoom ?? defaultSource.maxZoom, defaultSource.maxZoom),
          attributions: savedSource.attributions ?? defaultSource.attributions,
        };
      }

      return {
        ...structuredClone(DEFAULT_MAP_META),
        ...saved,

        tileSources,

        view: {
          ...DEFAULT_MAP_META.view,
          ...saved.view,
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
    center,
    zoom,
  }) {
    const current = this.get();

    return this.save({
      view: {
        ...current.view,

        ...(center && {
          center,
        }),

        ...(zoom !== undefined && {
          zoom,
        }),
      },
    });
  }

  setCenter(center) {
    return this.updateView({
      center,
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