package com.code.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.code.service.CodeFilterVO;

@Repository("codeOptionDAO")
public class CodeOptionDAO extends EgovAbstractMapper {

    /**
     * 주어진 조건에 따른 공통코드를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
	public List<CodeFilterVO> selectCmmCodeDetail(CodeFilterVO vo) throws Exception {
		return selectList("CodeOptionDAO.selectCmmCodeDetail", vo);
    }

    /**
     * 공통코드의 루트 코드만 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectCodeRoot(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectCodeRoot", vo);
    }

    /**
     * 공통코드로 사용할 사용자정보를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectUserDetail(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectUserDetail", vo);
    }

    /**
     * 공통코드로 사용할 가게정보를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectStoreDetail(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectStoreDetail", vo);
    }

    /**
     * 공통코드로 사용할 이벤트정보를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectEventDetail(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectEventDetail", vo);
    }

    /**
     * 공통코드로 사용할 이벤트상태를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectEventStatus(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectEventStatus", vo);
    }
}
