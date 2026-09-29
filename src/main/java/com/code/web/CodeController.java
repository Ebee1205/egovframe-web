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
	public String userList(Model model) throws Exception {
		model.addAttribute("codes", codeService.selectCodeList(new CodeDetailVO()));
		model.addAttribute("rootCodes", codeService.selectCodeRoot());
		return "forward:/WEB-INF/jsp/code/CodeList.jsp";
	}
}