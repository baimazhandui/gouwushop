package com.pinyougou.service;

import com.pinyougou.pojo.PayLog;

import java.io.Serializable;
import java.util.List;

/**
 * PayLogService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface PayLogService {

	/** 添加方法 */
	void save(PayLog payLog);

	/** 修改方法 */
	void update(PayLog payLog);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	PayLog findOne(Serializable id);

	/** 查询全部 */
	List<PayLog> findAll();

	/** 多条件分页查询 */
	List<PayLog> findByPage(PayLog payLog, int page, int rows);

}