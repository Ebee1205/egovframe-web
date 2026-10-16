/**
 * Shared point-marker layer. Coordinates use EPSG:4326 (lon/lat).
 * items: [{ id, lat, lon, title, color?, hoverColor?, icon?, ...customData }]
 * options: { items, color, hoverColor, opacity, selectedOpacity, hoverOpacity,
 *   icon, onSelect(item, {fromMap}), getPadding(size), style(item, selected, hovered) }
 * Appearance may be supplied per item or shared in options. icon accepts a URL
 * or ol.style.Icon options (src, anchor, width, height, scale, etc.). Images keep
 * their original colors unless icon.color explicitly supplies an OpenLayers tint.
 * Returns { setItems(items), has(id), select(id), hover(id|null), fitAll(), destroy() }.
 * UI bindings and application-specific data belong to the caller.
 */
export function createMapMarker(map, options = {}) {
    const ol = window.ol;
    const view = map.getView();
    const source = new ol.source.Vector();
    const layer = new ol.layer.Vector({source});
    const entries = new Map();
    let selected = null;
    let hovered = null;
    let disposed = false;
    let fitFrame = null;
    const target = map.getTargetElement();
    const originalTitle = target.title;
    const originalCursor = target.style.cursor;
    map.addLayer(layer);

    function markerStyle(item, active, over) {
        if (options.style) return options.style(item, active, over);
        const color = over ? (item.hoverColor ?? options.hoverColor ?? '#4645ab')
            : (item.color ?? options.color ?? '#5856d6');
        const opacity = over ? (item.hoverOpacity ?? options.hoverOpacity ?? 1)
            : active ? (item.selectedOpacity ?? options.selectedOpacity ?? 1)
            : (item.opacity ?? options.opacity ?? 0.5);
        const icon = item.icon ?? options.icon;
        let image;
        if (icon) {
            const config = typeof icon === 'string' ? {src: icon} : {...icon};
            image = new ol.style.Icon({...config, opacity: (config.opacity ?? 1) * opacity});
        } else {
            image = new ol.style.Circle({
                radius: active ? 18 : 15,
                fill: new ol.style.Fill({color}),
                stroke: new ol.style.Stroke({color: '#fff', width: 3})
            });
            image.setOpacity(opacity);
        }
        return new ol.style.Style({
            image,
            zIndex: over ? 3 : active ? 2 : 1
        });
    }

    function refresh(entry) {
        if (entry) entry.feature.setStyle(markerStyle(entry.item, entry === selected, entry === hovered));
    }

    function hover(id) {
        if (disposed) return;
        const next = id == null ? null : (entries.get(String(id)) || null);
        if (next === hovered) return;
        const previous = hovered;
        hovered = next;
        refresh(previous);
        refresh(hovered);
    }

    function fit(geometry, maxZoom) {
        map.updateSize();
        const size = map.getSize();
        if (!size || size[0] <= 0 || size[1] <= 0) return;
        view.fit(geometry, {
            padding: options.getPadding ? options.getPadding(size) : [40, 40, 40, 40],
            maxZoom, duration: 250
        });
    }

    function select(id, fromMap = false) {
        if (disposed) return false;
        const entry = entries.get(String(id));
        if (!entry) return false;
        const previous = selected;
        selected = entry;
        refresh(previous);
        refresh(entry);
        // Let the caller reveal its list/panel before calculating viewport padding.
        if (options.onSelect) options.onSelect(entry.item, {fromMap});
        fit(entry.feature.getGeometry(), Math.max(view.getZoom() || 0, 15));
        return true;
    }

    function fitAll() {
        if (!disposed && source.getFeatures().length) fit(source.getExtent(), 14);
    }

    function setItems(items) {
        if (disposed) return;
        if (fitFrame !== null) cancelAnimationFrame(fitFrame);
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
            // Preserve numeric zero, but reject missing/blank coordinates.
            if (!id || entries.has(id) || data.lat == null || data.lon == null
                || String(data.lat).trim() === '' || String(data.lon).trim() === ''
                || !Number.isFinite(lat) || !Number.isFinite(lon)
                || lat < -85.05112878 || lat > 85.05112878 || lon < -180 || lon > 180) return;
            const item = {...data};
            const feature = new ol.Feature({
                geometry: new ol.geom.Point(ol.proj.fromLonLat([lon, lat], view.getProjection())),
                markerId: id
            });
            feature.setStyle(markerStyle(item, false, false));
            entries.set(id, {item, feature});
            source.addFeature(feature);
        });
        fitFrame = requestAnimationFrame(function () { fitFrame = null; fitAll(); });
    }

    function featureAt(pixel) {
        return map.forEachFeatureAtPixel(pixel, function (feature) { return feature; }, {
            layerFilter: function (candidate) { return candidate === layer; }, hitTolerance: 6
        });
    }
    function handleClick(event) {
        const feature = featureAt(event.pixel);
        if (feature) select(feature.get('markerId'), true);
    }
    function handlePointer(event) {
        if (event.dragging) return;
        const feature = featureAt(event.pixel);
        const entry = feature && entries.get(feature.get('markerId'));
        hover(entry ? feature.get('markerId') : null);
        target.style.cursor = entry ? 'pointer' : originalCursor;
        target.title = entry ? String(entry.item.title || '') : originalTitle;
    }
    map.on('singleclick', handleClick);
    map.on('pointermove', handlePointer);
    function handleLeave() {
        hover(null);
        target.style.cursor = originalCursor;
        target.title = originalTitle;
    }
    target.addEventListener('pointerleave', handleLeave);
    setItems(options.items);

    function destroy() {
        if (disposed) return;
        disposed = true;
        if (fitFrame !== null) cancelAnimationFrame(fitFrame);
        map.un('singleclick', handleClick);
        map.un('pointermove', handlePointer);
        target.removeEventListener('pointerleave', handleLeave);
        map.removeLayer(layer);
        source.clear();
        entries.clear();
        target.style.cursor = originalCursor;
        target.title = originalTitle;
    }
    function has(id) { return !disposed && entries.has(String(id)); }
    return {setItems, has, select, hover, fitAll, destroy};
}
