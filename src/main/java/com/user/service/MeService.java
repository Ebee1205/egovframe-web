package com.user.service;

import java.util.List;

public interface MeService {

	/** 현재 로그인한 계정 조회. uid가 null이거나 존재하지 않으면 null */
	MeVO selectMe(Long uid) throws Exception;

	/** 전환 가능한 계정 리스트 */
	List<MeVO> selectMeList() throws Exception;

	/** 계정 전환(로그인) 대상 검증 후 계정 반환. 존재하지 않으면 null */
	MeVO switchMe(Long uid) throws Exception;
}
