<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<section id="overview-weather-hud" class="overview-weather-hud | card | shadow-sm" aria-label="현재 날씨">
    <div class="card-header | bg-transparent | border-0 | d-flex | justify-content-between | align-items-center | pt-3 | pb-0 | px-3">
        <strong class="small">현재 날씨</strong>
        <button type="button" class="btn | btn-light | btn-sm | rounded-pill | py-1 | px-2"
                data-weather-fetch data-url="<c:url value='/overview/weather.do'/>">
            <span class="spinner-border | spinner-border-sm | me-1" data-weather-spinner hidden aria-hidden="true"></span>날씨 조회 ↗
        </button>
    </div>
    <div class="card-body | p-0">
        <p class="weather-summary-date | text-body-secondary | mx-3 | mt-2 | mb-0" data-weather-status role="status" aria-live="polite">날씨 조회 버튼을 눌러주세요.</p>
        <div class="alert | alert-danger | m-3 | p-2" data-weather-error role="alert" hidden></div>
        <div data-weather-content hidden>
            <div class="d-flex | align-items-end | justify-content-between | gap-3 | px-3 | pt-2 | pb-3">
                <div class="weather-main-temperature | fw-bold | lh-1">
                    <span data-weather-temperature>—</span><small class="weather-main-unit | fw-normal | ms-1" data-weather-temperature-unit></small>
                </div>
                <span class="small | fw-semibold | pb-1" data-weather-condition></span>
            </div>
            <div class="weather-metrics-scroll | border-top" tabindex="0" role="region" aria-label="날씨 상세 항목, 좁은 화면에서는 좌우로 이동">
                <div class="weather-metrics-row | row | row-cols-6 | g-0 | flex-nowrap | m-0">
                    <div class="col | weather-detail-cell | px-2 | py-3" data-metric="RN1">
                        <div class="weather-detail-label | text-body-secondary | text-nowrap">1시간 강수량</div>
                        <div class="weather-detail-value | fw-semibold | text-nowrap | mt-1"><span data-field="value">—</span><small class="weather-detail-unit | fw-normal | ms-1" data-field="unit"></small></div>
                    </div>
                    <div class="col | weather-detail-cell | px-2 | py-3" data-metric="UUU">
                        <div class="weather-detail-label | text-body-secondary | text-nowrap">동서바람</div>
                        <div class="weather-detail-value | fw-semibold | text-nowrap | mt-1"><span data-field="value">—</span><small class="weather-detail-unit | fw-normal | ms-1" data-field="unit"></small></div>
                    </div>
                    <div class="col | weather-detail-cell | px-2 | py-3" data-metric="VVV">
                        <div class="weather-detail-label | text-body-secondary | text-nowrap">남북바람</div>
                        <div class="weather-detail-value | fw-semibold | text-nowrap | mt-1"><span data-field="value">—</span><small class="weather-detail-unit | fw-normal | ms-1" data-field="unit"></small></div>
                    </div>
                    <div class="col | weather-detail-cell | px-2 | py-3" data-metric="REH">
                        <div class="weather-detail-label | text-body-secondary | text-nowrap">습도</div>
                        <div class="weather-detail-value | fw-semibold | text-nowrap | mt-1"><span data-field="value">—</span><small class="weather-detail-unit | fw-normal | ms-1" data-field="unit"></small></div>
                    </div>
                    <div class="col | weather-detail-cell | px-2 | py-3" data-metric="VEC">
                        <div class="weather-detail-label | text-body-secondary | text-nowrap">풍향</div>
                        <div class="weather-detail-value | fw-semibold | text-nowrap | mt-1"><span data-field="value">—</span><small class="weather-detail-unit | fw-normal | ms-1" data-field="unit"></small></div>
                    </div>
                    <div class="col | weather-detail-cell | px-2 | py-3" data-metric="WSD">
                        <div class="weather-detail-label | text-body-secondary | text-nowrap">풍속</div>
                        <div class="weather-detail-value | fw-semibold | text-nowrap | mt-1"><span data-field="value">—</span><small class="weather-detail-unit | fw-normal | ms-1" data-field="unit"></small></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="card-footer | bg-transparent | text-body-secondary | border-top | py-2 | px-3" data-weather-content-footer hidden>
        출처: 기상청 <span class="float-end" data-weather-grid></span>
    </div>
</section>

<script>
(function () {
    var hud = document.getElementById('overview-weather-hud');
    var $ = function (name) { return hud.querySelector('[data-weather-' + name + ']'); };

    var button = $('fetch'), spinner = $('spinner'), status = $('status'), error = $('error');
    var content = $('content'), footer = $('content-footer');
    var metrics = hud.querySelectorAll('[data-metric]');

    function setText(el, text) { el.textContent = text == null ? '' : text; }

    function showError(message) {
        setText(error, message);
        error.hidden = !message;
    }

    function find(items, category) {
        return items.find(function (i) { return i.category === category; });
    }

    function render(items, common) {
        var date = String(common.baseDate || '').replace(/^(\d{4})(\d{2})(\d{2})$/, '$1.$2.$3');
        var time = String(common.baseTime || '').replace(/^(\d{2})(\d{2})$/, '$1:$2');
        var temp = find(items, 'T1H');
        var cond = find(items, 'PTY');

        setText(status, date + ' ─ ' + time + ' 발표 (KST)');
        setText($('grid'), '격자 ' + common.nx + ', ' + common.ny);
        setText($('temperature'), temp && temp.obsrValue != null ? temp.obsrValue : '—');
        setText($('temperature-unit'), temp && temp.unit);
        setText($('condition'), cond
            ? [cond.emoji, cond.valueName || cond.categoryName || cond.category].filter(Boolean).join(' ') : '');

        // 고정 타일에 값만 매핑
        metrics.forEach(function (tile) {
            var item = find(items, tile.dataset.metric);
            setText(tile.querySelector('[data-field="value"]'), item && item.obsrValue != null ? item.obsrValue : '—');
            setText(tile.querySelector('[data-field="unit"]'), item && item.unit);
            tile.title = (item && item.description) || '';
        });

        content.hidden = footer.hidden = false;
    }

    button.addEventListener('click', async function () {
        button.disabled = true;
        spinner.hidden = false;
        hud.setAttribute('aria-busy', 'true');
        setText(status, '날씨 정보를 조회하고 있습니다.');
        showError('');
        content.hidden = footer.hidden = true;

        try {
            var response = await fetch(button.dataset.url, {
                headers: { 'Accept': 'application/json' }, credentials: 'same-origin'
            });
            var result = await response.json();
            if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS') {
                throw new Error(result.code
                    ? '[' + result.code.resultCode + '] ' + result.code.message : '날씨 조회에 실패했습니다.');
            }
            var data = result.data;
            var items = Array.isArray(data) ? data : data && data.items;
            if (!Array.isArray(items) || items.length === 0) {
                setText(status, '조회된 날씨 정보가 없습니다.');
                return;
            }
            render(items, Array.isArray(data) ? items[0] : data);
        } catch (e) {
            setText(status, '');
            showError(e instanceof SyntaxError
                ? '서버 응답을 확인할 수 없습니다. 로그인 상태를 확인해주세요.'
                : (e.message || '날씨 정보를 불러오지 못했습니다.'));
        } finally {
            button.disabled = false;
            spinner.hidden = true;
            hud.removeAttribute('aria-busy');
        }
    });
})();
</script>
