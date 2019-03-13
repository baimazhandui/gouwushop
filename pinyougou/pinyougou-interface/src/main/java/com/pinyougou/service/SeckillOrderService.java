package com.pinyougou.service;

import com.pinyougou.pojo.SeckillOrder;

import java.io.Serializable;
import java.util.List;

/**
 * SeckillOrderService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface SeckillOrderService {

	/** 添加方法 */
	void save(SeckillOrder seckillOrder);

	/** 修改方法 */
	void update(SeckillOrder seckillOrder);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	SeckillOrder findOne(Serializable id);

	/** 查询全部 */
	List<SeckillOrder> findAll();

	/** 多条件分页查询 */
	List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows);

	/** 根据用户 id 从 Redis 查询对应秒杀订单 */
    SeckillOrder findOrderFromRedis(String userId);

    /** 根据用户 id 和交易号保存秒杀订单 */
	void saveOrder(String userId, String transactionId);

	/** 查询所有超时未支付的秒杀订单 */
    List<SeckillOrder> findOrderByTimout();

    /** 删除超时未支付订单 */
    void deleteOrderFromRedis(SeckillOrder seckillOrder);
}