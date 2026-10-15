<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<section class="overview-events" aria-labelledby="serviceEventHeading">
    <header class="d-flex | align-items-start | justify-content-between | gap-2 | mb-3">
        <div>
            <h2 id="serviceEventHeading" class="h5 | fw-semibold | mb-1">새로 올라온 이벤트</h2>
            <p class="text-body-secondary | small | mb-0">최신 10개</p>
        </div>
    </header>

    <c:choose>
        <c:when test="${empty events}">
            <div class="card | border-0 | bg-body-tertiary | text-center">
                <div class="card-body | py-5">
                    <p class="fw-semibold | mb-2">아직 등록된 이벤트가 없어요</p>
                    <p class="text-body-secondary | small | mb-0">새로운 이벤트가 등록되면 여기에 표시됩니다.</p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="d-flex | flex-column | gap-3">
                <c:forEach var="event" items="${events}" begin="0" end="9">
                    <c:set var="serviceEventCtg" value="${event.ctg}"/>
                    <c:forEach var="eventCtg" items="${eventCtgs}">
                        <c:if test="${event.ctg eq eventCtg.code}">
                            <c:set var="serviceEventCtg" value="${eventCtg.name}"/>
                        </c:if>
                    </c:forEach>
                    <c:set var="serviceEventStatus" value="${event.status}"/>
                    <c:forEach var="eventStatus" items="${eventStatuses}">
                        <c:if test="${event.status eq eventStatus.code}">
                            <c:set var="serviceEventStatus" value="${eventStatus.name}"/>
                        </c:if>
                    </c:forEach>

                    <details class="card | event-card">
                        <summary class="event-summary">
                            <div class="d-flex | flex-wrap | align-items-center | gap-2 | mb-2">
                                <c:if test="${not empty serviceEventCtg}">
                                    <span class="badge | bg-primary-subtle | text-primary-emphasis"><c:out value="${serviceEventCtg}"/></span>
                                </c:if>
                                <c:if test="${not empty serviceEventStatus}">
                                    <span class="badge | bg-secondary-subtle | text-secondary-emphasis"><c:out value="${serviceEventStatus}"/></span>
                                </c:if>
                            </div>
                            <div class="d-flex | align-items-start | justify-content-between | gap-2 | mb-2">
                                <h3 class="h6 | fw-semibold | mb-0 | event-title"><c:out value="${event.title}"/></h3>
                                <svg class="event-chevron | text-body-secondary | mt-1" width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
                                    <path d="m4 6 4 4 4-4"/>
                                </svg>
                            </div>
                            <div class="event-meta | text-body-secondary">
                                <c:choose>
                                    <c:when test="${not empty event.SDate}">
                                        <fmt:formatDate value="${event.SDate}" pattern="yyyy.MM.dd HH:mm"/>
                                        <c:if test="${not empty event.EDate}">
                                            <span aria-label="부터">~</span> <fmt:formatDate value="${event.EDate}" pattern="yyyy.MM.dd HH:mm"/>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>일정 미정</c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${not empty event.regName}">
                                <p class="event-meta | text-body-secondary | mt-1 | mb-0"><c:out value="${event.regName}"/></p>
                            </c:if>
                        </summary>

                        <div class="event-details">
                            <h4 class="h6 | fw-semibold | mb-2">이벤트 소개</h4>
                            <p class="event-description | small | mb-3"><c:out value="${empty event.dsc ? '상세 안내가 아직 등록되지 않았습니다.' : event.dsc}"/></p>
                            <dl class="mb-0">
                                <dt>장소</dt>
                                <dd><c:out value="${empty event.address ? '장소 미정' : event.address}"/></dd>
                                <dt>작성자</dt>
                                <dd><c:out value="${empty event.createdByNickname ? '알 수 없는 사용자' : event.createdByNickname}"/></dd>
                                <dt>등록일</dt>
                                <dd class="mb-0"><fmt:formatDate value="${event.CDate}" pattern="yyyy.MM.dd HH:mm"/></dd>
                            </dl>
                        </div>
                    </details>
                </c:forEach>
                
                
            </div>
        </c:otherwise>
    </c:choose>
</section>
