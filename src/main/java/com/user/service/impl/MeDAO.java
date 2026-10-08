package com.user.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.user.service.MeVO;

@Repository
public class MeDAO extends EgovAbstractMapper {

	public MeVO selectMe(Long uid) throws Exception {
		return selectOne("meDAO.selectMe", uid);
	}

	public List<MeVO> selectMeList() throws Exception {
		return selectList("meDAO.selectMeList");
	}
}
