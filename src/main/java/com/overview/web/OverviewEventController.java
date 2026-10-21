package com.overview.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import com.event.service.EventVO;

import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.event.service.EventFilterVO;
import com.event.service.EventService;

@Controller
public class OverviewEventController {

    private final EventService eventService;
    private final CodeService codeService;

    public OverviewEventController(EventService eventService, CodeService codeService) {
        this.eventService = eventService;
        this.codeService = codeService;
    }

    /** Overview event panel: show the ten most recently registered events. */
    @RequestMapping(value = "/overview/event/list/recent.do", method = RequestMethod.GET)
    public String eventRecentList(Model model) throws Exception {
        EventFilterVO filter = new EventFilterVO();
        filter.setPageIndex(1);
        filter.setPageSize(10);
        model.addAttribute("events", eventService.selectEventList(filter));

        CodeFilterVO categoryFilter = new CodeFilterVO();
        categoryFilter.setParentCode("EVENT_CTG_ROOT");
        model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(categoryFilter));

        CodeFilterVO statusFilter = new CodeFilterVO();
        statusFilter.setParentCode("EVENT_STAT_ROOT");
        model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(statusFilter));

        return "forward:/WEB-INF/jsp/overview/event/OverviewEventList.jsp";
    }
    
    // event 검색 + 무한스크롤 지원,..
    @RequestMapping(value = "/overview/event/list.do", method = RequestMethod.GET)
    public String eventList(@RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(value = "pageIndex", defaultValue = "1") int pageIndex,
            Model model) throws Exception {
        EventFilterVO filter = new EventFilterVO();
        filter.setTitle(title.trim());
        filter.setPageIndex(Math.max(1, Math.min(pageIndex, 1000000)));
        filter.setPageSize(10);
        List<EventVO> events = eventService.selectEventList(filter);
        model.addAttribute("events", events);
        model.addAttribute("searchTitle", filter.getTitle());
        model.addAttribute("searchPage", filter.getPageIndex());
        model.addAttribute("searchTotal", filter.getTotalCnt());
        model.addAttribute("searchHasNext", !events.isEmpty()
                && (long) filter.getFirstIndex() + events.size() < filter.getTotalCnt());

        CodeFilterVO categoryFilter = new CodeFilterVO();
        categoryFilter.setParentCode("EVENT_CTG_ROOT");
        model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(categoryFilter));

        CodeFilterVO statusFilter = new CodeFilterVO();
        statusFilter.setParentCode("EVENT_STAT_ROOT");
        model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(statusFilter));

        return "forward:/WEB-INF/jsp/overview/event/OverviewEventSearchList.jsp";
    }
}
