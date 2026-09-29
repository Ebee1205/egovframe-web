package com.code.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.code.service.CodeDefaultVO;

@Repository("codeUseDAO")
public class CodeUseDAO extends EgovAbstractMapper {

    /**
     * 주어진 조건에 따른 공통코드를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
	public List<CodeDefaultVO> selectCmmCodeDetail(CodeDefaultVO vo) throws Exception {
		return selectList("CodeUseDAO.selectCmmCodeDetail", vo);
    }

    /**
     * 공통코드로 사용할 가게정보를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeDefaultVO> selectStoreDetail(CodeDefaultVO vo) throws Exception {
    	return selectList("CodeUseDAO.selectStoreDetail", vo);
    }
}
