package com.pinyougou.service;

import com.pinyougou.pojo.SeckillGoods;

import java.io.Serializable;
import java.util.List;

/**
 * SeckillGoodsService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface SeckillGoodsService {

	/** 添加方法 */
	void save(SeckillGoods seckillGoods);

	/** 修改方法 */
	void update(SeckillGoods seckillGoods);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	SeckillGoods findOne(Serializable id);

	/** 查询全部 */
	List<SeckillGoods> findAll();

	/** 多条件分页查询 */
	List<SeckillGoods> findByPage(SeckillGoods seckillGoods, int page, int rows);

	/** 查询秒杀商品集合 */
    List<SeckillGoods> findSeckillGoods();

    /** 根据秒杀商品 id 从 redis 查询对应商品 */
	SeckillGoods findOneFromRedis(Long id);

	/** 提交订单到 Redis */
    void submitOrderToRedis(Long id, String userId);
}