package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.TypeTemplate;

import java.util.List;
import java.util.Map;

/**
 * TypeTemplateMapper 数据访问接口
 * @date 2018-09-28 18:49:11
 * @version 1.0
 */
public interface TypeTemplateMapper extends Mapper<TypeTemplate>{
    /** 多条件查询类型模版 */
    List<TypeTemplate> findAll(TypeTemplate typeTemplate);

    /** 查询所有模板的id和名字 */
    @Select("SELECT id, name text FROM tb_type_template ORDER BY id ASC")
    List<Map<String,Object>> findAllByIdAndName();
}