package com.event.service;

import java.util.List;

public interface EventService {

	EventVO selectEvent(EventVO eventVO) throws Exception;

	List<EventVO> selectEventList(EventFilterVO filterVO) throws Exception;

	int selectEventListCnt(EventFilterVO filterVO) throws Exception;

	void insertEvent(EventVO eventVO) throws Exception;

	void updateEvent(EventVO eventVO) throws Exception;

	void deleteEvent(EventVO eventVO) throws Exception;
}