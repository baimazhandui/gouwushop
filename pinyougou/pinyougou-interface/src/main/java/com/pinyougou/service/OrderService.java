package com.pinyougou.service;

import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;

import java.io.Serializable;
import java.util.List;

/**
 * OrderService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface OrderService {

	/** 添加方法 */
	void save(Order order, Long[] itemIds);

	/** 修改方法 */
	void update(Order order);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Order findOne(Serializable id);

	/** 查询全部 */
	List<Order> findAll();

	/** 多条件分页查询 */
	PageResult findByPage(String userId, int page, int rows);

	/** 根据用户名从 Redis 查询支付日志 */
    PayLog findPayLogFromRedis(String userId);

    /** 修改订单状态 */
	void updateOrderStatus(String outTradeNo, String transactionId);

	/** 修改订单状态
	 * @param orderId*/
    void changeOrderStatus(Long orderId);

	Order findOrder(Long orderId);
}