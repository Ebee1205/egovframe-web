package com.code.service;

import java.util.List;

public interface CodeService {

	CodeDetailVO selectCode(CodeDetailVO codeVO) throws Exception;

	List<CodeDetailVO> selectCodeList(CodeDetailVO codeVO) throws Exception;

	int selectCodeListCnt(CodeDetailVO codeVO) throws Exception;

	List<CodeFilterVO> selectCmmCodeDetail(CodeFilterVO filterVO) throws Exception;

	List<CodeFilterVO> selectCodeRoot() throws Exception;

	void insertCode(CodeDetailVO codeVO) throws Exception;

	void updateCode(CodeDetailVO codeVO) throws Exception;

	void deleteCode(CodeDetailVO codeVO) throws Exception;
	/** 코드 관리 규칙 위반을 공통 응답으로 전달하기 위한 예외이다. */
	class CodeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final int status;
		private final String resultCode;
		private final String field;
		public CodeException(int status, String resultCode, String message, String field) {
			super(message); this.status = status; this.resultCode = resultCode; this.field = field;
		}
		public int getStatus() { return status; }
		public String getResultCode() { return resultCode; }
		public String getField() { return field; }
	}
}
