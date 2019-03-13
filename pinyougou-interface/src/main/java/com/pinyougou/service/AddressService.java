package com.pinyougou.service;

import com.pinyougou.pojo.Address;

import java.io.Serializable;
import java.util.List;

/**
 * AddressService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface AddressService {

	/** 添加方法 */
	void save(Address address);

	/** 修改方法 */
	void update(Address address);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Address findOne(Serializable id);

	/** 查询全部 */
	List<Address> findAll();

	/** 多条件分页查询 */
	List<Address> findByPage(Address address, int page, int rows);

	/** 根据登录用户名查询地址 */
    List<Address> findAddressByUser(String userId);

	/** 添加地址 */
	void saveAddress(Address address);

	/** 修改地址 */
	void updateAddress(Address address);

	/** 根据地址id删除指定地址 */
	void deleteAddress(Long id);
}