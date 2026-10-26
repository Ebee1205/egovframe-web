package com.extra.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.cmm.service.ComResponseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Map;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExtraController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtraController.class);

    @RequestMapping("/extra/list.do")
    public String extraList(Model model) throws Exception {
    	

    	
        return "forward:/WEB-INF/jsp/extra/ExtraList.jsp";
    }
    
    
    @RequestMapping("/extra/file/upload.do")
    public String extraFileUpload(Model model) throws Exception {
    	
    	// 엑셀 파싱
    	// 엑셀 validation
    	// validatoin json으로 retrun 
    		
    	//
    	
    	return 'return';
    }
}
