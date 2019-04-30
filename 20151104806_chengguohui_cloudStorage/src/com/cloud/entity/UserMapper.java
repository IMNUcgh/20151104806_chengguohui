package com.cloud.entity;

import org.apache.ibatis.annotations.Param;

public interface UserMapper
{
	//新建用户
	public void addUser(User user);
	
	//通过用户名查找用户
	public User selectUserByUsername(String username);
	
	//更改用户状态
	public void changeUserStatus(Integer userId);

	/**
	 * 修改用户信息
	 * @param newPassword 新密码
	 * @param newEmail 新邮箱
	 */
	public void updateUser(@Param("userId") String userId, @Param("newPassword") String newPassword,
						   @Param("newEmail") String newEmail);
}
