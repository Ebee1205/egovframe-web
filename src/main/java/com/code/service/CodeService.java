package com.code.service;

import java.util.List;

public interface CodeService {

	CodeDetailVO selectCode(CodeDetailVO codeVO) throws Exception;

	List<CodeDetailVO> selectCodeList(CodeDetailVO codeVO) throws Exception;

	void insertCode(CodeDetailVO codeVO) throws Exception;

	void updateCode(CodeDetailVO codeVO) throws Exception;

	void deleteCode(CodeDetailVO codeVO) throws Exception;
}
