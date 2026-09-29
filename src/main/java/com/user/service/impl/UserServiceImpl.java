package com.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.user.service.UserFilterVO;
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
	public List<UserVO> selectUserList(UserFilterVO filterVO) throws Exception {
		if (filterVO.getPageIndex() < 1) {
			filterVO.setPageIndex(1);
		}
		filterVO.setFirstIndex((filterVO.getPageIndex() - 1) * filterVO.getPageSize());
		filterVO.setLastIndex(filterVO.getFirstIndex() + filterVO.getPageSize());
		filterVO.setTotalCnt(selectUserListCnt(filterVO));
		return userDAO.selectUserList(filterVO);
	}

	@Override
	public int selectUserListCnt(UserFilterVO filterVO) throws Exception {
		return userDAO.selectUserListCnt(filterVO);
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