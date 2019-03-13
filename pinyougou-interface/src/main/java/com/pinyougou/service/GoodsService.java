package com.pinyougou.service;

import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.Item;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * GoodsService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface GoodsService {

	/** 添加方法 */
	void save(Goods goods);

	/** 修改方法 */
	void update(Goods goods);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Goods findOne(Serializable id);

	/** 查询全部 */
	List<Goods> findAll();

	/** 多条件分页查询 */
	PageResult findByPage(Goods goods, int page, int rows);

	/** 根据auditStatus更新ids数组对应id的SPU商品的状态码 */
    void updateStatus(Long[] ids, String auditStatus);

    /** 根据isMarketable更新ids数组对应的id的SPU商品的是否上架属性 */
    void updateMarketable(Long[] ids, String isMarketable);

    /** 获取商品信息 */
    Map<String,Object> getGoods(Long goodsId);

	/** 查询上架的 SKU 商品数据 */
	List<Item> findItemByGoodsId(Long[] ids);
}