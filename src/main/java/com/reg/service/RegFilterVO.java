package com.reg.service;

import java.io.Serializable;

public class RegFilterVO implements Serializable {

	private static final long serialVersionUID = 1L;

	// 필드 선언부
	private Long reg_id; // 행정구역 ID
	private Long parentRegId; // 상위 행정구역 ID
	private String parentCode; // 상위 행정구역
	private String parentName; // 상위 행정구역명
	private String code; // 코드
	private String name; // 행정구역명
	private String haveFilter; // 상세 조건 여부
	private String filter = ""; // 상세조건

	
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

	public String getHaveFilter() {
		return haveFilter;
	}

	public void setHaveFilter(String haveFilter) {
		this.haveFilter = haveFilter;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
}
