package com.reg.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.reg.service.RegDetailVO;

@Repository
public class RegDetailDAO extends EgovAbstractMapper {

    public boolean isValidGeometry(RegDetailVO vo) {
        return Boolean.TRUE.equals(selectOne("regDAO.isValidGeometry", vo));
    }

	public RegDetailVO selectRegForUpdate(RegDetailVO regVO) {
		return selectOne("regDAO.selectRegForUpdate", regVO);
	}

	public int selectRegChildCount(RegDetailVO regVO) {
		return selectOne("regDAO.selectRegChildCount", regVO);
	}

	public int selectDuplicateRegCount(RegDetailVO regVO) {
		return selectOne("regDAO.selectDuplicateRegCount", regVO);
	}
	public RegDetailVO selectReg(RegDetailVO regVO) throws Exception {
		return selectOne("regDAO.selectReg", regVO);
	}

	public List<RegDetailVO> selectRegList(RegDetailVO regVO) throws Exception {
		return selectList("regDAO.selectRegList", regVO);
	}

	public int selectRegListCnt(RegDetailVO regVO) throws Exception {
		return selectOne("regDAO.selectRegListCnt", regVO);
	}

	public void insertReg(RegDetailVO regVO) throws Exception {
		insert("regDAO.insertReg", regVO);
	}

	public void updateReg(RegDetailVO regVO) throws Exception {
		update("regDAO.updateReg", regVO);
	}

	public void deleteReg(RegDetailVO regVO) throws Exception {
		delete("regDAO.deleteReg", regVO);
	}
}