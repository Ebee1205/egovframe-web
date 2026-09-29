package com.code.service;

import java.io.Serializable;
import java.util.Date;

public class CodeDetailVO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// 필드 선언부
	private Long cid; // 코드 ID
	private Long parentCid; // 상위 코드 ID
	private String parentCode; // 상위 코드
	private String parentName; // 상위 코드명
	private String code; // 코드
	private String name; // 코드명
	private Integer level; // 코드 레벨
	private String dsc; // 설명
	private String status; // 상태
	private Date cDate; // 생성일
	private Date uDate; // 수정일


	// Getter / Setter
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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getDsc() {
		return dsc;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
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
