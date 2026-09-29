package com.code.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.code.service.CodeVO;

@Repository
public class CodeDAO extends EgovAbstractMapper {

	public CodeVO selectCode(CodeVO codeVO) throws Exception {
		return selectOne("codeDAO.selectCode", codeVO);
	}

	public List<CodeVO> selectCodeList(CodeVO codeVO) throws Exception {
		return selectList("codeDAO.selectCodeList", codeVO);
	}

	public void insertCode(CodeVO codeVO) throws Exception {
		insert("codeDAO.insertCode", codeVO);
	}

	public void updateCode(CodeVO codeVO) throws Exception {
		update("codeDAO.updateCode", codeVO);
	}

	public void deleteCode(CodeVO codeVO) throws Exception {
		delete("codeDAO.deleteCode", codeVO);
	}
}