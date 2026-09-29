package com.code.service;

import java.io.Serializable;
import java.util.Date;

public class CodeVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long cid;
	private Long parentCid;
	private String parentCode;
	private String parentName;
	private String code;
	private String name;
	private Integer level;
	private String dsc;
	private String status;
	private Date cDate;
	private Date uDate;

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
