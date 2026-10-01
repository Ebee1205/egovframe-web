package com.code.web;

import java.util.Locale;
import javax.json.Json;

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
import com.cmm.util.ErrMessageBuildUtil;

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

	@InitBinder("codeDetailVO")
	public void initCodeBinder(WebDataBinder binder) {
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
		model.addAttribute("totalPage", (filterVO.getTotalCnt() - 1) / filterVO.getPageSize() + 1);
		return "forward:/WEB-INF/jsp/code/CodeList.jsp";
	}

	@RequestMapping(value = "/code/insert.do", method = RequestMethod.POST,
			produces = "application/json")
	public ResponseEntity<String> insertCode(
			@ModelAttribute("codeDetailVO") CodeDetailVO codeVO, BindingResult bindingResult) throws Exception {
		if (codeVO.getCode() != null) {
			codeVO.setCode(codeVO.getCode().trim());
		}
		if (codeVO.getName() != null) {
			codeVO.setName(codeVO.getName().trim());
		}
		beanValidator.validate(codeVO, bindingResult);

		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest()
					.body(ErrMessageBuildUtil.build(bindingResult, messageSource, Locale.KOREAN));
		}

		codeVO.setLevel(codeVO.getParentCid() == null ? 1 : 2);
		codeVO.setStatus("Y");

		try {
			codeService.insertCode(codeVO);
		} catch (DataIntegrityViolationException exception) {
			bindingResult.rejectValue("code", "errors.code.duplicate");
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(ErrMessageBuildUtil.build(bindingResult, messageSource, Locale.KOREAN));
		}

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Json.createObjectBuilder().add("message",
						messageSource.getMessage("success.code.insert", null, Locale.KOREAN)).build().toString());
	}
}
