package com.pinyougou.service;

import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Seller;

import java.io.Serializable;
import java.util.List;

/**
 * SellerService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface SellerService {

	/** 添加方法 */
	void save(Seller seller);

	/** 修改方法 */
	void update(Seller seller);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Seller findOne(Serializable id);

	/** 查询全部 */
	List<Seller> findAll();

	/** 多条件分页查询 */
	PageResult findByPage(Seller seller, int page, int rows);

	/** 根据sellerId修改状态码 */
    void updateStatus(String sellerId, String status);

    /** 根据商家id查询商家信息 */
    Seller findSeller(String sellerId);

    /** 修改商家信息 */
	void saveSellerMsg(Seller seller);

	/** 修改商家密码 */
	String changePassword(String sellerId, String oldPassword, String newPassword);
}