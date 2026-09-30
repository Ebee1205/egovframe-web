package com.code.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.code.service.CodeService;
import com.code.service.CodeFilterVO;
import com.code.service.CodeDetailVO;

@Service
public class CodeServiceImpl implements CodeService {

	private final CodeDetailDAO codeDAO;
	private final CodeOptionDAO codeOptionDAO;

	public CodeServiceImpl(CodeDetailDAO codeDAO, CodeOptionDAO codeOptionDAO) {
		this.codeDAO = codeDAO;
		this.codeOptionDAO = codeOptionDAO;
	}

	@Override
	public CodeDetailVO selectCode(CodeDetailVO codeVO) throws Exception {
		return codeDAO.selectCode(codeVO);
	}

	@Override
	public List<CodeDetailVO> selectCodeList(CodeDetailVO codeVO) throws Exception {
		if (codeVO.getPageIndex() < 1) {
			codeVO.setPageIndex(1); // 페이지 번호 보정
		}
		codeVO.setFirstIndex((codeVO.getPageIndex() - 1) * codeVO.getPageSize()); // 조회 시작 위치(OFFSET) 계산
		codeVO.setLastIndex(codeVO.getFirstIndex() + codeVO.getPageSize());
		codeVO.setTotalCnt(selectCodeListCnt(codeVO)); // 페이징 처리를 위한 총 건수 조회
		return codeDAO.selectCodeList(codeVO);
	}

	@Override
	public int selectCodeListCnt(CodeDetailVO codeVO) throws Exception {
		return codeDAO.selectCodeListCnt(codeVO);
	}

	@Override
	public List<CodeFilterVO> selectCmmCodeDetail(CodeFilterVO filterVO) throws Exception {
		return codeOptionDAO.selectCmmCodeDetail(filterVO);
	}

	@Override
	public List<CodeFilterVO> selectCodeRoot() throws Exception {
		return codeOptionDAO.selectCodeRoot(new CodeFilterVO());
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