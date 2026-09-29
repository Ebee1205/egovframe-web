package com.user.web;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.user.service.UserService;
import com.user.service.UserVO;

@Controller
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping("/userList.do")
	public String userList(Model model) throws Exception {
		model.addAttribute("users", userService.selectUserList(new UserVO()));
		return "forward:/WEB-INF/jsp/user/UserList.jsp";
	}
}