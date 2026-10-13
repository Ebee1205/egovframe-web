<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="modal fade" id="regDetailModal" tabindex="-1" aria-labelledby="regDetailTitle" aria-hidden="true"
     data-detail-url="<c:url value='/region/detail.do'/>">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title fs-5" id="regDetailTitle">행정구역 상세</h2>
                <button type="button" class="btn-close" data-coreui-dismiss="modal" aria-label="닫기"></button>
            </div>
            <div class="modal-body">
                <p id="regDetailLoading" class="text-body-secondary d-none" role="status">행정구역 정보를 불러오는 중입니다.</p>
                <dl id="regDetailFields" class="row mb-0 d-none">
                    <dt class="col-4">행정구역 ID</dt><dd class="col-8" id="regDetailId"></dd>
                    <dt class="col-4">상위 행정구역</dt><dd class="col-8" id="regDetailParent"></dd>
                    <dt class="col-4">지역코드</dt><dd class="col-8" id="regDetailCode"></dd>
                    <dt class="col-4">행정구역명</dt><dd class="col-8" id="regDetailName"></dd>
                    <dt class="col-4">레벨</dt><dd class="col-8" id="regDetailLevel"></dd>
                    <dt class="col-4">상태</dt><dd class="col-8" id="regDetailStatus"></dd>
                    <dt class="col-4">등록일</dt><dd class="col-8" id="regDetailCDate"></dd>
                    <dt class="col-4">수정일</dt><dd class="col-8" id="regDetailUDate"></dd>
                </dl>
                <div id="regDetailError" class="text-danger d-none" role="alert" aria-live="polite"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-coreui-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<script>
(function () {
    'use strict';
    var modalElement = document.getElementById('regDetailModal');
    var modal = coreui.Modal.getOrCreateInstance(modalElement);
    var loading = document.getElementById('regDetailLoading');
    var fields = document.getElementById('regDetailFields');
    var error = document.getElementById('regDetailError');
    var requestVersion = 0;
    var activeRequest = null;

    function display(id, value) {
        document.getElementById(id).textContent = value == null || value === '' ? '-' : String(value);
    }
    function formatDate(value) {
        if (!value) { return '-'; }
        var date = new Date(value);
        return isNaN(date.getTime()) ? String(value) : date.toLocaleString('ko-KR');
    }
    function fillDetail(data) {
        display('regDetailId', data.reg_id);
        display('regDetailParent', data.parentRegId == null ? '최상위 행정구역'
            : (data.parentName || '-') + ' (' + (data.parentCode || data.parentRegId) + ')');
        display('regDetailCode', data.code);
        display('regDetailName', data.name);
        display('regDetailLevel', data.level);
        display('regDetailStatus', data.status === 'Y' ? '사용' : '미사용');
        display('regDetailCDate', formatDate(data.cDate));
        display('regDetailUDate', formatDate(data.uDate));
    }
    async function fetchRegDetail(regId) {
        var version = ++requestVersion;
        if (activeRequest) { activeRequest.abort(); }
        var controller = new AbortController();
        activeRequest = controller;
        var timer = setTimeout(function () { controller.abort(); }, 30000);
        loading.classList.remove('d-none');
        fields.classList.add('d-none');
        error.textContent = '';
        error.classList.add('d-none');
        modal.show();
        try {
            var response = await fetch(modalElement.getAttribute('data-detail-url')
                + '?reg_id=' + encodeURIComponent(regId), {signal: controller.signal});
            var result = await response.json();
            if (version !== requestVersion) { return; }
            if (!response.ok || !result.code || result.code.resultCode !== 'SUCCESS' || !result.data) {
                throw new Error(result.code && result.code.message || '행정구역 정보를 불러오지 못했습니다.');
            }
            fillDetail(result.data);
            fields.classList.remove('d-none');
        } catch (exception) {
            if (version !== requestVersion) { return; }
            error.textContent = exception.name === 'AbortError' ? '응답 시간이 초과되었습니다. 다시 시도해주세요.'
                : exception.message || '서버와 통신하지 못했습니다.';
            error.classList.remove('d-none');
        } finally {
            clearTimeout(timer);
            if (version === requestVersion) {
                loading.classList.add('d-none');
                activeRequest = null;
            }
        }
    }
    modalElement.addEventListener('hidden.coreui.modal', function () {
        ++requestVersion;
        if (activeRequest) { activeRequest.abort(); activeRequest = null; }
        fields.classList.add('d-none');
        error.classList.add('d-none');
        loading.classList.add('d-none');
    });
    var table = document.getElementById('regListTable');
    if (table) {
        table.addEventListener('click', function (event) {
            var button = event.target.closest('button[data-reg-action="detail"]');
            if (button && table.contains(button)) { fetchRegDetail(button.getAttribute('data-reg-id')); }
        });
    }
})();
</script>
