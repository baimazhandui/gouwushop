package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.AreasMapper;
import com.pinyougou.mapper.CitiesMapper;
import com.pinyougou.mapper.ProvincesMapper;
import com.pinyougou.pojo.Areas;
import com.pinyougou.pojo.Cities;
import com.pinyougou.pojo.Provinces;
import com.pinyougou.service.ProvincesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.ProvincesService")
@Transactional
public class ProvinesServiceImpl implements ProvincesService {

    @Autowired
    private ProvincesMapper provincesMapper;

    @Autowired
    private CitiesMapper citiesMapper;

    @Autowired
    private AreasMapper areasMapper;
    /**
     * 添加方法
     *
     * @param provinces
     */
    @Override
    public void save(Provinces provinces) {

    }

    /**
     * 修改方法
     *
     * @param provinces
     */
    @Override
    public void update(Provinces provinces) {

    }

    /**
     * 根据主键id删除
     *
     * @param id
     */
    @Override
    public void delete(Serializable id) {

    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteAll(Serializable[] ids) {

    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public Provinces findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Provinces> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param provinces
     * @param page
     * @param rows
     */
    @Override
    public List<Provinces> findByPage(Provinces provinces, int page, int rows) {
        return null;
    }

    /**
     * 查询所有省份数据
     */
    @Override
    public List<Provinces> findProvinces() {
        return provincesMapper.selectAll();
    }

    /**
     * 根据省份id查询所有城市数据
     *
     * @param provinceId
     */
    @Override
    public List<Cities> findCities(String provinceId) {
        Cities cities = new Cities();
        cities.setProvinceId(provinceId);
        return citiesMapper.select(cities);
    }

    /**
     * 根据城市id查询所有地区数据
     *
     * @param cityId
     */
    @Override
    public List<Areas> findCAreas(String cityId) {
        Areas areas = new Areas();
        areas.setCityId(cityId);
        return areasMapper.select(areas);
    }
}
