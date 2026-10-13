package com.reg.web;

import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

import com.reg.service.RegService;
import com.reg.service.RegDetailVO;
import com.cmm.service.ComResponseVO;

@Controller
public class RegController {

	private final RegService regService;

	@Autowired
	private DefaultBeanValidator beanValidator;

	@Autowired
	private MessageSource messageSource;

	public RegController(RegService regService) {
		this.regService = regService;
	}

    @InitBinder("regCreateVO")
    public void initRegCreateBinder(WebDataBinder binder) {
        binder.setAllowedFields("parentRegId", "name", "code");
        binder.setMessageCodesResolver(new DefaultMessageCodesResolver() {
            @Override
            public String[] resolveMessageCodes(String errorCode, String objectName, String field, Class<?> fieldType) {
                if ("typeMismatch".equals(errorCode) && "parentRegId".equals(field)) {
                    return new String[] { "errors.reg.parent" };
                }
                return super.resolveMessageCodes(errorCode, objectName, field, fieldType);
            }
        });
    }

	@RequestMapping("/region/list.do")
	public String regList(RegDetailVO filterVO, Model model) throws Exception {
		model.addAttribute("regs", regService.selectRegList(filterVO));
		model.addAttribute("parentRegs", regService.selectRegParents());
		model.addAttribute("rootRegs", regService.selectRegRoot());
		model.addAttribute("filterVO", filterVO);
		model.addAttribute("totalPage", filterVO.getPageSize() > 0 && filterVO.getTotalCnt() > 0
				? (filterVO.getTotalCnt() + filterVO.getPageSize() - 1) / filterVO.getPageSize() : 0);
		return "forward:/WEB-INF/jsp/reg/RegList.jsp";
	}

	@RequestMapping(value = "/region/insert.do", method = RequestMethod.POST,
			produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> insertReg(
			@ModelAttribute("regCreateVO") RegDetailVO regVO, BindingResult bindingResult) throws Exception {
		if (regVO.getCode() != null) {
			regVO.setCode(regVO.getCode().trim());
		}
		if (regVO.getName() != null) {
			regVO.setName(regVO.getName().trim());
		}
		beanValidator.validate(regVO, bindingResult);

		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest()
					.body(ComResponseVO.validationFailure(bindingResult, messageSource, Locale.KOREAN, "VALIDATION_FAILED", "입력값을 확인해주세요.").toJson());
		}

		regVO.setStatus("Y");

		try {
			regService.insertReg(regVO);
		} catch (DataIntegrityViolationException exception) {
			bindingResult.reject("errors.reg.conflict", "행정구역 코드 또는 상위 행정구역을 확인해주세요.");
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(ComResponseVO.validationFailure(bindingResult, messageSource, Locale.KOREAN, "REG_CONFLICT", "행정구역 코드 또는 상위 행정구역을 확인해주세요.").toJson());
		}

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ComResponseVO.success(null,
						messageSource.getMessage("success.reg.insert", null, Locale.KOREAN)).toJson());
	}
	
	@RequestMapping(value = "/region/detail.do", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> detailReg(@RequestParam("reg_id") Long reg_id) throws Exception {
		RegDetailVO query = new RegDetailVO(); query.setReg_id(validRegId(reg_id));
		RegDetailVO code = regService.selectReg(query);
		if (code == null) { throw new RegService.RegException(404, "REG_NOT_FOUND", "행정구역을 찾을 수 없습니다.", null); }
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("reg_id", code.getReg_id()); data.put("parentRegId", code.getParentRegId());
		data.put("parentCode", code.getParentCode()); data.put("parentName", code.getParentName());
		data.put("code", code.getCode()); data.put("name", code.getName()); data.put("level", code.getLevel());
		data.put("status", code.getStatus());
		data.put("cDate", code.getCDate()); data.put("uDate", code.getUDate());
		return ResponseEntity.ok(ComResponseVO.success(data, null).toJson());
	}

	@ExceptionHandler(RegService.RegException.class)
	public ResponseEntity<String> handleRegError(RegService.RegException exception) {
		ComResponseVO<Void> result = exception.getField() == null
				? ComResponseVO.fail(exception.getResultCode(), exception.getMessage())
				: ComResponseVO.fieldFailure(exception.getResultCode(), exception.getMessage(), exception.getField(),
						exception.getResultCode(), exception.getMessage());
		return ResponseEntity.status(exception.getStatus())
				.contentType(org.springframework.http.MediaType.parseMediaType("application/json;charset=UTF-8"))
				.body(result.toJson());
	}

	private Long validRegId(Long reg_id) {
		if (reg_id == null || reg_id <= 0) { throw new RegService.RegException(400, "REG_ID_INVALID", "행정구역 ID를 확인해주세요.", "reg_id"); }
		return reg_id;
	}
}
