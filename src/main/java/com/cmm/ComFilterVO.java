package com.cmm;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ComFilterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 필드 선언부
    private String filterCondition = ""; // 검색조건
    private String filterKeyword = ""; // 검색Keyword
    private String filterUseYn = ""; // 검색사용여부
    
    private int totalCnt = 0; // 총 갯수

    private int pageIndex = 1; // 현재페이지
    private int pageNum = 10; // 페이지갯수
    private int pageSize = 10; // 페이지사이즈
    private int pageCnt = 10; // 페이지당 노출 갯수

    private int firstIndex = 1; // 첫번째 인덱스
    private int lastIndex = 1; // 마지막 인덱스


    // Getter / Setter
    public String getFilterCondition() {
        return filterCondition;
    }

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }

    public String getFilterKeyword() {
        return filterKeyword;
    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

    public String getFilterUseYn() {
        return filterUseYn;
    }

    public void setFilterUseYn(String filterUseYn) {
        this.filterUseYn = filterUseYn;
    }

    public int getTotalCnt() {
        return totalCnt;
    }

    public void setTotalCnt(int totalCnt) {
        this.totalCnt = totalCnt;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCnt() {
        return pageCnt;
    }

    public void setPageCnt(int pageCnt) {
        this.pageCnt = pageCnt;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    // 공통
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}