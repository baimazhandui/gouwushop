package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.AddressMapper;
import com.pinyougou.pojo.Address;
import com.pinyougou.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.AddressService")
@Transactional
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;
    /**
     * 添加方法
     *
     * @param address
     */
    @Override
    public void save(Address address) {

    }

    /**
     * 修改方法
     *
     * @param address
     */
    @Override
    public void update(Address address) {

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
    public Address findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Address> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param address
     * @param page
     * @param rows
     */
    @Override
    public List<Address> findByPage(Address address, int page, int rows) {
        return null;
    }

    /**
     * 根据登录用户名查询地址
     *
     * @param userId
     */
    @Override
    public List<Address> findAddressByUser(String userId) {
        try {
            Address address = new Address();
            address.setUserId(userId);

            return addressMapper.select(address);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加地址
     *
     * @param address
     */
    @Override
    public void saveAddress(Address address) {
        addressMapper.insertSelective(address);
    }

    /**
     * 修改地址
     *
     * @param address
     */
    @Override
    public void updateAddress(Address address) {
        addressMapper.updateByPrimaryKeySelective(address);
    }

    /**
     * 根据地址id删除指定地址
     *
     * @param id
     */
    @Override
    public void deleteAddress(Long id) {
        addressMapper.deleteByPrimaryKey(id);
    }
}
