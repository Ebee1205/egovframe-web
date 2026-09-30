package com.event.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.event.service.EventFilterVO;
import com.event.service.EventVO;

@Repository
public class EventDAO extends EgovAbstractMapper {

	public EventVO selectEvent(EventVO eventVO) throws Exception {
		return selectOne("eventDAO.selectEvent", eventVO);
	}

	public List<EventVO> selectEventList(EventFilterVO filterVO) throws Exception {
		return selectList("eventDAO.selectEventList", filterVO);
	}

	public int selectEventListCnt(EventFilterVO filterVO) throws Exception {
		return selectOne("eventDAO.selectEventListCnt", filterVO);
	}

	public void insertEvent(EventVO eventVO) throws Exception {
		insert("eventDAO.insertEvent", eventVO);
	}

	public void updateEvent(EventVO eventVO) throws Exception {
		update("eventDAO.updateEvent", eventVO);
	}

	public void deleteEvent(EventVO eventVO) throws Exception {
		delete("eventDAO.deleteEvent", eventVO);
	}
}