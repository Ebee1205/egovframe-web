package com.cmm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 공통유틸리티성 작업을 위한 Controller 클래스
 */

@Controller
public class ComUtilController {

	// private static final Logger LOGGER = LoggerFactory.getLogger(ComUtilController.class);

    /**
	 * validation rule dynamic java script
	 */
	
	@RequestMapping("/validator.do")
	public String validate(){
		return "cmm/validator";
	}

}