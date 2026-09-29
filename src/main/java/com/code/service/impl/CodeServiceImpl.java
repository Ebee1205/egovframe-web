package com.code.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.code.service.CodeService;
import com.code.service.CodeVO;

@Service
public class CodeServiceImpl implements CodeService {

	private final CodeDAO codeDAO;

	public CodeServiceImpl(CodeDAO codeDAO) {
		this.codeDAO = codeDAO;
	}

	@Override
	public CodeVO selectCode(CodeVO codeVO) throws Exception {
		return codeDAO.selectCode(codeVO);
	}

	@Override
	public List<CodeVO> selectCodeList(CodeVO codeVO) throws Exception {
		return codeDAO.selectCodeList(codeVO);
	}

	@Override
	public void insertCode(CodeVO codeVO) throws Exception {
		codeDAO.insertCode(codeVO);
	}

	@Override
	public void updateCode(CodeVO codeVO) throws Exception {
		codeDAO.updateCode(codeVO);
	}

	@Override
	public void deleteCode(CodeVO codeVO) throws Exception {
		codeDAO.deleteCode(codeVO);
	}
}