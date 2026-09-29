package com.code.web;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.code.service.CodeService;
import com.code.service.CodeDetailVO;

@Controller
public class CodeController {

	private final CodeService codeService;

	public CodeController(CodeService codeService) {
		this.codeService = codeService;
	}

	@RequestMapping("/CodeList.do")
	public String userList(CodeDetailVO filterVO, Model model) throws Exception {
		model.addAttribute("codes", codeService.selectCodeList(filterVO));
		model.addAttribute("rootCodes", codeService.selectCodeRoot());
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", (filterVO.getTotalCnt() - 1) / filterVO.getPageSize() + 1);
		return "forward:/WEB-INF/jsp/code/CodeList.jsp";
	}
}