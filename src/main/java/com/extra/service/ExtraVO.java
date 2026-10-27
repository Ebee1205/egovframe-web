package com.extra.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/** tb_extra: 따릉이 대여이력. 번호와 코드는 원본 문자열로 유지한다. */
public class ExtraVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bikeId; // 자전거ID
    private Date rentDt; // 대여일시
    private String rentId; // 대여소번호
    private String rentNm; // 대여소명
    private String rentHold; // 대여 거치대
    private Date rtnDt; // 반납일시
    private String rtnId; // 반납대여소번호
    private String rtnNm; // 반납대여소명
    private String rtnHold; // 반납거치대
    private BigDecimal useMin; // 사용시간 (분)
    private BigDecimal useDst; // 이용거리 (m)
    private String usrClsCd; // 사용자종류
    private String sexCd; // 성별
    private String birthYear; // 생년
    private String rentStationId; // 대여대여소ID
    private String returnStationId; // 반납대여소ID
    private Date cDate; // 등록일시

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public Date getRentDt() {
        return rentDt;
    }

    public void setRentDt(Date rentDt) {
        this.rentDt = rentDt;
    }

    public String getRentId() {
        return rentId;
    }

    public void setRentId(String rentId) {
        this.rentId = rentId;
    }

    public String getRentNm() {
        return rentNm;
    }

    public void setRentNm(String rentNm) {
        this.rentNm = rentNm;
    }

    public String getRentHold() {
        return rentHold;
    }

    public void setRentHold(String rentHold) {
        this.rentHold = rentHold;
    }

    public Date getRtnDt() {
        return rtnDt;
    }

    public void setRtnDt(Date rtnDt) {
        this.rtnDt = rtnDt;
    }

    public String getRtnId() {
        return rtnId;
    }

    public void setRtnId(String rtnId) {
        this.rtnId = rtnId;
    }

    public String getRtnNm() {
        return rtnNm;
    }

    public void setRtnNm(String rtnNm) {
        this.rtnNm = rtnNm;
    }

    public String getRtnHold() {
        return rtnHold;
    }

    public void setRtnHold(String rtnHold) {
        this.rtnHold = rtnHold;
    }

    public BigDecimal getUseMin() {
        return useMin;
    }

    public void setUseMin(BigDecimal useMin) {
        this.useMin = useMin;
    }

    public BigDecimal getUseDst() {
        return useDst;
    }

    public void setUseDst(BigDecimal useDst) {
        this.useDst = useDst;
    }

    public String getUsrClsCd() {
        return usrClsCd;
    }

    public void setUsrClsCd(String usrClsCd) {
        this.usrClsCd = usrClsCd;
    }

    public String getSexCd() {
        return sexCd;
    }

    public void setSexCd(String sexCd) {
        this.sexCd = sexCd;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getRentStationId() {
        return rentStationId;
    }

    public void setRentStationId(String rentStationId) {
        this.rentStationId = rentStationId;
    }

    public String getReturnStationId() {
        return returnStationId;
    }

    public void setReturnStationId(String returnStationId) {
        this.returnStationId = returnStationId;
    }

    public Date getCDate() {
        return cDate;
    }

    public void setCDate(Date cDate) {
        this.cDate = cDate;
    }
}
