package com.reg.service;

import java.util.List;

public interface RegService {

	RegDetailVO selectReg(RegDetailVO regVO) throws Exception;

	List<RegDetailVO> selectRegList(RegDetailVO regVO) throws Exception;

	int selectRegListCnt(RegDetailVO regVO) throws Exception;

	/** 루트 코드 또는 ID를 기준으로 바로 아래 사용 중인 행정구역을 조회한다. */
	List<RegFilterVO> selectCmmRegDetail(RegFilterVO filterVO) throws Exception;

	List<RegFilterVO> selectRegDetail(RegFilterVO filterVO) throws Exception;

	List<RegFilterVO> selectRegRoot() throws Exception;

	List<RegFilterVO> selectRegRoot(RegFilterVO filterVO) throws Exception;

	List<RegFilterVO> selectRegParents() throws Exception;

	/** 행정구역 관리 규칙 위반을 공통 응답으로 전달하기 위한 예외이다. */
	class RegException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final int status;
		private final String resultCode;
		private final String field;
		public RegException(int status, String resultCode, String message, String field) {
			super(message); this.status = status; this.resultCode = resultCode; this.field = field;
		}
		public int getStatus() { return status; }
		public String getResultCode() { return resultCode; }
		public String getField() { return field; }
	}
}
