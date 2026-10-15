package com.overview.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.event.service.EventFilterVO;
import com.event.service.EventService;
import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OverviewController {

    private final EventService eventService;
    private final CodeService codeService;

    public OverviewController(EventService eventService, CodeService codeService) {
        this.eventService = eventService;
        this.codeService = codeService;
    }

    @RequestMapping("/overview.do")
    public String mapService(Model model) throws Exception {
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
        
        return "forward:/WEB-INF/jsp/overview/Overview.jsp";
    }
}
