package com.basic;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.user.service.UserService;
import com.user.service.UserVO;

@Controller
public class HelloWorldController {

    private final UserService userService;

    public HelloWorldController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/helloWorld.do")
    public String helloWorld(Model model) throws Exception {
        model.addAttribute("users", userService.selectUserList(new UserVO()));
        return "forward:/WEB-INF/jsp/helloWorld.jsp";
    }
}
