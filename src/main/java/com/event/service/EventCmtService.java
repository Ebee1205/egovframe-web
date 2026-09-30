package com.event.service;

import java.util.List;

public interface EventCmtService {

    // 댓글 단건 조회
    EventCmtVO selectEventCmt(EventCmtVO eventCmtVO) throws Exception;

    // 이벤트의 최상위 댓글 목록 조회
    List<EventCmtVO> selectEventCmtList(EventCmtFilterVO filterVO) throws Exception;

    // 이벤트의 최상위 댓글 수 조회
    int selectEventCmtListCnt(EventCmtFilterVO filterVO) throws Exception;

    // 특정 댓글의 대댓글 목록 조회
    List<EventCmtVO> selectEventReplyList(EventCmtFilterVO filterVO) throws Exception;

    // 특정 댓글의 대댓글 수 조회
    int selectEventReplyListCnt(EventCmtFilterVO filterVO) throws Exception;

    // 댓글 / 대댓글 등록
    void insertEventCmt(EventCmtVO eventCmtVO) throws Exception;

    // 댓글 / 대댓글 수정
    void updateEventCmt(EventCmtVO eventCmtVO) throws Exception;

    // 댓글 / 대댓글 삭제
    void deleteEventCmt(EventCmtVO eventCmtVO) throws Exception;
}

