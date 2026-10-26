package com.overview.service.impl;

import java.util.List;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;
import com.event.service.EventVO;
import com.overview.service.OverviewEventFilterVO;

@Repository
public class OverviewEventDAO extends EgovAbstractMapper {

    public List<EventVO> selectEventList(OverviewEventFilterVO filterVO) throws Exception {
        return selectList("overviewEventDAO.selectEventList", filterVO);
    }

    public int selectEventListCnt(OverviewEventFilterVO filterVO) throws Exception {
        return selectOne("overviewEventDAO.selectEventListCnt", filterVO);
    }
}
