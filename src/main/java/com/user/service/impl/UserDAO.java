package com.user.service.impl;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import com.user.service.UserVO;

@Repository
public class UserDAO extends EgovAbstractMapper {

	public UserVO selectUser(UserVO userVO) throws Exception {
		return selectOne("userDAO.selectUser", userVO);
	}

	public List<UserVO> selectUserList(UserVO userVO) throws Exception {
		return selectList("userDAO.selectUserList", userVO);
	}

	public void insertUser(UserVO userVO) throws Exception {
		insert("userDAO.insertUser", userVO);
	}

	public void updateUser(UserVO userVO) throws Exception {
		update("userDAO.updateUser", userVO);
	}

	public void deleteUser(UserVO userVO) throws Exception {
		delete("userDAO.deleteUser", userVO);
	}
}