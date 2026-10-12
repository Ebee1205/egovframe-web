package com.code.web;

import java.util.Locale;

import org.springframework.context.MessageSource;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springmodules.validation.commons.DefaultBeanValidator;

import com.code.service.CodeService;
import com.code.service.CodeDetailVO;
import com.cmm.service.ComResponseVO;

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

	@InitBinder("codeCreateVO")
	public void initCodeCreateBinder(WebDataBinder binder) {
		binder.setAllowedFields("parentCid", "name", "code");
		binder.setMessageCodesResolver(new DefaultMessageCodesResolver() {
			@Override
			public String[] resolveMessageCodes(String errorCode, String objectName, String field, Class<?> fieldType) {
				if ("typeMismatch".equals(errorCode) && "parentCid".equals(field)) {
					return new String[] { "errors.code.parent" };
				}
				return super.resolveMessageCodes(errorCode, objectName, field, fieldType);
			}
		});
	}

	@RequestMapping("/code/list.do")
	public String codeList(CodeDetailVO filterVO, Model model) throws Exception {
		model.addAttribute("codes", codeService.selectCodeList(filterVO));
		model.addAttribute("rootCodes", codeService.selectCodeRoot());
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", filterVO.getPageSize() > 0 && filterVO.getTotalCnt() > 0
				? (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize() : 0);
		return "forward:/WEB-INF/jsp/code/CodeList.jsp";
	}

	@RequestMapping(value = "/code/insert.do", method = RequestMethod.POST,
			produces = "application/json")
	public ResponseEntity<String> insertCode(
			@ModelAttribute("codeCreateVO") CodeDetailVO codeVO, BindingResult bindingResult) throws Exception {
		if (codeVO.getCode() != null) {
			codeVO.setCode(codeVO.getCode().trim());
		}
		if (codeVO.getName() != null) {
			codeVO.setName(codeVO.getName().trim());
		}
		beanValidator.validate(codeVO, bindingResult);

		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest()
					.body(ComResponseVO.validationFailure(bindingResult, messageSource, Locale.KOREAN, "VALIDATION_FAILED", "입력값을 확인해주세요.").toJson());
		}

		codeVO.setLevel(codeVO.getParentCid() == null ? 1 : 2);
		codeVO.setStatus("Y");

		try {
			codeService.insertCode(codeVO);
		} catch (DataIntegrityViolationException exception) {
			bindingResult.rejectValue("code", "errors.code.duplicate");
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(ComResponseVO.validationFailure(bindingResult, messageSource, Locale.KOREAN, "CODE_DUPLICATE", "이미 사용 중인 코드입니다.").toJson());
		}

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ComResponseVO.success(null,
						messageSource.getMessage("success.code.insert", null, Locale.KOREAN)).toJson());
	}
}
