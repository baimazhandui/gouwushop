package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SellerMapper;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SellerService")
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerMapper sellerMapper;

    /**
     * 添加方法
     *
     * @param seller
     */
    @Override
    public void save(Seller seller) {
        try {
            seller.setStatus("0");
            seller.setCreateTime(new Date());
            sellerMapper.insertSelective(seller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改方法
     *
     * @param seller
     */
    @Override
    public void update(Seller seller) {

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
    public Seller findOne(Serializable id) {
        try {
            return sellerMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询全部
     */
    @Override
    public List<Seller> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param seller
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(Seller seller, int page, int rows) {
        try {
            PageInfo<Object> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    // 创建示范对象
                    Example example = new Example(Seller.class);
                    // 创建条件对象
                    Example.Criteria criteria = example.createCriteria();
                    // 审核状态码
                    if (seller != null && !StringUtils.isEmpty(seller.getStatus())){
                        // status = ?
                        criteria.andEqualTo("status", seller.getStatus());
                    }
                    // 公司名称
                    if (seller != null && !StringUtils.isEmpty(seller.getName())){
                        // name like ?
                        criteria.andLike("name", "%" + seller.getName() + "%");
                    }
                    // 店铺名称
                    if (seller != null && !StringUtils.isEmpty(seller.getNickName())){
                        // nick_name like ?
                        criteria.andLike("nickName", "%" + seller.getNickName() + "%");
                    }
                    sellerMapper.selectByExample(example);
                }
            });
            return new PageResult(pageInfo.getTotal(), pageInfo.getList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据sellerId修改状态码
     *
     * @param sellerId
     * @param status
     */
    @Override
    public void updateStatus(String sellerId, String status) {
        try {
            Seller seller = new Seller();
            seller.setSellerId(sellerId);
            seller.setStatus(status);
            sellerMapper.updateByPrimaryKeySelective(seller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据商家id查询商家信息
     *
     * @param sellerId
     */
    @Override
    public Seller findSeller(String sellerId) {
        return sellerMapper.selectByPrimaryKey(sellerId);
    }

    /**
     * 修改商家信息
     *
     * @param seller
     */
    @Override
    public void saveSellerMsg(Seller seller) {
        sellerMapper.updateByPrimaryKeySelective(seller);
    }

    /**
     * 修改商家密码
     *
     * @param sellerId
     * @param oldPassword
     * @param newPassword
     */
    @Override
    public String changePassword(String sellerId, String oldPassword, String newPassword) {
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            Seller seller = new Seller();
            seller.setSellerId(sellerId);
            Seller oldSeller = sellerMapper.select(seller).get(0);
            if (!encoder.matches(oldPassword, oldSeller.getPassword())) {
                return "密码错误";
            }
            newPassword = encoder.encode(newPassword);
            oldSeller.setPassword(newPassword);
            sellerMapper.updateByPrimaryKeySelective(oldSeller);
            return "修改密码成功";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改密码失败";
    }
}
