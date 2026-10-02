package com.map.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MapViewController {

    @RequestMapping("/content/map.do")
    public String mapContent() {
        return "forward:/WEB-INF/jsp/map/MapView.jsp";
    }


    @RequestMapping("/service/map.do")
    public String mapService() {
        return "forward:/WEB-INF/jsp/main/MainServiceView.jsp";
    }
}
