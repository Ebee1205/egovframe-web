package com.code.service;

import java.util.List;

public interface CodeService {

	CodeVO selectCode(CodeVO codeVO) throws Exception;

	List<CodeVO> selectCodeList(CodeVO codeVO) throws Exception;

	void insertCode(CodeVO codeVO) throws Exception;

	void updateCode(CodeVO codeVO) throws Exception;

	void deleteCode(CodeVO codeVO) throws Exception;
}
