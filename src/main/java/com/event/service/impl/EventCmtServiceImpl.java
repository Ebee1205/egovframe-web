package com.event.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.event.service.EventCmtFilterVO;
import com.event.service.EventCmtService;
import com.event.service.EventCmtVO;

@Service
public class EventCmtServiceImpl implements EventCmtService {

    private final EventCmtDAO eventCmtDAO;

    public EventCmtServiceImpl(EventCmtDAO eventCmtDAO) {
        this.eventCmtDAO = eventCmtDAO;
    }

    @Override
    public EventCmtVO selectEventCmt(EventCmtVO eventCmtVO) throws Exception {
        return eventCmtDAO.selectEventCmt(eventCmtVO);
    }

    @Override
    public List<EventCmtVO> selectEventCmtList(EventCmtFilterVO filterVO) throws Exception {
        if (filterVO.getPageIndex() < 1) {
            filterVO.setPageIndex(1);
        }

        filterVO.setFirstIndex(
            (filterVO.getPageIndex() - 1) * filterVO.getPageSize()
        );

        filterVO.setLastIndex(
            filterVO.getFirstIndex() + filterVO.getPageSize()
        );

        filterVO.setTotalCnt(
            selectEventCmtListCnt(filterVO)
        );

        return eventCmtDAO.selectEventCmtList(filterVO);
    }

    @Override
    public int selectEventCmtListCnt(EventCmtFilterVO filterVO) throws Exception {
        return eventCmtDAO.selectEventCmtListCnt(filterVO);
    }

    @Override
    public List<EventCmtVO> selectEventReplyList(EventCmtFilterVO filterVO) throws Exception {
        if (filterVO.getPageIndex() < 1) {
            filterVO.setPageIndex(1);
        }

        filterVO.setFirstIndex(
            (filterVO.getPageIndex() - 1) * filterVO.getPageSize()
        );

        filterVO.setLastIndex(
            filterVO.getFirstIndex() + filterVO.getPageSize()
        );

        filterVO.setTotalCnt(
            selectEventReplyListCnt(filterVO)
        );

        return eventCmtDAO.selectEventReplyList(filterVO);
    }

    @Override
    public int selectEventReplyListCnt(EventCmtFilterVO filterVO) throws Exception {
        return eventCmtDAO.selectEventReplyListCnt(filterVO);
    }

    @Override
    public void insertEventCmt(EventCmtVO eventCmtVO) throws Exception {
        eventCmtDAO.insertEventCmt(eventCmtVO);
    }

    @Override
    public void updateEventCmt(EventCmtVO eventCmtVO) throws Exception {
        eventCmtDAO.updateEventCmt(eventCmtVO);
    }

    @Override
    public void deleteEventCmt(EventCmtVO eventCmtVO) throws Exception {
        eventCmtDAO.deleteEventCmt(eventCmtVO);
    }
}