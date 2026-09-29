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
     * 공통코드의 루트 코드만 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeDefaultVO> selectCodeRoot(CodeDefaultVO vo) throws Exception {
    	return selectList("CodeUseDAO.selectCodeRoot", vo);
    }

    /**
     * 공통코드로 사용할 사용자정보를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeDefaultVO> selectUserDetail(CodeDefaultVO vo) throws Exception {
    	return selectList("CodeUseDAO.selectUserDetail", vo);
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

    /**
     * 공통코드로 사용할 이벤트정보를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeDefaultVO> selectEventDetail(CodeDefaultVO vo) throws Exception {
    	return selectList("CodeUseDAO.selectEventDetail", vo);
    }

    /**
     * 공통코드로 사용할 이벤트상태를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeDefaultVO> selectEventStatus(CodeDefaultVO vo) throws Exception {
    	return selectList("CodeUseDAO.selectEventStatus", vo);
    }
}
