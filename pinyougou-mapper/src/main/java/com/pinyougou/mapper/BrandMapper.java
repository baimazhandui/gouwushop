package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Brand;

import java.util.List;
import java.util.Map;

/**
 * BrandMapper 数据访问接口
 * @date 2018-09-28 18:49:11
 * @version 1.0
 */
public interface BrandMapper extends Mapper<Brand>{
    // 多条件查询品牌
    List<Brand> findAll(Brand brand);

    // 查询品牌的id和名字
    @Select("SELECT id, name text FROM tb_brand ORDER BY id ASC ")
    List<Map<String, Object>> findAllByIdAndName();
}