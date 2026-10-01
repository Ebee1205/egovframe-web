package com.event.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springmodules.validation.commons.DefaultBeanValidator;
import org.springmodules.validation.commons.DefaultValidatorFactory;

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
	@Autowired
	private DefaultBeanValidator beanValidator;
	@Autowired
	private DefaultValidatorFactory validatorFactory;

	public EventController(EventService eventService, EventCmtService eventCmtService, CodeService codeService) {
		this.eventService = eventService;
		this.eventCmtService = eventCmtService;
		this.codeService = codeService;
	}

	@InitBinder({"eventVO", "eventCreateVO"})
	public void initEventBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		if ("eventCreateVO".equals(binder.getObjectName())) {
			binder.setAllowedFields("title", "ctg", "status", "SDate", "EDate", "dsc", "address", "createdBy");
		}
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

	@RequestMapping("/event/create.do")
	public String eventCreate(Model model) throws Exception {
		if (!model.containsAttribute("eventCreateVO")) {
			EventVO eventVO = new EventVO();
			eventVO.setRid(1L);
			model.addAttribute("eventCreateVO", eventVO);
		}
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

	@RequestMapping(value = "/event/insert.do", method = RequestMethod.POST)
	public String eventInsert(@ModelAttribute("eventCreateVO") EventVO eventVO,
			BindingResult bindingResult, Model model) throws Exception {
		eventVO.setRid(1L);
		validatorFactory.getValidator("eventCreateVO", eventVO, bindingResult).validate();
		if (bindingResult.hasErrors()) {
			return eventCreate(model);
		}
		eventService.insertEvent(eventVO);
		return "redirect:/event/detail.do?eid=" + eventVO.getEid();
	}

	@RequestMapping(value = "/event/update.do", method = RequestMethod.POST)
	public String eventUpdate(@ModelAttribute("eventVO") EventVO eventVO, BindingResult bindingResult) throws Exception {
		beanValidator.validate(eventVO, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		}

		eventService.updateEvent(eventVO);
		return "redirect:/event/detail.do?eid=" + eventVO.getEid();
	}

	@RequestMapping(value = "/event/delete.do", method = RequestMethod.POST)
	public String eventDelete(@RequestParam("eid") Long eid) throws Exception {
		EventVO eventVO = new EventVO();
		eventVO.setEid(eid);
		eventService.deleteEvent(eventVO);
		return "redirect:/event/list.do";
	}

}
