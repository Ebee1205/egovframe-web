package com.overview.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.event.service.EventVO;
import com.overview.service.OverviewEventFilterVO;
import com.overview.service.OverviewEventService;

@Service
public class OverviewEventServiceImpl implements OverviewEventService {

    private final OverviewEventDAO overviewEventDAO;

    public OverviewEventServiceImpl(OverviewEventDAO overviewEventDAO) {
        this.overviewEventDAO = overviewEventDAO;
    }

    @Override
    public List<EventVO> selectEventList(OverviewEventFilterVO filterVO) throws Exception {
        filterVO.setTotalCnt(overviewEventDAO.selectEventListCnt(filterVO));
        return overviewEventDAO.selectEventList(filterVO);
    }
}
