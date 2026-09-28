package com.user.service;

import java.util.List;

public interface UserService {

	UserVO selectUser(UserVO userVO) throws Exception;

	List<UserVO> selectUserList(UserVO userVO) throws Exception;

	void insertUser(UserVO userVO) throws Exception;

	void updateUser(UserVO userVO) throws Exception;

	void deleteUser(UserVO userVO) throws Exception;
}