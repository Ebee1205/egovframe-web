package com.reg.service;

import java.io.Serializable;
import java.util.Date;

import com.cmm.service.ComFilterVO;

public class RegDetailVO extends ComFilterVO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// 필드 선언부
	private Long reg_id; // 행정구역 ID
	private Long parentRegId; // 상위 행정구역 ID
	private String parentCode; // 상위 행정구역
	private String parentName; // 상위 행정구역명
	private String code; // 코드
	private String name; // 행정구역명
	private Integer level; // 행정구역 레벨
	private String geom; // 영역 EWKT (SRID 보존, Polygon/MultiPolygon)
	private String status; // 상태
	private Date cDate; // 생성일
	private Date uDate; // 수정일


	// Getter / Setter
    public Long getReg_id() {
		return reg_id;
	}

	public void setReg_id(Long reg_id) {
		this.reg_id = reg_id;
	}

	public Long getParentRegId() {
		return parentRegId;
	}

	public void setParentRegId(Long parentRegId) {
		this.parentRegId = parentRegId;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getGeom() {
		return geom;
	}

	public void setGeom(String geom) {
		this.geom = geom;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCDate() {
		return cDate;
	}

	public void setCDate(Date cDate) {
		this.cDate = cDate;
	}

	public Date getUDate() {
		return uDate;
	}

	public void setUDate(Date uDate) {
		this.uDate = uDate;
	}
}
