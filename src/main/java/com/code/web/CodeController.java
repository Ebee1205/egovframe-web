package com.code.web;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springmodules.validation.commons.DefaultBeanValidator;

import com.code.service.CodeService;
import com.code.service.CodeDetailVO;

@Controller
public class CodeController {

	private final CodeService codeService;

	@Autowired
	private DefaultBeanValidator beanValidator;

	@Autowired
	private MessageSource messageSource;

	public CodeController(CodeService codeService) {
		this.codeService = codeService;
	}

	@RequestMapping("/code/list.do")
	public String codeList(CodeDetailVO filterVO, Model model) throws Exception {
		model.addAttribute("codes", codeService.selectCodeList(filterVO));
		model.addAttribute("rootCodes", codeService.selectCodeRoot());
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", (filterVO.getTotalCnt() - 1) / filterVO.getPageSize() + 1);
		return "forward:/WEB-INF/jsp/code/CodeList.jsp";
	}

	@RequestMapping(value = "/code/insert.do", method = RequestMethod.POST)
	public ResponseEntity<String> insertCode(
			@ModelAttribute("codeVO") CodeDetailVO codeVO, BindingResult bindingResult) throws Exception {
		beanValidator.validate(codeVO, bindingResult);

		if (bindingResult.hasErrors()) {
			// 응답 본문은 "필드명:메시지" 형식의 줄 단위 목록으로, 화면에서 필드별 오류로 분리해 표시한다.
			StringBuilder errorMessage = new StringBuilder();
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				if (errorMessage.length() > 0) {
					errorMessage.append("\n");
				}
				errorMessage.append(fieldError.getField()).append(":").append(messageSource.getMessage(fieldError, Locale.KOREAN));
			}
			return ResponseEntity.badRequest().body(errorMessage.toString());
		}

		codeVO.setCode(codeVO.getCode().trim());
		codeVO.setName(codeVO.getName().trim());
		codeVO.setLevel(codeVO.getParentCid() == null ? 1 : 2);
		codeVO.setStatus("Y");

		try {
			codeService.insertCode(codeVO);
		} catch (DataIntegrityViolationException exception) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("code:같은 부모 코드 아래에 동일한 코드가 이미 있습니다.");
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("코드가 생성되었습니다.");
	}
}
