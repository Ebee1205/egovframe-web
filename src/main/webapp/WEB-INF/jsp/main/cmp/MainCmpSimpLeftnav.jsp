<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!-- Leftnav -->
<div class="sidebar | sidebar-fixed | border-end | h-100">
    <ul id="sidebar-menu" class="sidebar-nav"
        data-context-path="<c:out value='${pageContext.request.contextPath}'/>"></ul>

    <script src="<c:url value='/resources/js/selectMenu.js'/>"></script>

    <div class="sidebar-footer | border-top | d-flex">
        <p><small>v0.0.1</small></p>
    </div>
</div>
<!--// Leftnav -->