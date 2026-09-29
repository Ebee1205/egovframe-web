<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!-- Leftnav -->
<div class="sidebar | sidebar-fixed | border-end | h-100">
    <ul class="sidebar-nav">
        <li class="nav-title">Compact nav</li>
        <li class="nav-item">
            <a class="nav-link" href="<c:url value='/codeList.do'/>">
                <i class="nav-icon cil-speedometer"></i>공통코드 
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="<c:url value='/userList.do'/>">
                <i class="nav-icon cil-layers"></i> 사용자
            </a>
        </li>
        <!-- <li class="nav-item nav-group show">
            <a class="nav-link nav-group-toggle" href="#">
            <i class="nav-icon cil-puzzle"></i> Items group
            </a>
            <ul class="nav-group-items">
            <li class="nav-item">
                <a class="nav-link" href="#">Items group item</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="#">Items group item</a>
            </li>
            </ul>
        </li> -->
    </ul>
</div>
<!--// Leftnav -->