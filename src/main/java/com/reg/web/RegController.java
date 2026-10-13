package com.reg.web;

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

	@InitBinder({"regCreateVO", "regUpdateVO"})
	public void initRegCreateBinder(WebDataBinder binder) {
		if ("regUpdateVO".equals(binder.getObjectName())) {
			binder.setAllowedFields("rid", "parentRegId", "name", "code", "geom", "status");
		} else {
			binder.setAllowedFields("parentRegId", "name", "code", "geom");
		}
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
	public ResponseEntity<String> detailReg(@RequestParam("rid") Long rid) throws Exception {
		RegDetailVO query = new RegDetailVO(); query.setRid(validRegId(rid));
		RegDetailVO code = regService.selectReg(query);
		if (code == null) { throw new RegService.RegException(404, "REG_NOT_FOUND", "행정구역을 찾을 수 없습니다.", null); }
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("rid", code.getRid()); data.put("parentRegId", code.getParentRegId());
		data.put("parentCode", code.getParentCode()); data.put("parentName", code.getParentName());
		data.put("code", code.getCode()); data.put("name", code.getName()); data.put("level", code.getLevel());
		data.put("geom", code.getGeom()); data.put("status", code.getStatus());
		data.put("cDate", code.getCDate()); data.put("uDate", code.getUDate());
		return ResponseEntity.ok(ComResponseVO.success(data, null).toJson());
	}

	@RequestMapping(value = "/region/update.do", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public ResponseEntity<String> updateReg(@ModelAttribute("regUpdateVO") RegDetailVO code,
			BindingResult errors) throws Exception {
		if (code.getName() != null) { code.setName(code.getName().trim()); }
		if (code.getCode() != null) { code.setCode(code.getCode().trim()); }
		beanValidator.validate(code, errors);
		if (!errors.hasFieldErrors("rid") && (code.getRid() == null || code.getRid() <= 0)) {
			errors.rejectValue("rid", "errors.reg.id", "행정구역 ID를 확인해주세요.");
		}
		if (!"Y".equals(code.getStatus()) && !"N".equals(code.getStatus())) {
			errors.rejectValue("status", "errors.reg.status", "상태는 사용 또는 미사용으로 지정해주세요.");
		}
		if (errors.hasErrors()) {
			return ResponseEntity.badRequest().body(ComResponseVO.validationFailure(errors, messageSource,
					Locale.KOREAN, "VALIDATION_FAILED", "입력값을 확인해주세요.").toJson());
		}
		try { regService.updateReg(code); }
		catch (DataIntegrityViolationException exception) {
			errors.reject("errors.reg.conflict", "행정구역 코드 또는 상위 행정구역을 확인해주세요.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(ComResponseVO.validationFailure(errors,
					messageSource, Locale.KOREAN, "REG_CONFLICT", "행정구역 정보를 확인해주세요.").toJson());
		}
		return ResponseEntity.ok(ComResponseVO.success(null, "행정구역이 수정되었습니다.").toJson());
	}

	@RequestMapping(value = "/region/delete.do", method = RequestMethod.POST)
	public String deleteReg(@RequestParam(value = "rid", required = false) String rid,
			RedirectAttributes redirectAttributes) throws Exception {
		String message;
		try {
			RegDetailVO query = new RegDetailVO();
			query.setRid(validRegId(rid == null ? null : Long.valueOf(rid)));
			regService.deleteReg(query);
			message = "행정구역이 삭제되었습니다.";
		} catch (NumberFormatException exception) {
			message = "행정구역 ID를 확인해주세요.";
		} catch (RegService.RegException exception) {
			message = exception.getMessage();
		} catch (DataIntegrityViolationException exception) {
			message = "다른 데이터에서 사용 중인 행정구역은 삭제할 수 없습니다.";
		}
		redirectAttributes.addFlashAttribute("regDeleteMessage", message);
		return "redirect:/region/list.do";
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

	private Long validRegId(Long rid) {
		if (rid == null || rid <= 0) { throw new RegService.RegException(400, "REG_ID_INVALID", "행정구역 ID를 확인해주세요.", "rid"); }
		return rid;
	}
}
