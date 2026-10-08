package com.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.user.service.MeService;
import com.user.service.MeVO;

@Service
public class MeServiceImpl implements MeService {

	private final MeDAO meDAO;

	public MeServiceImpl(MeDAO meDAO) {
		this.meDAO = meDAO;
	}

	@Override
	public MeVO selectMe(Long uid) throws Exception {
		return uid == null ? null : meDAO.selectMe(uid);
	}

	@Override
	public List<MeVO> selectMeList() throws Exception {
		return meDAO.selectMeList();
	}

	@Override
	public MeVO switchMe(Long uid) throws Exception {
		return selectMe(uid);
	}
}
