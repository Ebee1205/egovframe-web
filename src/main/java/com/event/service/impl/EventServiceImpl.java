package com.event.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.event.service.EventFilterVO;
import com.event.service.EventService;
import com.event.service.EventVO;

@Service
public class EventServiceImpl implements EventService {

	private final EventDAO eventDAO;

	public EventServiceImpl(EventDAO eventDAO) {
		this.eventDAO = eventDAO;
	}

	@Override
	public EventVO selectEvent(EventVO eventVO) throws Exception {
		return eventDAO.selectEvent(eventVO);
	}

	@Override
	public List<EventVO> selectEventList(EventFilterVO filterVO) throws Exception {
		if (filterVO.getPageIndex() < 1) {
			filterVO.setPageIndex(1);
		}
		filterVO.setFirstIndex((filterVO.getPageIndex() - 1) * filterVO.getPageSize());
		filterVO.setLastIndex(filterVO.getFirstIndex() + filterVO.getPageSize());
		filterVO.setTotalCnt(selectEventListCnt(filterVO));
		return eventDAO.selectEventList(filterVO);
	}

	@Override
	public int selectEventListCnt(EventFilterVO filterVO) throws Exception {
		return eventDAO.selectEventListCnt(filterVO);
	}

	@Override
	public void insertEvent(EventVO eventVO) throws Exception {
		eventDAO.insertEvent(eventVO);
	}

	@Override
	public void updateEvent(EventVO eventVO) throws Exception {
		eventDAO.updateEvent(eventVO);
	}

	@Override
	public void deleteEvent(EventVO eventVO) throws Exception {
		eventDAO.deleteEvent(eventVO);
	}
}