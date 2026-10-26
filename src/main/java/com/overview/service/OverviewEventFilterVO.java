package com.overview.service;

import java.math.BigDecimal;
import com.event.service.EventFilterVO;

/** 지도 이벤트 조회에 사용하는 영역 조건. */
public class OverviewEventFilterVO extends EventFilterVO {

    private static final long serialVersionUID = 1L;

    private BigDecimal minLat;
    private BigDecimal minLon;
    private BigDecimal maxLat;
    private BigDecimal maxLon;

    public BigDecimal getMinLat() {
        return minLat;
    }

    public void setMinLat(BigDecimal minLat) {
        this.minLat = minLat;
    }

    public BigDecimal getMinLon() {
        return minLon;
    }

    public void setMinLon(BigDecimal minLon) {
        this.minLon = minLon;
    }

    public BigDecimal getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(BigDecimal maxLat) {
        this.maxLat = maxLat;
    }

    public BigDecimal getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(BigDecimal maxLon) {
        this.maxLon = maxLon;
    }
}
