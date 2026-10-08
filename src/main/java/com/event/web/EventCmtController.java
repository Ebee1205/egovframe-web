package com.event.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.event.service.EventCmtFilterVO;
import com.event.service.EventCmtService;
import com.event.service.EventCmtVO;

@Controller
public class EventCmtController {

    private final EventCmtService eventCmtService;

    public EventCmtController(EventCmtService eventCmtService) {
        this.eventCmtService = eventCmtService;
    }

    /**
     * 이벤트 댓글 목록
     */
    @RequestMapping("/event/cmt/list.do")
    public String eventCmtList(
            @RequestParam("eid") Long eid,
            EventCmtFilterVO filterVO,
            Model model) throws Exception {

        filterVO.setEid(eid);

        model.addAttribute(
            "comments",
            eventCmtService.selectEventCmtList(filterVO)
        );

        model.addAttribute("filterVO", filterVO);

        model.addAttribute(
            "totalPage",
            filterVO.getTotalCnt() == 0
                ? 0
                : (filterVO.getTotalCnt() + filterVO.getPageSize() - 1)
                    / filterVO.getPageSize()
        );

        return "forward:/WEB-INF/jsp/event/EventCmtList.jsp";
    }

    /**
     * 특정 댓글의 대댓글 목록
     */
    @RequestMapping("/event/cmt/reply/list.do")
    public String eventReplyList(
            @RequestParam("eid") Long eid,
            @RequestParam("cmtId") Long cmtId,
            EventCmtFilterVO filterVO,
            Model model) throws Exception {

        filterVO.setEid(eid);
        filterVO.setParentCmtId(cmtId);

        model.addAttribute(
            "replies",
            eventCmtService.selectEventReplyList(filterVO)
        );

        model.addAttribute("filterVO", filterVO);

        return "forward:/WEB-INF/jsp/event/EventReplyList.jsp";
    }

    /**
     * 댓글 / 대댓글 등록
     */
    @RequestMapping("/event/cmt/insert.do")
    public String insertEventCmt(
            EventCmtVO eventCmtVO,
            RedirectAttributes redirectAttributes) throws Exception {

        if (eventCmtVO.getUid() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a current user before adding a comment.");
        }
        eventCmtService.insertEventCmt(eventCmtVO);

        redirectAttributes.addAttribute("eid", eventCmtVO.getEid());

        return "redirect:/event/detail.do";
    }

    /**
     * 댓글 / 대댓글 수정
     */
    @RequestMapping("/event/cmt/update.do")
    public String updateEventCmt(
            EventCmtVO eventCmtVO,
            RedirectAttributes redirectAttributes) throws Exception {

        eventCmtService.updateEventCmt(eventCmtVO);

        redirectAttributes.addAttribute("eid", eventCmtVO.getEid());

        return "redirect:/event/detail.do";
    }

    /**
     * 댓글 / 대댓글 삭제
     */
    @RequestMapping("/event/cmt/delete.do")
    public String deleteEventCmt(
            EventCmtVO eventCmtVO,
            RedirectAttributes redirectAttributes) throws Exception {

        eventCmtService.deleteEventCmt(eventCmtVO);

        redirectAttributes.addAttribute("eid", eventCmtVO.getEid());

        return "redirect:/event/detail.do";
    }
}