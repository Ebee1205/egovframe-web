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
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
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
    	
    	//여기서부터는 그냥 impl에 넣는게 나을듯... 너무 지저분한데
        try (Jsonb jsonb = JsonbBuilder.create()) {
        	
            Map<?, ?> root = jsonb.fromJson(ultraSrtNcstService.fetchUltraSrtNcstJson(), Map.class);
            Map<?, ?> response = (Map<?, ?>) root.get("response");
            Map<?, ?> header = (Map<?, ?>) response.get("header");
            String resultCode = (String) header.get("resultCode");
            String resultMsg = (String) header.get("resultMsg");
            if (resultCode == null || resultMsg == null) {
                throw new IllegalStateException("기상청 API 응답 헤더 누락");
            }
            if (!"00".equals(resultCode)) {
                return ResponseEntity.status(502)
                        .body(ComResponseVO.fail(resultCode, resultMsg).toJson());
            }
            Map<?, ?> body = (Map<?, ?>) response.get("body");
            Map<?, ?> items = (Map<?, ?>) body.get("items");
            return ResponseEntity.ok(ComResponseVO.success(items.get("item"), resultMsg).toJson());
        } catch (Exception ex) {
            LOGGER.warn("날씨 조회 실패 (예외 유형): {}", ex.getClass().getSimpleName());
            return ResponseEntity.status(500).body(ComResponseVO.fail("INTERNAL_SERVER_ERROR",
                    "날씨 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.").toJson());
        }
    }
}
