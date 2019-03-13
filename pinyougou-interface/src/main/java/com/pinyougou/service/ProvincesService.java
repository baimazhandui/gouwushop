package com.pinyougou.service;

import com.pinyougou.pojo.Areas;
import com.pinyougou.pojo.Cities;
import com.pinyougou.pojo.Provinces;

import java.io.Serializable;
import java.util.List;

/**
 * ProvincesService 服务接口
 * @date 2018-09-28 19:08:41
 * @version 1.0
 */
public interface ProvincesService {

	/** 添加方法 */
	void save(Provinces provinces);

	/** 修改方法 */
	void update(Provinces provinces);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Provinces findOne(Serializable id);

	/** 查询全部 */
	List<Provinces> findAll();

	/** 多条件分页查询 */
	List<Provinces> findByPage(Provinces provinces, int page, int rows);

	/** 查询所有省份数据 */
	List<Provinces> findProvinces();

	/** 根据省份id查询所有城市数据 */
	List<Cities> findCities(String provinceId);

	/** 根据城市id查询所有地区数据 */
	List<Areas> findCAreas(String cityId);
}