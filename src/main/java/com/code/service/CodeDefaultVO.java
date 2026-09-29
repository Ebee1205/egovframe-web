package com.code.service;

import java.io.Serializable;

public class CodeDefaultVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long cid; // 코드 ID
	private Long parentCid; // 상위 코드 ID
	private String parentCode; // 상위 코드
	private String parentName; // 상위 코드명
	private String code; // 코드
	private String name; // 코드명
	private String haveFilter; // 상세 조건 여부
	private String filter = ""; // 상세조건


    public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public Long getParentCid() {
		return parentCid;
	}

	public void setParentCid(Long parentCid) {
		this.parentCid = parentCid;
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
