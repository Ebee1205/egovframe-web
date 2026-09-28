package com.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.user.service.UserService;
import com.user.service.UserVO;

@Service
public class UserServiceImpl implements UserService {

	private final UserDAO userDAO;

	public UserServiceImpl(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	@Override
	public UserVO selectUser(UserVO userVO) throws Exception {
		return userDAO.selectUser(userVO);
	}

	@Override
	public List<UserVO> selectUserList(UserVO userVO) throws Exception {
		return userDAO.selectUserList(userVO);
	}

	@Override
	public void insertUser(UserVO userVO) throws Exception {
		userDAO.insertUser(userVO);
	}

	@Override
	public void updateUser(UserVO userVO) throws Exception {
		userDAO.updateUser(userVO);
	}

	@Override
	public void deleteUser(UserVO userVO) throws Exception {
		userDAO.deleteUser(userVO);
	}
}