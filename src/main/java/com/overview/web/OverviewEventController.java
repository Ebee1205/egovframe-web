package com.overview.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.event.service.EventVO;

import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.overview.service.OverviewEventFilterVO;
import com.overview.service.OverviewEventService;

@Controller
public class OverviewEventController {

    private final OverviewEventService overviewEventService;
    private final CodeService codeService;

    public OverviewEventController(OverviewEventService overviewEventService, CodeService codeService) {
        this.overviewEventService = overviewEventService;
        this.codeService = codeService;
    }

    /** Overview event panel: all events within the current map bounds. */
    @RequestMapping(value = "/overview/event/list/recent.do", method = RequestMethod.GET)
    public String eventRecentList(
            @RequestParam("minLat") BigDecimal minLat,
            @RequestParam("minLon") BigDecimal minLon,
            @RequestParam("maxLat") BigDecimal maxLat,
            @RequestParam("maxLon") BigDecimal maxLon,
            Model model) throws Exception {
        if (minLat.compareTo(BigDecimal.valueOf(-90)) < 0
                || maxLat.compareTo(BigDecimal.valueOf(90)) > 0
                || minLon.compareTo(BigDecimal.valueOf(-180)) < 0
                || maxLon.compareTo(BigDecimal.valueOf(180)) > 0
                || minLat.compareTo(maxLat) > 0 || minLon.compareTo(maxLon) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 지도 영역입니다.");
        }
        OverviewEventFilterVO filter = new OverviewEventFilterVO();
        filter.setPageIndex(1);
        filter.setPageSize(0);
        filter.setMinLat(minLat);
        filter.setMinLon(minLon);
        filter.setMaxLat(maxLat);
        filter.setMaxLon(maxLon);
        model.addAttribute("events", overviewEventService.selectEventList(filter));
        model.addAttribute("viewportLoaded", true);

        CodeFilterVO categoryFilter = new CodeFilterVO();
        categoryFilter.setParentCode("EVENT_CTG_ROOT");
        model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(categoryFilter));

        CodeFilterVO statusFilter = new CodeFilterVO();
        statusFilter.setParentCode("EVENT_STAT_ROOT");
        model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(statusFilter));

        return "forward:/WEB-INF/jsp/overview/event/OverviewEventList.jsp";
    }

    // Search all events independently of the map bounds.
    @RequestMapping(value = "/overview/event/list.do", method = RequestMethod.GET)
    public String eventList(@RequestParam(value = "title", defaultValue = "") String title,
            Model model) throws Exception {

        OverviewEventFilterVO filter = new OverviewEventFilterVO();

        filter.setTitle(title.trim());
        filter.setPageIndex(1);
        filter.setPageSize(0);

        List<EventVO> events = overviewEventService.selectEventList(filter);
        model.addAttribute("events", events);
        model.addAttribute("searchTitle", filter.getTitle());
        model.addAttribute("searchTotal", filter.getTotalCnt());

        CodeFilterVO categoryFilter = new CodeFilterVO();
        categoryFilter.setParentCode("EVENT_CTG_ROOT");
        model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(categoryFilter));

        CodeFilterVO statusFilter = new CodeFilterVO();
        statusFilter.setParentCode("EVENT_STAT_ROOT");
        model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(statusFilter));

        return "forward:/WEB-INF/jsp/overview/event/OverviewEventSearchList.jsp";
    }
}
