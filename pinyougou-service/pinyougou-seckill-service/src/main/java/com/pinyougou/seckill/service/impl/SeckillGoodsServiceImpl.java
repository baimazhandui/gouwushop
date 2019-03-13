package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SeckillGoodsService")
@Transactional
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;
    /**
     * 添加方法
     *
     * @param seckillGoods
     */
    @Override
    public void save(SeckillGoods seckillGoods) {

    }

    /**
     * 修改方法
     *
     * @param seckillGoods
     */
    @Override
    public void update(SeckillGoods seckillGoods) {

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
    public SeckillGoods findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<SeckillGoods> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param seckillGoods
     * @param page
     * @param rows
     */
    @Override
    public List<SeckillGoods> findByPage(SeckillGoods seckillGoods, int page, int rows) {
        return null;
    }

    /**
     * 查询秒杀商品集合
     */
    @Override
    public List<SeckillGoods> findSeckillGoods() {
        // 定义秒杀商品数据
        List<SeckillGoods> seckillGoodsList = null;
        seckillGoodsList = redisTemplate.boundHashOps(
                "seckillGoodsList").values();
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            System.out.println("redis缓存中数据:" + seckillGoodsList);
            return seckillGoodsList;
        }
        try {
            // 创建示范对象
            Example example = new Example(SeckillGoods.class);
            // 创建条件查询对象
            Example.Criteria criteria = example.createCriteria();
            // 添加审核通过条件
            criteria.andEqualTo("status", "1");
            // 剩余库存大于 0
            criteria.andGreaterThan("stockCount", 0);
            // 开始时间小于等于当前时间
            criteria.andLessThanOrEqualTo("startTime", new Date());
            // 结束时间大于等于当前时间
            criteria.andGreaterThanOrEqualTo("endTime", new Date());

            seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            System.out.println("===将秒杀商品存入 redis 缓存===");
            // 将秒杀商品存入 redis 缓存
            try {
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    redisTemplate.boundHashOps("seckillGoodsList")
                            .put(seckillGoods.getId(), seckillGoods);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return seckillGoodsList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据秒杀商品 id 从 redis 查询对应商品
     *
     * @param id
     */
    @Override
    public SeckillGoods findOneFromRedis(Long id) {
        try {
            /*return (SeckillGoods) redisTemplate
                    .boundHashOps("seckillGoodsList").get("id");*/
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                    .boundHashOps("seckillGoodsList").get(id);
            return seckillGoods;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交订单到 Redis
     *
     * @param id
     * @param userId
     */
    @Override
    public void submitOrderToRedis(Long id, String userId) {
        try {
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                    .boundHashOps("seckillGoodsList").get(id);
            // 判断库存数据
            if (seckillGoods != null && seckillGoods.getStockCount() > 0) {
                // 库存 -1
                seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
                // 判断是否已经被秒光
                if (seckillGoods.getStockCount() == 0) {
                    // 同步秒杀商品到数据库(修改库存)
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                    // 删除 redis 中的秒杀数据
                    redisTemplate.boundHashOps("seckillGoodsList")
                            .delete(id);
                } else {
                    // 重新存入 redis
                    redisTemplate.boundHashOps("seckillGoodsList")
                            .put(id, seckillGoods);
                }
                // 创建秒杀订单对象
                SeckillOrder seckillOrder = new SeckillOrder();
                // 设置订单 id
                seckillOrder.setId(idWorker.nextId());
                // 设置秒杀商品 id
                seckillOrder.setSeckillId(id);
                // 设置秒杀价格
                seckillOrder.setMoney(seckillGoods.getCostPrice());
                // 设置用户 id
                seckillOrder.setUserId(userId);
                // 设置商家 id
                seckillOrder.setSellerId(seckillGoods.getSellerId());
                // 设置创建时间
                seckillOrder.setCreateTime(new Date());
                // 设置状态码(未付款)
                seckillOrder.setStatus("0");
                // 保存到 redis 中
                redisTemplate.boundHashOps("seckillOrderList")
                        .put(userId, seckillOrder);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
