package com.extra.service;

import com.cmm.service.ComFilterVO;

public class ExtraFilterVO extends ComFilterVO {
    private static final long serialVersionUID = 1L;

    private String bikeId;

    private String rentId;

    private String rtnId;

    public String getBikeId() { return bikeId; }
    public void setBikeId(String bikeId) { this.bikeId = bikeId; }

    public String getRentId() { return rentId; }
    public void setRentId(String rentId) { this.rentId = rentId; }

    public String getRtnId() { return rtnId; }
    public void setRtnId(String rtnId) { this.rtnId = rtnId; }
}
