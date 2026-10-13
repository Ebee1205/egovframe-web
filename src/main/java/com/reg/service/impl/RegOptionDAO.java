package com.reg.service.impl;

import java.util.List;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;
import com.reg.service.RegFilterVO;

@Repository("regOptionDAO")
public class RegOptionDAO extends EgovAbstractMapper {
    public List<RegFilterVO> selectRegDetail(RegFilterVO vo) throws Exception {
        return selectList("RegOptionDAO.selectRegDetail", vo);
    }
    public List<RegFilterVO> selectRegRoot(RegFilterVO vo) throws Exception {
        return selectList("RegOptionDAO.selectRegRoot", vo);
    }
    public List<RegFilterVO> selectRegParents(RegFilterVO vo) throws Exception {
        return selectList("RegOptionDAO.selectRegParents", vo);
    }
}
