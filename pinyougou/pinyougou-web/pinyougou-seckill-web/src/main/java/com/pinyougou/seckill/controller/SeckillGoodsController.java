package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

// 秒杀订单控制器
@RestController
@RequestMapping("/seckill")
public class SeckillGoodsController {

    @Reference(timeout = 10000)
    private SeckillGoodsService seckillGoodsService;

    @GetMapping("/findSeckillGoods")
    public List<SeckillGoods> findSeckillGoods() {
        return seckillGoodsService.findSeckillGoods();
    }

    @GetMapping("/findOne")
    public SeckillGoods findOne(Long id) {
        return seckillGoodsService.findOneFromRedis(id);
    }

    @GetMapping("/submitOrder")
    public boolean submitOrder(Long id, HttpServletRequest request) {
        try {
            // 获取登录用户名
            String userId = request.getRemoteUser();
            // 提交订单到 redis
            seckillGoodsService.submitOrderToRedis(id, userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
