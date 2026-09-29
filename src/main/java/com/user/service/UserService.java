package com.user.service;

import java.util.List;

public interface UserService {

	UserVO selectUser(UserVO userVO) throws Exception;

	List<UserVO> selectUserList(UserFilterVO filterVO) throws Exception;

	int selectUserListCnt(UserFilterVO filterVO) throws Exception;

	void insertUser(UserVO userVO) throws Exception;

	void updateUser(UserVO userVO) throws Exception;

	void deleteUser(UserVO userVO) throws Exception;
}