package com.user.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.user.service.MeService;
import com.user.service.MeVO;

@Controller
public class MeController {

	private static final String JSON = "application/json;charset=UTF-8";

	private final MeService meService;

	public MeController(MeService meService) {
		this.meService = meService;
	}

	@RequestMapping(value = "/me/list.do", produces = JSON)
	@ResponseBody
	public String list() throws Exception {
		List<MeVO> list = meService.selectMeList();
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				json.append(',');
			}
			json.append(toJson(list.get(i)));
		}
		return json.append(']').toString();
	}

	@RequestMapping(value = "/me/switch.do", produces = JSON)
	@ResponseBody
	public String switchMe(Long uid) throws Exception {
		MeVO me = meService.switchMe(uid);
		return me == null ? "null" : toJson(me);
	}

	private static String toJson(MeVO me) {
		return "{\"uid\":" + me.getUid()
				+ ",\"email\":\"" + escape(me.getEmail())
				+ "\",\"nickname\":\"" + escape(me.getNickname())
				+ "\",\"type\":\"" + escape(me.getType())
				+ "\",\"status\":\"" + escape(me.getStatus()) + "\"}";
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (char c : value.toCharArray()) {
			if (c == '"' || c == '\\') {
				sb.append('\\').append(c);
			} else if (c < 0x20) {
				sb.append(String.format("\\u%04x", (int) c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
