<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<section class="p-2 | overview-events" aria-labelledby="serviceEventHeading">
    <header class="d-flex | p-2 | align-items-start | justify-content-between | gap-2 | mb-4">
        <div>
            <h2 id="serviceEventHeading" class="h5 | fw-semibold | mb-1">새로 올라온 이벤트</h2>
            <p class="text-body-secondary | small | mb-0">최신 10개</p>
        </div>
    </header>

    <c:choose>
    
        <c:when test="${empty events}">
            <div class="card | border-0 | bg-transparent | text-center">
                <div class="card-body | py-5">
                    <p class="fw-semibold | mb-2">아직 등록된 이벤트가 없어요</p>
                    <p class="text-body-secondary | small | mb-0">새로운 이벤트가 등록되면 여기에 표시됩니다.</p>
                </div>
            </div>
        </c:when>
        
        <c:otherwise>
            <div class="d-flex | flex-column | gap-2">
            
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

                    <div class="card | event-card"
                         role="button" tabindex="-1" aria-pressed="false" aria-disabled="true"
                         data-overview-event="<c:out value='${event.eid}'/>"
                         data-lat="<c:out value='${event.lat}'/>"
                         data-lon="<c:out value='${event.lon}'/>">
                            <div class="d-flex | flex-wrap | align-items-center | gap-2 | mb-2">
                                <c:if test="${not empty serviceEventCtg}">
                                    <span class="badge | bg-primary-subtle | text-primary-emphasis"><c:out value="${serviceEventCtg}"/></span>
                                </c:if>
                                <c:if test="${not empty serviceEventStatus}">
                                    <span class="badge | bg-secondary-subtle | text-secondary-emphasis"><c:out value="${serviceEventStatus}"/></span>
                                </c:if>
                            </div>
                            
                            
                            <h3 class="h6 | fw-semibold | mb-0 | event-title"><c:out value="${event.title}"/></h3>
                            
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
                             <c:if test="${not empty event.regName or not empty event.address}">
                                <p class="event-meta | text-body-secondary | mb-1">
                                    <c:out value="${event.regName}"/>
                                    <c:if test="${not empty event.regName and not empty event.address}">
                                        <span class="mx-1" aria-hidden="true">·</span>
                                    </c:if>
                                    <c:out value="${event.address}"/>
                                </p>
                            </c:if>
                            
                            <p class="event-description | small | mb-1"><c:out value="${empty event.dsc ? '상세 안내가 아직 등록되지 않았습니다.' : event.dsc}"/></p>
                            
                            <dl class="event-meta | event-credit | text-body-secondary | mb-0">
                                <div class="event-credit-row">
                                    <dt>작성자<span aria-hidden="true">|</span></dt>
                                    <dd><c:out value="${empty event.createdByNickname ? '알 수 없는 사용자' : event.createdByNickname}"/></dd>
                                </div>
                                <div class="event-credit-row">
                                    <dt>등록일 <span aria-hidden="true">|</span></dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${not empty event.CDate}"><fmt:formatDate value="${event.CDate}" pattern="yyyy.MM.dd HH:mm"/></c:when>
                                            <c:otherwise>등록일 미정</c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                            </dl>
                            
                        </div>
                </c:forEach>
                
                
            </div>
        </c:otherwise>
    </c:choose>
</section>
