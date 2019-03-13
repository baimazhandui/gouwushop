package com.pinyougou.service;

import com.pinyougou.pojo.Address;
import com.pinyougou.pojo.User;

import java.io.Serializable;
import java.util.List;

/**
 * UserService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface UserService {

	/** 添加方法 */
	void save(User user);

	/** 修改方法 */
	void update(User user);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	User findOne(Serializable id);

	/** 查询全部 */
	List<User> findAll();

	/** 多条件分页查询 */
	List<User> findByPage(User user, int page, int rows);

	/** 根据电话号码发送短信验证码 */
	boolean sendCode(String phone);

	/** 校验验证码 */
    boolean checkSmsCode(String phone, String smsCode);

    /** 根据用户名查询用户 */
    User findUser(String username);

    /** 根据用户id查询地址列表 */
    List<Address> findAddressByUser(String userId);

    /** 根据用户id和地址id修改默认状态 */
	void updateDefault(String username, Long id);

	/** 添加地址 */
	void saveAddress(Address address);

	/** 修改地址 */
	void updateAddress(Address address);

	/** 根据地址id删除指定地址 */
	void deleteAddress(Long id);

    void saveMsg(String username, String phone);
}