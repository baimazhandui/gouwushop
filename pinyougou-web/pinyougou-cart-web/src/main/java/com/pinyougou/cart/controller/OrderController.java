package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.CartService;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    // 订单服务接口
    @Reference(timeout = 10000)
    private OrderService orderService;

    @Reference(timeout = 30000)
    private CartService cartService;

    // 微信支付服务接口
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /*@GetMapping("/updateCart")
    public boolean updateCart(Long[] selectedIds, HttpServletRequest request) {
        try {
            String username = request.getRemoteUser();
            List<Cart> carts = cartService.findCartRedis(username);
            carts = cartService.updateCart(carts, selectedIds);
            // 往Redis中存储购物车
            cartService.saveCartRedis(username, carts);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }*/

    // 保存订单信息
    @PostMapping("/save")
    public boolean save(@RequestBody Order order, Long[] itemIds, HttpServletRequest request) {
        try {
            // 获取登录用户名
            String userId = request.getRemoteUser();
            order.setUserId(userId);
            // 设置订单来源为 PC 端
            order.setSourceType("2");
            orderService.save(order, itemIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 生成微信支付二维码
    @GetMapping("/genPayCode")
    public Map<String, String> genPayCode(HttpServletRequest request) {
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 从 Redis 查询支付日志
        PayLog payLog = orderService.findPayLogFromRedis(userId);
        // 调用生成微信支付二维码方法
        return weixinPayService.genPayCode(payLog.getOutTradeNo(),
                String.valueOf(payLog.getTotalFee()));
    }

    // 查询支付状态
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo) {
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);

        try {
            // 调用查询订单接口
            Map<String, String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            if (resMap != null && resMap.size() > 0) {
                // 判断是否支付成功
                if ("SUCCESS".equals(resMap.get("trade_state"))) {
                    // 修改订单状态
                    orderService.updateOrderStatus(outTradeNo, resMap.get("transaction_id"));
                    data.put("status", 1);
                }
                if ("NOTPAY".equals(resMap.get("trade_state"))) {
                    data.put("status", 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
