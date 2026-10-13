package com.reg.service.impl;

import java.util.List;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;
import com.reg.service.RegFilterVO;

@Repository("regOptionDAO")
public class RegOptionDAO extends EgovAbstractMapper {
    /** 선택한 사용 중인 루트(parentCode 또는 parentRegId)의 바로 아래 행정구역을 조회한다. */
    public List<RegFilterVO> selectCmmRegDetail(RegFilterVO vo) throws Exception {
        return selectList("RegOptionDAO.selectCmmRegDetail", vo);
    }

    /** 기존 호출부와의 호환을 위한 하위 행정구역 조회 메서드. */
    public List<RegFilterVO> selectRegDetail(RegFilterVO vo) throws Exception {
        return selectCmmRegDetail(vo);
    }

    /** 사용 중인 최상위 행정구역(p_reg_id IS NULL)만 조회한다. */
    public List<RegFilterVO> selectRegRoot(RegFilterVO vo) throws Exception {
        return selectList("RegOptionDAO.selectRegRoot", vo);
    }

    /** 다단계 행정구역 등록 시 상위 항목으로 사용할 전체 사용 중인 행정구역을 조회한다. */
    public List<RegFilterVO> selectRegParents(RegFilterVO vo) throws Exception {
        return selectList("RegOptionDAO.selectRegParents", vo);
    }
}
