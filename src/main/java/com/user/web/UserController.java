package com.user.web;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.code.service.CodeFilterVO;
import com.code.service.CodeService;
import com.user.service.UserFilterVO;
import com.user.service.UserService;

@Controller
public class UserController {

	private final UserService userService;
	private final CodeService codeService;

	public UserController(UserService userService, CodeService codeService) {
		this.userService = userService;
		this.codeService = codeService;
	}

	@RequestMapping("/user/list.do")
	public String userList(UserFilterVO filterVO, Model model) throws Exception {
		model.addAttribute("users", userService.selectUserList(filterVO));

		CodeFilterVO userTypeFilter = new CodeFilterVO();
		userTypeFilter.setParentCode("USER_TYPE_ROOT");
		model.addAttribute("userTypes", codeService.selectCmmCodeDetail(userTypeFilter));

		CodeFilterVO userStatusFilter = new CodeFilterVO();
		userStatusFilter.setParentCode("USER_STAT_ROOT");
		model.addAttribute("userStatuses", codeService.selectCmmCodeDetail(userStatusFilter));
		
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", filterVO.getTotalCnt() == 0 ? 0
				: (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize());
		return "forward:/WEB-INF/jsp/user/UserList.jsp";
	}

	@RequestMapping("/user/create.do")
	public String userCreate() {
		return "forward:/WEB-INF/jsp/user/UserCreate.jsp";
	}

	@RequestMapping("/user/detail.do")
	public String userDetail() {
		return "forward:/WEB-INF/jsp/user/UserDetail.jsp";
	}
}