<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />

<c:set var="uri" value="${pageContext.request.requestURI}" />

<!-- Leftnav -->
<div class="sidebar | app-sidebar | sidebar-narrow | sidebar-fixed | border-end | h-100" id="sidebar">
    <ul id="sidebar-menu" class="sidebar-nav">

        <li class="nav-item">
            <a class="nav-link ${fn:contains(uri, '/map') ? 'active' : ''}"
               href="<c:url value='/service/map.do'/>"
               title="지도" aria-label="지도">
                <span class="nav-icon material-symbols-outlined">map</span>
                <span class="visually-hidden">지도</span>
            </a>
        </li>

        <!-- <li class="nav-item">
            <a class="nav-link ${fn:contains(uri, '/event') ? 'active' : ''}"
               href="<c:url value='/service/event.do'/>"
               title="이벤트정보" aria-label="이벤트정보">
                <span class="nav-icon material-symbols-outlined">celebration</span>
                <span class="visually-hidden">이벤트정보</span>
            </a>
        </li>

        <li class="nav-item">
            <a class="nav-link ${fn:contains(uri, '/store') ? 'active' : ''}"
               href="<c:url value='/service/store.do'/>"
               title="가게정보" aria-label="가게정보">
                <span class="nav-icon material-symbols-outlined">storefront</span>
                <span class="visually-hidden">가게정보</span>
            </a>
        </li> -->

    </ul>

    <div class="sidebar-footer | border-top | d-flex">
        <button class="sidebar-toggler" type="button"
            aria-label="관리 페이지로 이동"
            onclick="window.location.href='<c:url value='/event/list.do'/>'"></button>
    </div>
</div>
<!--// Leftnav -->
