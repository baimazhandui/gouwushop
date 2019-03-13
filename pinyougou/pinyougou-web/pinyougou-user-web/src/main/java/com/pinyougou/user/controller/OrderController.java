package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.WeixinPayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(timeout = 10000)
    private OrderService orderService;

    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    @GetMapping("/findByPage")
    public PageResult findByPage(HttpServletRequest request, Integer page, Integer rows) {
        String userId = request.getRemoteUser();
        /** GET请求中文转码 */
        return orderService.findByPage(userId, page, rows);
    }

    // 生成微信支付二维码
    @GetMapping("/genPayCode")
    public Map<String, String> genPayCode(Long orderId) {
        /*// 获取登录用户名
        String userId = request.getRemoteUser();
        // 从 Redis 查询支付日志
        PayLog payLog = orderService.findPayLogFromRedis(userId);
        // 调用生成微信支付二维码方法
        return weixinPayService.genPayCode(payLog.getOutTradeNo(),
                String.valueOf(payLog.getTotalFee()));*/

        IdWorker idWorker = new IdWorker();
        String outTradeNo = String.valueOf(idWorker.nextId());
        Order order = orderService.findOrder(orderId);
        double money = order.getPayment().doubleValue();
        int i = (int) (money * 100);


        return weixinPayService.genPayCode(outTradeNo, String.valueOf(i));

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
                    /*orderService.changeOrderStatus(orderId);*/
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
