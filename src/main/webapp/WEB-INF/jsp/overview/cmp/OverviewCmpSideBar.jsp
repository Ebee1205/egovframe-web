<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="mspId" value="${empty param.panelId ? 'mapSidePanel' : param.panelId}"/>
<c:set var="mspWidth" value="${empty param.panelWidth ? '300px' : param.panelWidth}"/>
<c:set var="mspFolded" value="${param.panelFolded == 'true'}"/>
<c:set var="mspContent" value="${param.contentPage}"/>
<div id="<c:out value='${mspId}'/>" class="map-side-panel ${mspFolded ? 'folded' : ''}"
    style="--map-side-panel-width: <c:out value='${mspWidth}'/>">

    <div class="row g-0 | panel-content" ${mspFolded ? 'inert' : ''}>
        <c:if test="${not empty mspContent and fn:startsWith(mspContent, '/WEB-INF/jsp/') and not fn:contains(mspContent, '..')}">
            <jsp:include page="${mspContent}"/>
        </c:if>
    </div>

    <button type="button" class="fold-button" aria-expanded="${mspFolded ? 'false' : 'true'}"
        aria-label="${mspFolded ? '패널 펼치기' : '패널 접기'}">
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M10 3 5 8l5 5"/>
        </svg>
    </button>
</div>

<script>
    (function () {
        var panel = document.getElementById('<c:out value="${mspId}"/>');
        var btn = panel.querySelector('.fold-button');
        var content = panel.querySelector('.panel-content');

        btn.addEventListener('click', function () {
            var isFolded = panel.classList.toggle('folded');
            btn.setAttribute('aria-expanded', String(!isFolded));
            btn.setAttribute('aria-label', isFolded ? '패널 펼치기' : '패널 접기');
            content.inert = isFolded;   // 접힌 동안 내부 요소로 Tab 포커스가 가지 않도록
        });
    })();
</script>