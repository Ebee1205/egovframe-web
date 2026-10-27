<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%-- 목록과 업로드 다이얼로그에서 공유하는 화면 확인용 목업 테이블. --%>
<div class="table-responsive">
    <table class="table | table-hover | table-bordered | align-middle | text-nowrap">
        <caption class="visually-hidden">엑셀 등록 테스트 목업 데이터 15건</caption>
        <thead class="table-light | text-center">
            <tr>
                <th scope="col">NO</th>
                <th scope="col">지역</th>
                <th scope="col">제목</th>
                <th scope="col">분류</th>
                <th scope="col">시작일</th>
                <th scope="col">종료일</th>
                <th scope="col">주소</th>
                <th scope="col">좌표1</th>
                <th scope="col">좌표2</th>
                <th scope="col">설명</th>
                <th scope="col">담당자</th>
                <th scope="col">등록일</th>
                <th scope="col">수정일</th>
                <th scope="col">상태</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="row" begin="1" end="15">
                <tr>
                    <td class="text-center">${row}</td>
                    <td>서울특별시 중구</td>
                    <td>목업 이벤트 ${row}</td>
                    <td>문화행사</td>
                    <td>2026-10-26 09:00</td>
                    <td>2026-10-26 18:00</td>
                    <td>서울특별시 중구 세종대로 110</td>
                    <td>126.9780</td>
                    <td>37.5665</td>
                    <td>화면 확인용 샘플 ${row}</td>
                    <td>담당자 ${row}</td>
                    <td>2026-10-20 10:00</td>
                    <td>2026-10-26 10:00</td>
                    <td class="text-center">등록</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
