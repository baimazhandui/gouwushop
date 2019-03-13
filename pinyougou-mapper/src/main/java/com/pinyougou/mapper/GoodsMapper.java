package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Goods;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * GoodsMapper 数据访问接口
 * @date 2018-09-28 18:49:11
 * @version 1.0
 */
public interface GoodsMapper extends Mapper<Goods>{

    /** 多条件查询商品 */
    List<Map<String,Object>> findAll(Goods goods);

    /** 根据auditStatus更新id数组对应id的SPU商品的状态码 */
    void updateStatus(@Param("ids") Long[] ids, @Param("auditStatus") String auditStatus);

    /** 根据isDelete修改id数组对应id的SPU商品的idDelete属性 */
    void deleteAll(@Param("ids") Serializable[] ids, @Param("isDelete") String isDelete);

    /** 根据isMarketable更新ids数组对应的id的SPU商品的是否上架属性 */
    void updateMarketable(@Param("ids") Long[] ids, @Param("isMarketable") String isMarketable);
}