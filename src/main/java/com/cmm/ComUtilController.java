package com.cmm;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springmodules.validation.commons.DefaultBeanValidator;


/**
 * 공통유틸리티성 작업을 위한 Controller 클래스
 */

@Controller
public class ComUtilController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComUtilController.class);
	@Autowired
	private DefaultBeanValidator beanValidator;

	@PostConstruct
	public void logValidatorReady() {
		LOGGER.info("DefaultBeanValidator is configured and ready: " + beanValidator.getClass().getName());
	}

    /**
	 * validation rule dynamic java script
	 */
	
	@RequestMapping("/validator.do")
	public String validate(){
		LOGGER.info("Commons Validator JavaScript endpoint invoked: /validator.do");
		return "cmm/validator";
	}

}