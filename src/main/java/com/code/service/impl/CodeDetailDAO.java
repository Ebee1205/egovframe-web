package com.code.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.code.service.CodeDetailVO;

@Repository
public class CodeDetailDAO extends EgovAbstractMapper {

	public CodeDetailVO selectCode(CodeDetailVO codeVO) throws Exception {
		return selectOne("codeDAO.selectCode", codeVO);
	}

	public List<CodeDetailVO> selectCodeList(CodeDetailVO codeVO) throws Exception {
		return selectList("codeDAO.selectCodeList", codeVO);
	}

	public int selectCodeListCnt(CodeDetailVO codeVO) throws Exception {
		return selectOne("codeDAO.selectCodeListCnt", codeVO);
	}

	public void insertCode(CodeDetailVO codeVO) throws Exception {
		insert("codeDAO.insertCode", codeVO);
	}

	public void updateCode(CodeDetailVO codeVO) throws Exception {
		update("codeDAO.updateCode", codeVO);
	}

	public void deleteCode(CodeDetailVO codeVO) throws Exception {
		delete("codeDAO.deleteCode", codeVO);
	}
}