<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<section id="overview-weather-hud" class="overview-weather-hud card shadow-sm" aria-label="현재 날씨">
    <div class="card-body p-3">
        <strong>현재 날씨</strong>
        <p data-weather-status class="mb-2 mt-2" role="status" aria-live="polite">날씨 조회 버튼을 눌러주세요.</p>
        <ul data-weather-items class="list-unstyled mb-2"></ul>
        <small data-weather-source class="text-body-secondary" hidden>출처: 기상청</small>
        <div class="mt-2">
            <button type="button" data-weather-fetch data-url="<c:url value='/overview/weather.do'/>"
                    class="btn btn-sm btn-outline-secondary">날씨 조회</button>
        </div>
    </div>
</section>
<script>
(function () {
    var hud = document.getElementById('overview-weather-hud');
    var button = hud.querySelector('[data-weather-fetch]');
    var status = hud.querySelector('[data-weather-status]');
    var list = hud.querySelector('[data-weather-items]');
    var source = hud.querySelector('[data-weather-source]');

    button.addEventListener('click', async function () {
        button.disabled = true;
        hud.setAttribute('aria-busy', 'true');
        status.textContent = '날씨 정보를 조회하고 있습니다.';
        list.textContent = '';
        source.hidden = true;
        try {
            var response = await fetch(button.dataset.url, {
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var result = await response.json();
            if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS') {
                throw new Error(result.code
                    ? '[' + result.code.resultCode + '] ' + result.code.message
                    : '날씨 조회에 실패했습니다.');
            }
            var items = result.data;
            if (!Array.isArray(items) || items.length === 0) {
                status.textContent = '조회된 날씨 정보가 없습니다.';
                return;
            }
            var first = items[0];
            status.textContent = '발표: ' + first.baseDate + ' ' + first.baseTime
                + ' (KST) / 격자: ' + first.nx + ', ' + first.ny;
            items.forEach(function (item) {
                var li = document.createElement('li');
                li.textContent = item.category + ': ' + item.obsrValue;
                list.appendChild(li);
            });
            source.hidden = false;
        } catch (error) {
            status.textContent = error instanceof SyntaxError
                ? '서버 응답을 확인할 수 없습니다. 로그인 상태를 확인해주세요.'
                : (error.message || '날씨 정보를 불러오지 못했습니다.');
        } finally {
            button.disabled = false;
            hud.removeAttribute('aria-busy');
        }
    });
})();
</script>
