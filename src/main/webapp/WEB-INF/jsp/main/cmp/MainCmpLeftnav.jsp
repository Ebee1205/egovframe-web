<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />

<!-- Leftnav -->
<div class="sidebar | app-sidebar | sidebar-fixed | border-end | h-100"  id="sidebar">
    <!-- <div class="sidebar-brand | d-flex | justify-content-center | align-items-center">
        <img src="<c:url value='/resources/images/logo.png'/>"
            height="30"
            alt="Logo"
            class="sidebar-logo">
    </div> -->


    <ul id="sidebar-menu" class="sidebar-nav"
        data-context-path="<c:out value='${pageContext.request.contextPath}'/>"></ul>

    <script src="<c:url value='/resources/js/selectMenu.js'/>"></script>

    <div class="sidebar-footer | border-top | d-flex">
        <button class="sidebar-toggler" type="button"
            aria-label="지도 서비스로 이동"
            onclick="window.location.href='<c:url value='/service/map.do'/>'"></button>
    </div>
</div>
<!--// Leftnav -->
