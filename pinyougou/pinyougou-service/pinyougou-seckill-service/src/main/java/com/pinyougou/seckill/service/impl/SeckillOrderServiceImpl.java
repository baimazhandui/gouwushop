package com.pinyougou.seckill.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SeckillOrderService")
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 添加方法
     *
     * @param seckillOrder
     */
    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    /**
     * 修改方法
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {

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
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param seckillOrder
     * @param page
     * @param rows
     */
    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }

    /**
     * 根据用户 id 从 Redis 查询对应秒杀订单
     *
     * @param userId
     */
    @Override
    public SeckillOrder findOrderFromRedis(String userId) {
        try {
            return (SeckillOrder) redisTemplate
                    .boundHashOps("seckillOrderList").get(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据用户 id 和交易号保存秒杀订单
     *
     * @param userId
     * @param transactionId
     */
    @Override
    public void saveOrder(String userId, String transactionId) {
        try{
            /** 根据用户ID从redis中查询秒杀订单 */
            SeckillOrder seckillOrder = (SeckillOrder)redisTemplate
                    .boundHashOps("seckillOrderList").get(userId);
            /** 判断秒杀订单 */
            if(seckillOrder != null){
                /** 微信交易流水号 */
                seckillOrder.setTransactionId(transactionId);
                /** 支付时间 */
                seckillOrder.setPayTime(new Date());
                /** 状态码(已付款) */
                seckillOrder.setStatus("1");
                /** 保存到数据库 */
                seckillOrderMapper.insertSelective(seckillOrder);
                /** 删除Redis中的订单 */
                redisTemplate.boundHashOps("seckillOrderList").delete(userId);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询所有超时未支付的秒杀订单
     */
    @Override
    public List<SeckillOrder> findOrderByTimout() {
        // 定义 List 集合封装所有超时未支付订单
        List<SeckillOrder> timeoutList = new ArrayList<>();
        // 查询所有未支付订单
        List<SeckillOrder> seckillOrderList = redisTemplate
                .boundHashOps("seckillOrderList").values();
        System.out.println("未支付订单:" + seckillOrderList.size());
        if (seckillOrderList != null && seckillOrderList.size() > 0) {
            for (SeckillOrder seckillOrder : seckillOrderList) {
                // 时间间隔 5 分钟
                long timeoutInterval = 5 * 60 * 1000;
                long interval = new Date().getTime() - seckillOrder.getCreateTime().getTime();
                if (interval - timeoutInterval > 0) {
                    // 代表未支付订单超时
                    timeoutList.add(seckillOrder);
                }
            }
        }
        return timeoutList;
    }

    /**
     * 删除超时未支付订单
     *
     * @param seckillOrder
     */
    @Override
    public void deleteOrderFromRedis(SeckillOrder seckillOrder) {
        try {
            // 删除 Redis 中超时未支付订单
            redisTemplate.boundHashOps("seckillOrderList")
                    .delete(seckillOrder.getUserId());
            // 恢复库存数量
            // 获取 redis 中的秒杀商品数据
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(
                    "seckillGoodsList").get(seckillOrder.getSeckillId());
            // 判断缓存中是否存在该商品
            if (seckillGoods != null) {
                // 修改该商品库存数量
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            } else {
                // 缓存中没有该商品,从数据库获取
                seckillGoods = seckillGoodsMapper
                        .selectByPrimaryKey(seckillOrder.getSeckillId());
                // 修改库存
                seckillGoods.setStockCount(1);
            }
            // 存入 redis 缓存中
            redisTemplate.boundHashOps("seckillGoodsList")
                    .put(seckillGoods.getId(), seckillGoods);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
