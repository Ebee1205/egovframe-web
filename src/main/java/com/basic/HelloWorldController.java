package com.basic;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloWorldController {

    @RequestMapping("/helloWorld.do")
    public String helloWorld() {
        return "forward:/WEB-INF/jsp/helloWorld.jsp";
    }
}
