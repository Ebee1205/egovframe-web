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
     * 공통코드로 사용할 사용자유형을 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectUsertype(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectUsertype", vo);
    }

    /**
     * 공통코드로 사용할 사용자상태를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectUserStatus(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectUserStatus", vo);
    }

    /**
     * 공통코드로 사용할 가게 카테고리를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectStoreCtg(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectStoreCtg", vo);
    }

    /**
     * 공통코드로 사용할 이벤트 카테고리를 불러온다.
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public List<CodeFilterVO> selectEventCtg(CodeFilterVO vo) throws Exception {
    	return selectList("CodeOptionDAO.selectEventCtg", vo);
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
