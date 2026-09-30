package com.event.web;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.code.service.CodeFilterVO;
import com.code.service.CodeService;

import com.event.service.EventVO;
import com.event.service.EventFilterVO;
import com.event.service.EventService;

import com.event.service.EventCmtVO;
import com.event.service.EventCmtFilterVO;
import com.event.service.EventCmtService;


@Controller
public class EventController {

	private final EventService eventService;
	private final EventCmtService eventCmtService;
	private final CodeService codeService;

	public EventController(EventService eventService, EventCmtService eventCmtService, CodeService codeService) {
		this.eventService = eventService;
		this.eventCmtService = eventCmtService;
		this.codeService = codeService;
	}

	@RequestMapping("/event/list.do")
	public String eventList(EventFilterVO filterVO, Model model) throws Exception {
		model.addAttribute("events", eventService.selectEventList(filterVO));

		CodeFilterVO eventCtgFilter = new CodeFilterVO();
		eventCtgFilter.setParentCode("EVENT_CTG_ROOT");
		model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(eventCtgFilter));

		CodeFilterVO eventStatusFilter = new CodeFilterVO();
		eventStatusFilter.setParentCode("EVENT_STAT_ROOT");
		model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(eventStatusFilter));

		CodeFilterVO tagFilter = new CodeFilterVO();
		tagFilter.setParentCode("TAG_ROOT");
		model.addAttribute("tags", codeService.selectCmmCodeDetail(tagFilter));

		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", filterVO.getTotalCnt() == 0 ? 0 : (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize());

		return "forward:/WEB-INF/jsp/event/EventList.jsp";
	}

	@RequestMapping("/event/create.do")
	public String eventCreate(Model model) throws Exception {
		CodeFilterVO eventCtgFilter = new CodeFilterVO();
		eventCtgFilter.setParentCode("EVENT_CTG_ROOT");
		model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(eventCtgFilter));

		CodeFilterVO eventStatusFilter = new CodeFilterVO();
		eventStatusFilter.setParentCode("EVENT_STAT_ROOT");
		model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(eventStatusFilter));

		CodeFilterVO tagFilter = new CodeFilterVO();
		tagFilter.setParentCode("TAG_ROOT");
		model.addAttribute("tags", codeService.selectCmmCodeDetail(tagFilter));

		return "forward:/WEB-INF/jsp/event/EventCreate.jsp";
	}

	@RequestMapping("/event/detail.do")
	public String eventDetail(@RequestParam("eid") Long eid, EventCmtFilterVO cmtFilterVO, Model model) throws Exception {
		EventVO eventVO = new EventVO();
		eventVO.setEid(eid);
		model.addAttribute("event", eventService.selectEvent(eventVO));

		cmtFilterVO.setEid(eid);
		List<EventCmtVO> comments = eventCmtService.selectEventCmtList(cmtFilterVO);
		model.addAttribute("comments", comments);
		model.addAttribute("cmtFilterVO", cmtFilterVO);

		Map<Long, List<EventCmtVO>> replyMap = new HashMap<Long, List<EventCmtVO>>();
		for (EventCmtVO comment : comments) {
			EventCmtFilterVO replyFilterVO = new EventCmtFilterVO();
			replyFilterVO.setEid(eid);
			replyFilterVO.setParentCmtId(comment.getCmtId());
			replyMap.put(comment.getCmtId(), eventCmtService.selectEventReplyList(replyFilterVO));
		}
		model.addAttribute("replyMap", replyMap);

		CodeFilterVO eventCtgFilter = new CodeFilterVO();
		eventCtgFilter.setParentCode("EVENT_CTG_ROOT");
		model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(eventCtgFilter));

		CodeFilterVO eventStatusFilter = new CodeFilterVO();
		eventStatusFilter.setParentCode("EVENT_STAT_ROOT");
		model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(eventStatusFilter));

		CodeFilterVO tagFilter = new CodeFilterVO();
		tagFilter.setParentCode("TAG_ROOT");
		model.addAttribute("tags", codeService.selectCmmCodeDetail(tagFilter));

		return "forward:/WEB-INF/jsp/event/EventDetail.jsp";
	}

}