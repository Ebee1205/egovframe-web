package com.overview.service;

import java.io.Serializable;

public class UltraSrtNcstVO implements Serializable {

    private static final long serialVersionUID = 1L;
 

    private String baseDate; // 발표일자 (YYYYMMDD)
    private String baseTime; // 발표시각 (HHmm)
    private Integer nx; // 예보지점 X 격자 좌표
    private Integer ny; // 예보지점 Y 격자 좌표
    private String category; // 자료구분코드
    private String obsrValue; // 실황값 (API 원문)

    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public Integer getNx() {
        return nx;
    }

    public void setNx(Integer nx) {
        this.nx = nx;
    }

    public Integer getNy() {
        return ny;
    }

    public void setNy(Integer ny) {
        this.ny = ny;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getObsrValue() {
        return obsrValue;
    }

    public void setObsrValue(String obsrValue) {
        this.obsrValue = obsrValue;
    }

}
