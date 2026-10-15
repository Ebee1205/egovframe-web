package com.event.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

import com.cmm.service.ComResponseVO;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.impl.FileMngUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import com.cmm.service.file.FileVO;
import org.springframework.beans.factory.annotation.Value;
import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.reg.service.RegService;

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
	private final RegService regService;
	private final FileMngService fileMngService;
	private final MessageSource messageSource;
	@Value("${Globals.fileUpload.Extensions:}")
	private String fileExtensions;
	@Value("${Globals.fileUpload.MaxSizePerFile:52428800}")
	private long fileMaxSize;
	@Autowired
	private DefaultBeanValidator beanValidator;
	@Autowired
	private DefaultValidatorFactory validatorFactory;

	public EventController(EventService eventService, EventCmtService eventCmtService, CodeService codeService,
			FileMngService fileMngService, RegService regService, MessageSource messageSource) {
		this.eventService = eventService;
		this.eventCmtService = eventCmtService;
		this.codeService = codeService;
		this.fileMngService = fileMngService;
		this.regService = regService;
		this.messageSource = messageSource;
	}

	@InitBinder({"eventVO", "eventCreateVO"})
	public void initEventBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		if ("eventCreateVO".equals(binder.getObjectName())) {
			binder.setAllowedFields("title", "ctg", "status", "SDate", "EDate", "dsc", "address", "lat", "lon", "createdBy", "fileIds");
		}
	}

	@RequestMapping("/event/list.do")
	public String eventList(EventFilterVO filterVO, Model model) throws Exception {
		model.addAttribute("events", eventService.selectEventList(filterVO));
		model.addAttribute("rootRegs", regService.selectRegRoot());
		model.addAttribute("childRegs", regService.selectRegParents());

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
		model.addAttribute("totalPage", filterVO.getPageSize() <= 0 || filterVO.getTotalCnt() == 0
				? 0 : (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize());

		return "forward:/WEB-INF/jsp/event/EventList.jsp";
	}

	@RequestMapping("/event/detail.do")
	public String eventDetail(@RequestParam("eid") Long eid, EventCmtFilterVO cmtFilterVO, Model model) throws Exception {
		EventVO eventVO = new EventVO();
		eventVO.setEid(eid);
		EventVO event = eventService.selectEvent(eventVO);
		if (event == null) {
			throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다.");
		}
		model.addAttribute("event", event);
		model.addAttribute("eventFiles", event.getAttachedFiles());
		model.addAttribute("fileAccept", fileExtensions.replaceAll("\\.(?=[^.]+)", ",.").replaceFirst("^,", ""));
		model.addAttribute("fileMaxSize", fileMaxSize);
		
		model.addAttribute("rootRegs", regService.selectRegRoot());
		model.addAttribute("childRegs", regService.selectRegParents());

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
			eventVO.setReg_id(1L);
			model.addAttribute("eventCreateVO", eventVO);
		}
		
		model.addAttribute("rootRegs", regService.selectRegRoot());
		model.addAttribute("childRegs", regService.selectRegParents());
		
		CodeFilterVO eventCtgFilter = new CodeFilterVO();
		eventCtgFilter.setParentCode("EVENT_CTG_ROOT");
		model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(eventCtgFilter));

		CodeFilterVO eventStatusFilter = new CodeFilterVO();
		eventStatusFilter.setParentCode("EVENT_STAT_ROOT");
		model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(eventStatusFilter));

		CodeFilterVO tagFilter = new CodeFilterVO();
		tagFilter.setParentCode("TAG_ROOT");
		model.addAttribute("tags", codeService.selectCmmCodeDetail(tagFilter));
		EventVO formEvent = (EventVO) model.asMap().get("eventCreateVO");
		List<FileVO> eventFiles = new java.util.ArrayList<FileVO>();
		for (Long fileId : formEvent.getFileIds()) {
			if (fileId != null) {
				FileVO query = new FileVO();
				query.setFileId(fileId);
				FileVO file = fileMngService.selectFileInf(query);
				if (file != null) {
					eventFiles.add(file);
				}
			}
		}
		model.addAttribute("eventFiles", eventFiles);
		model.addAttribute("fileAccept", fileExtensions.replaceAll("\\.(?=[^.]+)", ",.").replaceFirst("^,", ""));
		model.addAttribute("fileMaxSize", fileMaxSize);

		return "forward:/WEB-INF/jsp/event/EventCreate.jsp";
	}

	@RequestMapping(value = "/event/insert.do", method = RequestMethod.POST)
	public String eventInsert(@ModelAttribute("eventCreateVO") EventVO eventVO,
			BindingResult bindingResult, Model model) throws Exception {
		eventVO.setReg_id(1L);
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
