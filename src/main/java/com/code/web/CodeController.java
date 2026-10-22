package com.code.web;

import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.context.MessageSource;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

	@InitBinder({"codeCreateVO", "codeUpdateVO"})
	public void initCodeCreateBinder(WebDataBinder binder) {
		if ("codeUpdateVO".equals(binder.getObjectName())) {
			binder.setAllowedFields("cid", "parentCid", "name", "code", "dsc", "status");
		} else {
			binder.setAllowedFields("parentCid", "name", "code");
		}
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

	@RequestMapping(value = "/code/insert.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
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
	
	@RequestMapping(value = "/code/detail.do", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> detailCode(@RequestParam("cid") Long cid) throws Exception {
		CodeDetailVO query = new CodeDetailVO(); query.setCid(validCodeId(cid));
		CodeDetailVO code = codeService.selectCode(query);
		if (code == null) { 
			throw new CodeService.CodeException(404, "CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", null); 
		}
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("cid", code.getCid()); data.put("parentCid", code.getParentCid());
		data.put("parentCode", code.getParentCode()); data.put("parentName", code.getParentName());
		data.put("code", code.getCode()); data.put("name", code.getName()); data.put("level", code.getLevel());
		data.put("dsc", code.getDsc()); data.put("status", code.getStatus());
		return ResponseEntity.ok(ComResponseVO.success(data, null).toJson());
	}

	@RequestMapping(value = "/code/update.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> updateCode(@ModelAttribute("codeUpdateVO") CodeDetailVO code,
			BindingResult errors) throws Exception {
		if (code.getName() != null) { 
			code.setName(code.getName().trim()); 
		}
		if (code.getCode() != null) { 
			code.setCode(code.getCode().trim()); 
		}
		
		beanValidator.validate(code, errors);
		
		if (!errors.hasFieldErrors("cid") && (code.getCid() == null || code.getCid() <= 0)) {
			errors.rejectValue("cid", "errors.code.id", "코드 ID를 확인해주세요.");
		}
		if (!"Y".equals(code.getStatus()) && !"N".equals(code.getStatus())) {
			errors.rejectValue("status", "errors.code.status", "상태는 사용 또는 미사용으로 지정해주세요.");
		}
		if (code.getDsc() != null && code.getDsc().length() > 500) {
			errors.rejectValue("dsc", "errors.code.description", "설명은 500자 이내로 입력해주세요.");
		}
		if (errors.hasErrors()) {
			return ResponseEntity.badRequest().body(ComResponseVO.validationFailure(errors, messageSource,
					Locale.KOREAN, "VALIDATION_FAILED", "입력값을 확인해주세요.").toJson());
		}
		
		try { 
			codeService.updateCode(code); 
		}
		
		catch (DataIntegrityViolationException exception) {
			errors.rejectValue("code", "errors.code.duplicate");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(ComResponseVO.validationFailure(errors,
					messageSource, Locale.KOREAN, "CODE_CONFLICT", "코드 정보를 확인해주세요.").toJson());
		}
		return ResponseEntity.ok(ComResponseVO.success(null, "코드가 수정되었습니다.").toJson());
	}

	@RequestMapping(value = "/code/delete.do", method = RequestMethod.POST)
	public String deleteCode(@RequestParam(value = "cid", required = false) String cid,
			RedirectAttributes redirectAttributes) throws Exception {
		String message;
		try {
			CodeDetailVO query = new CodeDetailVO();
			query.setCid(validCodeId(cid == null ? null : Long.valueOf(cid)));
			codeService.deleteCode(query);
			message = "코드가 삭제되었습니다.";
		} catch (NumberFormatException exception) {
			message = "코드 ID를 확인해주세요.";
		} catch (CodeService.CodeException exception) {
			message = exception.getMessage();
		} catch (DataIntegrityViolationException exception) {
			message = "다른 데이터에서 사용 중인 코드는 삭제할 수 없습니다.";
		}
		redirectAttributes.addFlashAttribute("codeDeleteMessage", message);
		return "redirect:/code/list.do";
	}

	@ExceptionHandler(CodeService.CodeException.class)
	public ResponseEntity<String> handleCodeError(CodeService.CodeException exception) {
		ComResponseVO<Void> result = exception.getField() == null
				? ComResponseVO.fail(exception.getResultCode(), exception.getMessage())
				: ComResponseVO.fieldFailure(exception.getResultCode(), exception.getMessage(), exception.getField(),
						exception.getResultCode(), exception.getMessage());
		return ResponseEntity.status(exception.getStatus())
				.contentType(org.springframework.http.MediaType.parseMediaType("application/json;charset=UTF-8"))
				.body(result.toJson());
	}

	private Long validCodeId(Long cid) {
		if (cid == null || cid <= 0) { throw new CodeService.CodeException(400, "CODE_ID_INVALID", "코드 ID를 확인해주세요.", "cid"); }
		return cid;
	}
}
