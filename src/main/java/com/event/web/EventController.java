package com.event.web;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.event.service.EventFilterVO;
import com.event.service.EventService;

@Controller
public class EventController {

	private final EventService eventService;
	private final CodeService codeService;

	public EventController(EventService eventService, CodeService codeService) {
		this.eventService = eventService;
		this.codeService = codeService;
	}

	@RequestMapping("/event/list.do")
	public String eventList(EventFilterVO filterVO, Model model) throws Exception {
		model.addAttribute("events", eventService.selectEventList(filterVO));

		CodeFilterVO eventCtgFilter = new CodeFilterVO();
		eventCtgFilter.setParentCode("EVENT_CTG_ROOT");
		model.addAttribute("eventTypes", codeService.selectCmmCodeDetail(eventCtgFilter));

		CodeFilterVO eventStatusFilter = new CodeFilterVO();
		eventStatusFilter.setParentCode("EVENT_STAT_ROOT");
		model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(eventStatusFilter));
		
		CodeFilterVO tagFilter = new CodeFilterVO();
		tagFilter.setParentCode("TAG_ROOT");
		model.addAttribute("tags", codeService.selectCmmCodeDetail(tagFilter));
		
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", filterVO.getTotalCnt() == 0 ? 0
				: (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize());
		return "forward:/WEB-INF/jsp/event/EventList.jsp";
	}

	@RequestMapping("/event/create.do")
	public String eventCreate() {
		return "forward:/WEB-INF/jsp/event/EventCreate.jsp";
	}
}