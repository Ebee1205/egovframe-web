package com.user.web;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.user.service.UserFilterVO;
import com.user.service.UserService;

@Controller
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping("/userList.do")
	public String userList(UserFilterVO filterVO, Model model) throws Exception {
		model.addAttribute("users", userService.selectUserList(filterVO));
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", filterVO.getTotalCnt() == 0 ? 0
				: (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize());
		return "forward:/WEB-INF/jsp/user/UserList.jsp";
	}
}