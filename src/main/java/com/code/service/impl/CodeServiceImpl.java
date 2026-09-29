package com.code.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.code.service.CodeService;
import com.code.service.CodeDetailVO;

@Service
public class CodeServiceImpl implements CodeService {

	private final CodeDetailDAO codeDAO;

	public CodeServiceImpl(CodeDetailDAO codeDAO) {
		this.codeDAO = codeDAO;
	}

	@Override
	public CodeDetailVO selectCode(CodeDetailVO codeVO) throws Exception {
		return codeDAO.selectCode(codeVO);
	}

	@Override
	public List<CodeDetailVO> selectCodeList(CodeDetailVO codeVO) throws Exception {
		return codeDAO.selectCodeList(codeVO);
	}

	@Override
	public void insertCode(CodeDetailVO codeVO) throws Exception {
		codeDAO.insertCode(codeVO);
	}

	@Override
	public void updateCode(CodeDetailVO codeVO) throws Exception {
		codeDAO.updateCode(codeVO);
	}

	@Override
	public void deleteCode(CodeDetailVO codeVO) throws Exception {
		codeDAO.deleteCode(codeVO);
	}
}