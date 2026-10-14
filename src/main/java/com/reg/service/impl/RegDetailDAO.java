package com.reg.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.reg.service.RegDetailVO;

@Repository
public class RegDetailDAO extends EgovAbstractMapper {

	public RegDetailVO selectReg(RegDetailVO regVO) throws Exception {
		return selectOne("regDAO.selectReg", regVO);
	}

	public List<RegDetailVO> selectRegList(RegDetailVO regVO) throws Exception {
		return selectList("regDAO.selectRegList", regVO);
	}

	public int selectRegListCnt(RegDetailVO regVO) throws Exception {
		return selectOne("regDAO.selectRegListCnt", regVO);
	}
}