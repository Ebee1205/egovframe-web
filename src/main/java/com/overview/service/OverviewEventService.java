package com.overview.service;

import java.util.List;
import com.event.service.EventVO;

public interface OverviewEventService {

    List<EventVO> selectEventList(OverviewEventFilterVO filterVO) throws Exception;
}
