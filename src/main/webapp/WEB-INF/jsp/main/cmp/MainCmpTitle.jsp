<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!-- Title -->
<div>
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb | mb-2">
            <c:forEach var="crumb" items="${fn:split(path, '/')}" varStatus="status">
                <li class="breadcrumb-item${status.last ? ' active' : ''}"${status.last ? ' aria-current="page"' : ''}>
                    <c:out value="${crumb}"/>
                </li>
            </c:forEach>
        </ol>
    </nav>
    <h3 class="h3 | mb-1"><c:out value="${title}"/></h3>
    <p class="text-body-secondary | mb-0"><c:out value="${dcs}"/></p>
</div>
<!--// Title -->