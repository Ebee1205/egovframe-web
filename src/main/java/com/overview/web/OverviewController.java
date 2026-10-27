package com.overview.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.event.service.EventService;
import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.overview.service.UltraSrtNcstService;
import com.cmm.service.ComResponseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OverviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverviewController.class);

    private final EventService eventService;
    private final CodeService codeService;
    private final UltraSrtNcstService ultraSrtNcstService;

    public OverviewController(EventService eventService, CodeService codeService,
            UltraSrtNcstService ultraSrtNcstService) {
        this.eventService = eventService;
        this.codeService = codeService;
        this.ultraSrtNcstService = ultraSrtNcstService;
    }

    @RequestMapping("/overview.do")
    public String mapService(Model model) throws Exception {
        // The browser supplies its stored map bounds after the map initializes.

        CodeFilterVO categoryFilter = new CodeFilterVO();
        categoryFilter.setParentCode("EVENT_CTG_ROOT");
        model.addAttribute("eventCtgs", codeService.selectCmmCodeDetail(categoryFilter));

        CodeFilterVO statusFilter = new CodeFilterVO();
        statusFilter.setParentCode("EVENT_STAT_ROOT");
        model.addAttribute("eventStatuses", codeService.selectCmmCodeDetail(statusFilter));

        return "forward:/WEB-INF/jsp/overview/Overview.jsp";
    }

    @RequestMapping(value = "/overview/weather.do", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> mapWeatherService() {
        try {
            ComResponseVO<Map<String, Object>> result = ultraSrtNcstService.fetchWeatherResponse();
            
            int status = "SUCCESS".equals(result.getCode().getResultCode()) ? 200 : 502;
            return ResponseEntity.status(status).body(result.toJson());
            
        } catch (Exception ex) {
            LOGGER.warn("날씨 조회 실패 (예외 유형): {}", ex.getClass().getSimpleName());
            return ResponseEntity.status(500).body(ComResponseVO.fail("INTERNAL_SERVER_ERROR", "날씨 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.").toJson());
        }
    }

}
