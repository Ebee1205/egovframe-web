package com.event.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.event.service.EventCmtFilterVO;
import com.event.service.EventCmtVO;

@Repository
public class EventCmtDAO extends EgovAbstractMapper {

    public EventCmtVO selectEventCmt(EventCmtVO eventCmtVO) throws Exception {
        return selectOne("eventCmtDAO.selectEventCmt", eventCmtVO);
    }

    public List<EventCmtVO> selectEventCmtList(EventCmtFilterVO filterVO) throws Exception {
        return selectList("eventCmtDAO.selectEventCmtList", filterVO);
    }

    public int selectEventCmtListCnt(EventCmtFilterVO filterVO) throws Exception {
        return selectOne("eventCmtDAO.selectEventCmtListCnt", filterVO);
    }

    public List<EventCmtVO> selectEventReplyList(EventCmtFilterVO filterVO) throws Exception {
        return selectList("eventCmtDAO.selectEventReplyList", filterVO);
    }

    public int selectEventReplyListCnt(EventCmtFilterVO filterVO) throws Exception {
        return selectOne("eventCmtDAO.selectEventReplyListCnt", filterVO);
    }

    public void insertEventCmt(EventCmtVO eventCmtVO) throws Exception {
        insert("eventCmtDAO.insertEventCmt", eventCmtVO);
    }

    public void updateEventCmt(EventCmtVO eventCmtVO) throws Exception {
        update("eventCmtDAO.updateEventCmt", eventCmtVO);
    }

    public void deleteEventCmt(EventCmtVO eventCmtVO) throws Exception {
        delete("eventCmtDAO.deleteEventCmt", eventCmtVO);
    }

    public void clearEventCmtParentsByEid(Long eid) throws Exception {
        update("eventCmtDAO.clearEventCmtParentsByEid", eid);
    }

    public void deleteEventCmtsByEid(Long eid) throws Exception {
        delete("eventCmtDAO.deleteEventCmtsByEid", eid);
    }
}